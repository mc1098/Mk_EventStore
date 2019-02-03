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
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_TransactionPageTest
{
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    private File transactionLogFile;
    
    public Mk_TransactionPageTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp() throws IOException
    {
        this.transactionLogFile = testFolder.newFile("TL");
    }
    
    @After
    public void tearDown()
    {
    }

    @Test
    public void testParse() throws Exception
    {
        System.out.println("parse");
        
        Transaction transaction = new Transaction(TransactionType.PUT_SNAPSHOT, 
                0, 0, 1, 0, new byte[]{111});
        TransactionParser parser = new Mk_TransactionParser();
        TransactionPage expResult = new Mk_TransactionPage(transactionLogFile, parser);
        expResult.writeTransaction(transaction);
        expResult.refresh();
        
        byte[] bytes = readFile(transactionLogFile);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        TransactionPage result = Mk_TransactionPage.parse(transactionLogFile, 
                buffer, parser);
        
        assertEquals(expResult, result);
    }
    
    @Test
    public void testParse_EmptyFile() throws Exception
    {
        System.out.println("parse_EmptyFile");
        
        TransactionParser parser = new Mk_TransactionParser();
        TransactionPage expResult = new Mk_TransactionPage(transactionLogFile, parser);
        
        byte[] bytes = readFile(transactionLogFile);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        TransactionPage result = Mk_TransactionPage.parse(transactionLogFile, 
                buffer, parser);
        
        assertEquals(expResult, result);
    }

    @Test
    public void testWriteTransaction_HasTransaction() throws Exception
    {
        System.out.println("writeTransaction_HasTransaction");
        Transaction expResult = new Transaction(TransactionType.PUT_EVENT, 0, 0, 1, 0, new byte[]{20, 10});
        TransactionParser parser = new Mk_TransactionParser();
        Mk_TransactionPage instance = new Mk_TransactionPage(transactionLogFile, parser);
        instance.writeTransaction(expResult);
        instance.refresh();
        byte[] bytes = readFile(transactionLogFile);
        Transaction result =  parser.parse(ByteBuffer.wrap(bytes));
        
        assertEquals(true, instance.hasTransaction());
        assertEquals(expResult, result);
        
    }

    @Test
    public void testWriteTransaction_List_HasTransaction() throws Exception
    {
        System.out.println("writeTransaction_List_HasTransaction");
        
        Transaction transaction1 = new Transaction(TransactionType.PUT_SNAPSHOT, 0, 1, 0, 0, new byte[]{10, 20});
        Transaction transaction2 = new Transaction(TransactionType.PUT_EVENT, 0, 1, 0, 0, new byte[]{20, 40});
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        TransactionParser parser = new Mk_TransactionParser();
        Mk_TransactionPage instance = new Mk_TransactionPage(transactionLogFile, parser);
        instance.writeTransaction(transactions);
        instance.refresh();
        
        byte[] bytes = readFile(transactionLogFile);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        
        assertEquals(true, instance.hasTransaction());
        assertEquals(transaction1, parser.parse(buffer));
        assertEquals(transaction2, parser.parse(buffer));
        
    }
    
    @Test
    public void testPoll() throws Exception
    {
        System.out.println("poll");
        long l = 2L;
        TimeUnit tu = TimeUnit.SECONDS;
        TransactionParser parser = new Mk_TransactionParser();
        Mk_TransactionPage instance = new Mk_TransactionPage(transactionLogFile, parser);
        Transaction expResult = new Transaction(TransactionType.PUT_SNAPSHOT, 0,
                0, l, 0, new byte[]{1, 50, 22, 12});
        
        instance.writeTransaction(expResult);
        instance.refresh(); //refresh called to confirm pending to be added to transaction queue.
        
        Transaction result = instance.poll(l, tu);
        assertEquals(expResult, result);
    }

    @Test
    public void testConfirmTransactionProcessed() throws TransactionException
    {
        System.out.println("confirmTransactionProcessed");
        Transaction transaction = new Transaction(TransactionType.PUT_SNAPSHOT, 
                0, 0, 1, 0, new byte[]{10});
        TransactionParser parser = new Mk_TransactionParser();
        Mk_TransactionPage instance = new Mk_TransactionPage(transactionLogFile, parser);
        instance.writeTransaction(transaction);
        instance.refresh();
        assertEquals(true, instance.hasTransaction());
        instance.confirmTransactionProcessed(transaction);
        assertEquals(false, instance.hasTransaction());
    }
    
    @Test
    public void testConfirmTransactionProcessed_NoTransactionQueued()
    {
        System.out.println("confirmTransactionProcessed_NoTransactionQueued");
        
        DummyLogHandler dlh = new DummyLogHandler();
        Logger.getLogger(Mk_TransactionPage.class.getName()).addHandler(dlh);
        
        Transaction transaction = new Transaction(TransactionType.PUT_SNAPSHOT, 
                0, 0, 1, 0, new byte[]{10});
        Mk_TransactionPage instance = new Mk_TransactionPage(null, null);
        assertEquals(false, instance.hasTransaction());
        instance.confirmTransactionProcessed(transaction);
        assertEquals(false, instance.hasTransaction());
        assertEquals(true, dlh.wasUsed);
    }

    @Test
    public void testTruncateLog() throws Exception
    {
        System.out.println("truncateLog");
        
        Transaction transaction = new Transaction(TransactionType.PUT_EVENT, 0, 0, 1, 0, new byte[]{33});
        TransactionParser parser = new Mk_TransactionParser();
        Mk_TransactionPage instance = new Mk_TransactionPage(transactionLogFile, parser);
        instance.writeTransaction(transaction);
        byte[] bytes = readFile(transactionLogFile);
        
        assertTrue("Expect file to contain bytes for the written transaction.",
                bytes.length > 0);
        instance.truncateLog();
        bytes = readFile(transactionLogFile);
        assertTrue("Expect file to be empty after truncateLog method was used.", 
                bytes.length == 0);
    }
    
    @Test
    public void testEquals() throws Exception
    {
        System.out.println("equals");
        
        File file = testFolder.newFile("test");
        TransactionParser parser = new Mk_TransactionParser();
        Transaction transaction = new Transaction(TransactionType.PUT_SNAPSHOT, 
                0, 0, 1, 0, new byte[]{10});
        
        TransactionPage page1 = new Mk_TransactionPage(transactionLogFile, null);
        TransactionPage page2 = new Mk_TransactionPage(file, null);
        TransactionPage page3 = new Mk_TransactionPage(transactionLogFile, parser);
        page3.writeTransaction(transaction);
        page3.refresh();
        
        assertEquals(page1, page1); //sanity check
        assertNotEquals(page1, page2);
        assertNotEquals(page1, page3);
        assertNotEquals(page1, new Object());
    }

    
    private byte[] readFile(File file) throws IOException
    {
        try(FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.READ))
        {
            int size = (int) fc.size();
            ByteBuffer buffer = ByteBuffer.allocate(size);
            fc.read(buffer);
            return buffer.array();
        }
    }
    
}

class DummyLogHandler extends Handler
{
    public boolean wasUsed;
    
    @Override
    public void publish(LogRecord lr)
    {
        this.wasUsed = true;
    }

    @Override
    public void flush()
    {
        //ignore
    }

    @Override
    public void close() throws SecurityException
    {
        //ignore
    }
    
}