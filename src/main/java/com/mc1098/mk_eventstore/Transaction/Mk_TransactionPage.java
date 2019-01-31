/*
 * Copyright (C) 2019 Max Cripps <43726912+mc1098@users.noreply.github.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mc1098.mk_eventstore.Transaction;

import com.mc1098.mk_eventstore.Exception.TransactionException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_TransactionPage implements TransactionPage
{
    
    public static TransactionPage parse(File file, ByteBuffer buffer, 
            TransactionParser transactionParser)
    {
        buffer.rewind();
        Queue<Transaction> transactions = new ArrayDeque<>();
        while (buffer.hasRemaining())
            transactions.add(transactionParser.parse(buffer));
        
        return new Mk_TransactionPage(transactions, file, transactionParser);
    }
    
    private final BlockingQueue<Transaction> transactions;
    private final Queue<Transaction> pending;
    private final TransactionParser parser;
    private final File file;
    
    public Mk_TransactionPage(File file, TransactionParser parser)
    {
        this.transactions = new ArrayBlockingQueue(200, true);
        this.pending = new ArrayDeque<>();
        this.parser = parser;
        this.file = file;
    }
    
    private Mk_TransactionPage(Queue<Transaction> transactions, File file, 
            TransactionParser parser)
    {
        this.transactions = new ArrayBlockingQueue<>(200, true, transactions);
        this.pending = new ArrayDeque<>();
        this.parser = parser;
        this.file = file;
    }

    @Override
    public void writeTransaction(Transaction transaction) throws TransactionException
    {
        ByteBuffer buffer = ByteBuffer.wrap(parser.toBytes(transaction));
        writeBytes(buffer);
        pending.add(transaction);
    }

    @Override
    public void writeTransaction(List<Transaction> transactions) 
            throws TransactionException
    {
        int size = 0;
        byte[][] arryBytes = new byte[transactions.size()][0];
        for (int i = 0; i < transactions.size(); i++)
        {
            byte[] bytes = parser.toBytes(transactions.get(i));
            size += bytes.length;
            arryBytes[i] = bytes;
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(size);
        for (byte[] bytes : arryBytes)
            buffer.put(bytes);
        
        writeBytes(buffer);
        this.pending.addAll(transactions);
    }
    
    private synchronized void writeBytes(ByteBuffer buffer) 
            throws TransactionException
    {
        try (FileChannel fc = FileChannel.open(file.toPath(), 
                StandardOpenOption.APPEND))
        {
            buffer.rewind();
            fc.write(buffer);
        } catch(IOException ex)
        {
            throw new TransactionException(ex);
        }
    }
    
    @Override
    public boolean hasTransaction() {return !transactions.isEmpty();}
    
    @Override
    public Transaction poll(long l, TimeUnit tu) throws InterruptedException
    {
        return transactions.poll(l, tu);
    }

    @Override
    public void confirmTransactionProcessed(Transaction transaction)
    {
        if(transactions.contains(transaction))
            transactions.remove();
    }
    
    @Override
    public void truncateLog() throws IOException
    {
        try(FileChannel fc = FileChannel.open(file.toPath(), 
                StandardOpenOption.WRITE))
        {
            if(fc.size() != 0)
                fc.truncate(0);
        }
    }
    
    @Override
    public void refresh() 
    {
        transactions.addAll(pending);
        pending.clear();
    }
    
}
