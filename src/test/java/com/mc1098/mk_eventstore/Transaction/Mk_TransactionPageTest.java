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
import com.mc1098.mk_eventstore.Exception.FileSystem.FileSystemException;
import com.mc1098.mk_eventstore.Exception.ParseException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import com.mc1098.mk_eventstore.FileSystem.ByteParser;
import com.mc1098.mk_eventstore.FileSystem.ByteSerializer;
import com.mc1098.mk_eventstore.FileSystem.RelativeFileSystem;
import com.mc1098.mk_eventstore.FileSystem.WriteOption;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_TransactionPageTest
{
    
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
    }
    
    @After
    public void tearDown()
    {
    }

    @Test
    public void testWriteTransaction_HasTransaction() throws Exception
    {
        System.out.println("writeTransaction_HasTransaction");
        Transaction expResult = new Transaction(TransactionType.PUT_EVENT, 0, 0, 1, 0, new byte[]{20, 10});
        DummyFileSystem dfs = new DummyFileSystem();
        TransactionConverter converter = new Mk_TransactionConverter();
        Mk_TransactionPage instance = new Mk_TransactionPage(dfs, converter);
        instance.writeTransaction(expResult);
        instance.refresh();
        
        assertEquals(true, instance.hasTransaction());
        assertTrue(dfs.writeUsed);
        
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
        DummyFileSystem dfs = new DummyFileSystem();
        TransactionConverter converter = new Mk_TransactionConverter();
        Mk_TransactionPage instance = new Mk_TransactionPage(dfs, converter);
        instance.writeTransaction(transactions);
        instance.refresh();
        
        assertEquals(true, instance.hasTransaction());
        assertTrue(dfs.serializeAndWriteUsed);
    }
    
    @Test
    public void testPeek() throws Exception
    {
        System.out.println("peek");
        long l = 2L;
        DummyFileSystem dfs = new DummyFileSystem();
        TransactionConverter converter = new Mk_TransactionConverter();
        Mk_TransactionPage instance = new Mk_TransactionPage(dfs, converter);
        Transaction expResult = new Transaction(TransactionType.PUT_SNAPSHOT, 0,
                0, l, 0, new byte[]{1, 50, 22, 12});
        
        instance.writeTransaction(expResult);
        instance.refresh(); //refresh called to confirm pending to be added to transaction queue.
        
        Transaction result = instance.peek();
        assertEquals(expResult, result);
        assertTrue(dfs.writeUsed);
    }

    @Test
    public void testConfirmTransactionProcessed() throws EventStoreException
    {
        System.out.println("confirmTransactionProcessed");
        Transaction transaction = new Transaction(TransactionType.PUT_SNAPSHOT, 
                0, 0, 1, 0, new byte[]{10});
        DummyFileSystem dfs = new DummyFileSystem();
        TransactionConverter converter = new Mk_TransactionConverter();
        Mk_TransactionPage instance = new Mk_TransactionPage(dfs, converter);
        instance.writeTransaction(transaction);
        instance.refresh();
        assertTrue(instance.hasTransaction());
        instance.confirmTransactionProcessed(transaction);
        assertFalse(instance.hasTransaction());
        assertTrue(dfs.writeUsed);
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
        DummyFileSystem dfs = new DummyFileSystem();
        TransactionConverter converter = new Mk_TransactionConverter();
        Mk_TransactionPage instance = new Mk_TransactionPage(dfs, converter);
        instance.truncateLog();
        //truncateLog flips an internal flag that is only checked on the 
        //next write request.
        instance.writeTransaction(transaction);
        
        assertTrue(dfs.truncateFileUsed);
        assertTrue(dfs.writeUsed);
        
    }
    
    @Test
    public void testEquals() throws Exception
    {
        System.out.println("equals");
        
        DummyFileSystem dfs = new DummyFileSystem();
        TransactionConverter converter = new Mk_TransactionConverter();
        Transaction transaction = new Transaction(TransactionType.PUT_SNAPSHOT, 
                0, 0, 1, 0, new byte[]{10});
        
        TransactionPage page1 = new Mk_TransactionPage(null, null);
        TransactionPage page2 = new Mk_TransactionPage(dfs, converter);
        page2.writeTransaction(transaction);
        page2.refresh();
        
        assertEquals(page1, page1); //sanity check
        assertNotEquals(page1, page2);
        assertNotEquals(page1, new Object());
    }

    
    class DummyFileSystem implements RelativeFileSystem
    {

        public boolean writeUsed;
        public boolean serializeAndWriteUsed;
        public boolean truncateFileUsed;

        @Override
        public String getRootPath()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Path getRelativePath(String... strings)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public File getDirectory(String... strings) throws FileSystemException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public File getOrCreateDirectory(String... strings) throws FileSystemException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public File getFile(String... strings) throws FileSystemException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void createFile(String... strings) throws FileSystemException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public File getOrCreateFile(String... strings) throws FileSystemException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean doesFileExist(String... strings)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public byte[] read(String... strings) throws FileSystemException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> T readAndParse(ByteParser<T> parser, String... strings) throws FileSystemException, ParseException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> List<T> readAndParseRecursively(ByteParser<T> parser, String... strings) throws FileSystemException, ParseException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void write(WriteOption wo, byte[] bytes, String... strings) throws FileSystemException
        {
            this.writeUsed = true;
        }

        @Override
        public <T> void serializeAndWrite(WriteOption wo, 
                ByteSerializer<T> serializer, List<T> list, String... strings) 
                throws FileSystemException, SerializationException
        {
            this.serializeAndWriteUsed = true;
        }

        @Override
        public void truncateFile(String... strings) throws FileSystemException
        {
            this.truncateFileUsed = true;
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