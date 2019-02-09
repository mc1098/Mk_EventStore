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

import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.FileSystem.RelativeFileSystem;
import com.mc1098.mk_eventstore.FileSystem.WriteOption;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_TransactionPage implements TransactionPage
{
    
    private static final Logger LOGGER = Logger.getLogger(Mk_TransactionPage.class.getName());
    
    private final AtomicBoolean truncatePending;
    private final RelativeFileSystem fileSystem;
    private final BlockingQueue<Transaction> transactions;
    private final Queue<Transaction> pending;
    private final TransactionConverter parser;
    private static final String TL = "TL";
    
    public Mk_TransactionPage(RelativeFileSystem rfs, 
            TransactionConverter parser)
    {
        this.truncatePending = new AtomicBoolean();
        this.fileSystem = rfs;
        this.transactions = new ArrayBlockingQueue(200, true);
        this.pending = new ArrayDeque<>();
        this.parser = parser;
    }
    
    public Mk_TransactionPage(RelativeFileSystem rfs, 
            Queue<Transaction> transactions, TransactionConverter parser)
    {
        this.truncatePending = new AtomicBoolean();
        this.fileSystem = rfs;
        this.transactions = new ArrayBlockingQueue<>(200, true, transactions);
        this.pending = new ArrayDeque<>();
        this.parser = parser;
    }

    @Override
    public void writeTransaction(Transaction transaction) throws EventStoreException
    {
        if(truncatePending.get())
            fileSystem.truncateFile(TL);
        byte[] bytes = parser.toBytes(transaction);
        fileSystem.write(WriteOption.APPEND, bytes, TL);
        pending.add(transaction);
        truncatePending.set(false);
    }

    @Override
    public void writeTransaction(List<Transaction> transactions) 
            throws EventStoreException
    {
        if(truncatePending.get())
            fileSystem.truncateFile(TL);
        fileSystem.serializeAndWrite(WriteOption.APPEND, parser, transactions, 
                TL);
        this.pending.addAll(transactions);
        truncatePending.set(false);
    }
    
    @Override
    public boolean hasTransaction() {return !transactions.isEmpty();}
    
    @Override
    public Transaction peek()
    {
        return transactions.peek();
    }

    @Override
    public void confirmTransactionProcessed(Transaction transaction)
    {
        if(!transactions.remove(transaction))
            LOGGER.log(Level.INFO, "Removal of transaction failed when "
                    + "confirming it was processed.");
    }
    
    @Override
    public void truncateLog()
    {
        truncatePending.set(true);
    }
    
    @Override
    public void refresh() 
    {
        transactions.addAll(pending);
        pending.clear();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Mk_TransactionPage))
            return false;
        
        Mk_TransactionPage tp = (Mk_TransactionPage)o;
        
        //blockingQueue#equals was return false despite both queues containing same 
        return Arrays.equals(transactions.toArray(), tp.transactions.toArray());
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.transactions);
        return hash;
    }
    
}
