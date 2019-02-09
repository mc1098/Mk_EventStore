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

import com.mc1098.mk_eventstore.Entity.Mk_Snapshot;
import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.SimpleEventConverter;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Exception.TransactionException;
import com.mc1098.mk_eventstore.Page.EntityPage;
import com.mc1098.mk_eventstore.Page.Mk_EntityPage;
import com.mc1098.mk_eventstore.Page.Mk_EntityPageConverter;
import com.mc1098.mk_eventstore.Page.PageDirectory;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.mc1098.mk_eventstore.Page.EntityPageConverter;
import com.mc1098.mk_eventstore.Event.EventConverter;
import com.mc1098.mk_eventstore.Exception.FileSystem.FileSystemException;
import com.mc1098.mk_eventstore.Exception.ParseException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import com.mc1098.mk_eventstore.FileSystem.ByteParser;
import com.mc1098.mk_eventstore.FileSystem.ByteSerializer;
import com.mc1098.mk_eventstore.FileSystem.RelativeFileSystem;
import com.mc1098.mk_eventstore.FileSystem.WriteOption;
import java.io.File;
import java.nio.file.Path;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_TransactionWorkerTest
{
    
    public Mk_TransactionWorkerTest()
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
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }
    
    @Test
    public void testProcessNullTransaction() throws Exception
    {
        System.out.println("processNullTransaction");
        
        Transaction transaction = null;
        
        DummyFileSystem dfs = new DummyFileSystem();
        DummyTransactionPage dtp = new DummyTransactionPage(transaction);
        DummyPageDirectory dpd = null;
        Mk_TransactionWorker instance = new Mk_TransactionWorker(dfs, dtp, dpd, null);
        instance.flush();
        
        assertTrue("Expect truncate log to be called when null transaction is "
                + "returned, which is only expected when the queue is empty.", 
                dfs.truncateFileUsed);
        
    }

    @Test
    public void testProcessSnapshotTransaction() throws Exception
    {
        System.out.println("processSnapshotTransaction");
        
        Transaction transaction = new Transaction(TransactionType.PUT_SNAPSHOT, 
                0, 0, 1, 0, new byte[]{22});
        
        DummyFileSystem dfs = new DummyFileSystem();
        DummyTransactionPage dtp = new DummyTransactionPage(transaction);
        DummyPageDirectory dpd = new DummyPageDirectory();
        EventConverter ef = new SimpleEventConverter();
        EntityPageConverter parser = new Mk_EntityPageConverter(dpd, ef);
        
        Mk_TransactionWorker instance = new Mk_TransactionWorker(dfs, dtp, dpd, parser);
        instance.flush();
        
        assertEquals("Expect transaction confirmed to be equal to the one loaded"
                + " into the TransactionPage and processed by TransactionWorker", 
                transaction, dtp.transaction);
        assertTrue("Expecting transaction to be confirmed after successful "
                + "processing",  dtp.wasTransactionConfirmedProcessed);
        assertNotNull("Processing of this transaction should have created a "
                + "new Entity page.", dpd.page);
        assertEquals(0, dpd.page.getPageId());
        assertEquals(0, dpd.page.getEntity());
        assertEquals(1, dpd.page.getEntityId());
        assertEquals(0, dpd.page.getVersion());
        assertTrue("Processing of this transaction should have called the "
                + "createPendingPage method on page directory.", 
                dpd.wasCreatePendingUsed);
        assertTrue("Processing of this transaction should have called the "
                + "confirmPendingPage method on page directory.", 
                dpd.wasPageConfirmed);
        
    }
    
    @Test
    public void testProcessSnapshotTransaction_AlreadyProcessed() throws Exception
    {
        System.out.println("processSnapshotTransaction_AlreadyProcessed");
        
        Transaction transaction = new Transaction(TransactionType.PUT_SNAPSHOT, 
                0, 0, 1, 0, new byte[]{22});
        
        EntityPage page = new Mk_EntityPage(0, 0, 1, 10, 
                new Mk_Snapshot("testEntity", 1, 0, new byte[]{10}));
        
        DummyFileSystem dfs = new DummyFileSystem();
        DummyTransactionPage dtp = new DummyTransactionPage(transaction);
        DummyPageDirectory dpd = new DummyPageDirectory();
        dpd.page = page;
        EventConverter ef = new SimpleEventConverter();
        EntityPageConverter parser = new Mk_EntityPageConverter(dpd, ef);
        
        Mk_TransactionWorker instance = new Mk_TransactionWorker(dfs, dtp, dpd, parser);
        instance.flush();
        
        assertEquals(transaction, dtp.transaction);
        assertEquals(true, dtp.wasTransactionConfirmedProcessed);
        assertNotNull(dpd.page);
        assertEquals(0, dpd.page.getPageId());
        assertEquals(0, dpd.page.getEntity());
        assertEquals(1, dpd.page.getEntityId());
        assertEquals(0, dpd.page.getVersion());
        assertFalse(dpd.wasCreatePendingUsed);
        assertFalse(dpd.wasPageConfirmed);
        
    }
    
    @Test(expected = EventStoreException.class)
    public void testProcessMalformedEventTransaction_Exception() throws Exception
    {
        System.out.println("processEventTransaction");
        
        Transaction transaction = new Transaction(TransactionType.PUT_EVENT, 
                0, 0, 1, 0, new byte[]{22});
        
        EntityPage page = new Mk_EntityPage(0, 0, 1, 10, 
                new Mk_Snapshot("testEntity", 1, 0, new byte[]{10}));
        
        DummyTransactionPage dtp = new DummyTransactionPage(transaction);
        DummyPageDirectory dpd = new DummyPageDirectory();
        dpd.page = page;
        EventConverter ef = new SimpleEventConverter();
        EntityPageConverter parser = new Mk_EntityPageConverter(dpd, ef);
        
        Mk_TransactionWorker instance = new Mk_TransactionWorker(null, dtp, dpd, parser);
        instance.flush();
        
    }
    
    
    

    @Test
    public void testStopAfterTransaction() throws Exception
    {
        System.out.println("stopAfterTransaction");
        Mk_TransactionWorker instance = new Mk_TransactionWorker(null, null, null, null);
        instance.stopAfterTransaction();
        assertTrue(instance.isShuttingDown());
    }
    
    class DummyTransactionPage implements TransactionPage
    {
        int transactions = 1;
        public Transaction transaction;
        public boolean wasTransactionConfirmedProcessed;
        
        public DummyTransactionPage(Transaction transaction)
        {
            this.transaction = transaction;
        }

        @Override
        public void writeTransaction(Transaction transaction) throws TransactionException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void writeTransaction(List<Transaction> transactions) throws TransactionException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean hasTransaction() 
        {
            if(transactions > 0)
            {
                transactions-=1;
                return true;
            }
            return false;
        }

        @Override
        public Transaction peek()
        {
            return transaction;
        }

        @Override
        public void confirmTransactionProcessed(Transaction transaction)
        {
            if(this.transaction.equals(transaction))
                wasTransactionConfirmedProcessed = true;
        }

        @Override
        public void truncateLog() 
        {
            //ignore
        }

        @Override
        public void refresh()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
    
    class DummyPageDirectory implements PageDirectory
    {
        
        public EntityPage page;
        public boolean wasPageConfirmed;
        public boolean wasGetPageUsed;
        public boolean wasCreatePendingUsed;

        @Override
        public String getEntityName(long entity) {return "testEntity";}

        @Override
        public long getEntity(String entityName)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean hasEntity(String entityName)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getEPR(long entity) {return 10;}

        @Override
        public boolean doesPageExist(long entity, long entityId, long pageNo)
        {
            return page != null;
        }

        @Override
        public EntityPage getEntityPage(long entity, long id) throws EventStoreException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EntityPage getEntityPage(long entity, long id, long pageNo) throws EventStoreException
        {
            wasGetPageUsed = true;
            return page;
        }

        @Override
        public List<EntityPage> getEntityPages(long entity, long id, long pageFrom) throws EventStoreException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<EntityPage> getEntityPages(long entity, long id, long pageNo, long pageNo1) throws EventStoreException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EntityPage createPendingEntityPage(long entity, long id, long pageNo, Snapshot snapshot)
        {
            this.wasCreatePendingUsed = true;
            page = new Mk_EntityPage(pageNo, entity, id, 10, snapshot);
            return page;
        }

        @Override
        public EntityPage confirmPendingPage(EntityPage page) throws EventStoreException
        {
            if(this.page.equals(page))
                wasPageConfirmed = true;
            return page;
        }

        @Override
        public TransactionPage getTransactionPage()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EntityPageConverter getEntityPageConverter()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    class DummyFileSystem implements RelativeFileSystem
    {

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
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> void serializeAndWrite(WriteOption wo, ByteSerializer<T> serializer, List<T> list, String... strings) throws FileSystemException, SerializationException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void truncateFile(String... strings) throws FileSystemException
        {
            this.truncateFileUsed = true;
        }
        
    }
    
}

