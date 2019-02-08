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
package com.mc1098.mk_eventstore.EventStore;

import com.mc1098.mk_eventstore.Entity.EntityToken;
import com.mc1098.mk_eventstore.Entity.Mk_Snapshot;
import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Event.Mk_Event;
import com.mc1098.mk_eventstore.Event.SimpleEventConverter;
import com.mc1098.mk_eventstore.Exception.EntityChronologicalException;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Exception.TransactionException;
import com.mc1098.mk_eventstore.FileSystem.PageFileSystem;
import com.mc1098.mk_eventstore.FileSystem.RelativeFileSystem;
import com.mc1098.mk_eventstore.Page.EntityPage;
import com.mc1098.mk_eventstore.Page.Mk_EntityPage;
import com.mc1098.mk_eventstore.Page.Mk_PageDirectory;
import com.mc1098.mk_eventstore.Page.PageDirectory;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionPage;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionConverter;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionWorker;
import com.mc1098.mk_eventstore.Transaction.Transaction;
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import com.mc1098.mk_eventstore.Transaction.TransactionType;
import com.mc1098.mk_eventstore.Transaction.TransactionWorker;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.mc1098.mk_eventstore.Page.EntityPageConverter;
import com.mc1098.mk_eventstore.Transaction.TransactionConverter;
import java.nio.file.Paths;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_EventStoreTest
{
    
    Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
    
    Event event = new Mk_Event("testEvent", "testEntity", 1, 0, LocalDateTime.now(), new HashMap<>());
    Event event2 = new Mk_Event("testEvent", "testEntity", 1, 1, LocalDateTime.now(), new HashMap<>());
    Event event3 = new Mk_Event("testEvent", "testEntity", 1, 2, LocalDateTime.now(), new HashMap<>());
    Event event4 = new Mk_Event("testEvent", "testEntity", 1, 3, LocalDateTime.now(), new HashMap<>());
    Event event5 = new Mk_Event("testEvent", "testEntity", 1, 4, LocalDateTime.now(), new HashMap<>());
    
    EntityPage page = new Mk_EntityPage(0, 0, 1, 3, 10, snapshot, 
                    new ArrayDeque<Event>(){{add(event); add(event2); add(event3);}});
    
    public Mk_EventStoreTest()
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
        File file = new File("Entity");
        if(file.exists())
        {
            for (File f : file.listFiles())
                f.delete();
            file.delete();
        }
    }

    @Test
    public void testCreate_Default() throws Exception
    {
        System.out.println("create_Default");
        
        EventStore result = Mk_EventStore.create();
        
        File transactionLog = new File("Entity/TL");
        File enmFile = new File("Entity/ENM");
        RelativeFileSystem rfs = PageFileSystem.ofRoot(Paths.get("Entity"));
        TransactionConverter parser = new Mk_TransactionConverter();
        TransactionPage transactionPage = new Mk_TransactionPage(transactionLog, parser);
        PageDirectory directory = Mk_PageDirectory.setup(rfs, new SimpleEventConverter(), transactionPage);
        EventStore expResult = new Mk_EventStore(directory, transactionPage, null);
        
        assertEquals(expResult, result);
        assertTrue(transactionLog.exists());
        assertTrue(enmFile.exists());
    }

    @Test
    public void testCreate_PageDirectory_TransactionPage() throws Exception
    {
        System.out.println("create");
        
        File transactionLog = new File("Entity/TL");
        transactionLog.getParentFile().mkdirs();
        transactionLog.createNewFile();
        File enmFile = new File("Entity/ENM");
        enmFile.createNewFile();
        RelativeFileSystem rfs = PageFileSystem.ofRoot(Paths.get("Entity"));
        TransactionConverter parser = new Mk_TransactionConverter();
        TransactionPage transactionPage = new Mk_TransactionPage(transactionLog, parser);
        PageDirectory directory = Mk_PageDirectory.setup(rfs, new SimpleEventConverter(), transactionPage);
        EventStore expResult = new Mk_EventStore(directory, transactionPage, null);
        
        EventStore result = Mk_EventStore.create(rfs, directory, transactionPage);
        
        assertEquals(expResult, result);
        assertTrue(transactionLog.exists());
        assertTrue(enmFile.exists());
    }
    
    @Test (expected = EventStoreException.class)
    public void testCreate_TransactionFileLocked() throws Exception
    {
        System.out.println("create_TransactionFileLocked");
        
        File transactionLog = new File("Entity/TL");
        transactionLog.getParentFile().mkdirs();
        transactionLog.createNewFile();
        RelativeFileSystem rfs = PageFileSystem.ofRoot(Paths.get("Entity"));
        try (FileChannel fc = FileChannel.open(transactionLog.toPath(), StandardOpenOption.WRITE); 
                FileLock fl = fc.lock())
        {
            
            TransactionConverter parser = new Mk_TransactionConverter();
            TransactionPage transactionPage = new Mk_TransactionPage(transactionLog, parser);
            PageDirectory directory = Mk_PageDirectory.setup(rfs, new SimpleEventConverter(), transactionPage);
            Mk_EventStore.create(rfs, directory, transactionPage);
        }
    }

    @Test
    public void testGetById_String_long() throws Exception
    {
        System.out.println("getById_latest");
        String entityName = "testEntity";
        long id = 1L;
        DummyPageDirectory dpd = new DummyPageDirectory();
        Mk_EventStore instance = new Mk_EventStore(dpd, null, null);
        EntityToken expResult = new EntityToken(new Mk_Snapshot("testEntity", id,
                0, new byte[]{10}), new Event[]{event, event2, event3});
        EntityToken result = instance.getById(entityName, id);
        
        assertEquals(expResult, result);
        assertTrue(dpd.wasGetEntityUsed);
        assertTrue(dpd.wasGetEntityPageUsed);
    }

    @Test
    public void testGetById_3args() throws Exception
    {
        System.out.println("getByIdAndVersion");
        String entityName = "testEntity";
        long id = 1L;
        long version = 1L;
        DummyPageDirectory dpd = new DummyPageDirectory();
        Mk_EventStore instance = new Mk_EventStore(dpd, null, null);
        EntityToken expResult = new EntityToken(new Mk_Snapshot(entityName, id, 
                0, new byte[]{10}), new Event[]{event});
        
        EntityToken result = instance.getById(entityName, id, version);
        assertEquals(expResult, result);
        assertTrue(dpd.wasGetEPRUsed);
        assertTrue(dpd.wasGetEntityPageUsed);
        assertTrue(dpd.wasGetEntityUsed);
    }
    
    @Test
    public void testGetEventsById_3args() throws Exception
    {
        System.out.println("getEventsById");
        String entityName = "testEntity";
        long id = 1L;
        long fromVer = 0L;
        DummyPageDirectory dpd = new DummyPageDirectory();
        Mk_EventStore instance = new Mk_EventStore(dpd, null, null);
        Queue<Event> expResult = new ArrayDeque<Event>(){{add(event); add(event2); add(event3);}};
        Queue<Event> result = instance.getEventsById(entityName, id, fromVer);
        
        assertArrayEquals(expResult.toArray(), result.toArray());
        assertTrue(dpd.wasGetEPRUsed);
        assertTrue(dpd.wasGetEntityPageUsed);
        assertTrue(dpd.wasGetEntityUsed);
    }

    @Test
    public void testGetEventsById_4args() throws Exception
    {
        System.out.println("getEventsById");
        String entityName = "testEntity";
        long id = 1L;
        long fromVer = 0L;
        long toVer = 2L;
        DummyPageDirectory dpd = new DummyPageDirectory();
        Mk_EventStore instance = new Mk_EventStore(dpd, null, null);
        Queue<Event> expResult = new ArrayDeque<Event>(){{add(event); add(event2);}};
        Queue<Event> result = instance.getEventsById(entityName, id, fromVer, toVer);
        
        assertArrayEquals(expResult.toArray(), result.toArray());
        assertTrue(dpd.wasGetEPRUsed);
        assertTrue(dpd.wasGetEntityPageUsed);
        assertTrue(dpd.wasGetEntityUsed);
    }

    @Test
    public void testGetSnapshot_String_long() throws Exception
    {
        System.out.println("getSnapshot");
        String entityName = "testEntity";
        long id = 1L;
        DummyPageDirectory dpd = new DummyPageDirectory();
        Mk_EventStore instance = new Mk_EventStore(dpd, null, null);
        Snapshot expResult = snapshot;
        Snapshot result = instance.getSnapshot(entityName, id);
        assertEquals(expResult, result);
        assertTrue(dpd.wasGetEntityPageUsed);
        assertTrue(dpd.wasGetEntityUsed);
    }

    @Test
    public void testGetSnapshot_3args() throws Exception
    {
        System.out.println("getSnapshot");
        String entityName = "testEntity";
        long id = 1L;
        long lteq = 0L;
        DummyPageDirectory dpd = new DummyPageDirectory();
        Mk_EventStore instance = new Mk_EventStore(dpd, null, null);
        Snapshot expResult = snapshot;
        Snapshot result = instance.getSnapshot(entityName, id, lteq);
        assertEquals(expResult, result);
        assertTrue(dpd.wasGetEPRUsed);
        assertTrue(dpd.wasGetEntityPageUsed);
        assertTrue(dpd.wasGetEntityUsed);
    }

    @Test
    public void testSaveSnapshot() throws Exception
    {
        System.out.println("saveSnapshot");
        Snapshot ss = snapshot;
        DummyPageDirectory dpd = new DummyPageDirectory();
        dpd.hasEntity = true;
        dpd.doesPageExist = false;
        DummyTransactionPage dtp = new DummyTransactionPage();
        Mk_EventStore instance = new Mk_EventStore(dpd, dtp, null);
        instance.saveSnapshot(ss);
        assertEquals(ss, dpd.createdPage.getSnapshot());
        assertTrue(dpd.wasGetEPRUsed);
        assertTrue(dpd.wasGetEntityUsed);
        assertTrue(dpd.wasCreatePendingUsed);
        assertTrue(dpd.wasConfirmPendingUsed);
        assertTrue(dtp.wasRefreshUsed);
        assertNotNull(dtp.writeTransaction);
        assertEquals(TransactionType.PUT_SNAPSHOT, dtp.writeTransaction.getType());
    }
    
    @Test (expected = EntityChronologicalException.class)
    public void testSaveInvalidSnapshot() throws Exception
    {
        System.out.println("saveInvalidSnapshot");
        Snapshot ss = new Mk_Snapshot("testEntity", 1, 5, new byte[]{10});
        DummyPageDirectory dpd = new DummyPageDirectory();
        dpd.hasEntity = true;
        dpd.doesPageExist = false;
        DummyTransactionPage dtp = new DummyTransactionPage();
        Mk_EventStore instance = new Mk_EventStore(dpd, dtp, null);
        instance.saveSnapshot(ss);
    }
    
    @Test (expected = EntityChronologicalException.class)
    public void testSaveDuplicateSnapshot() throws Exception
    {
        System.out.println("saveDuplicateSnapshot");
        
        Snapshot ss = snapshot;
        DummyPageDirectory dpd = new DummyPageDirectory();
        dpd.hasEntity = true;
        dpd.doesPageExist = true;
        DummyTransactionPage dtp = new DummyTransactionPage();
        Mk_EventStore instance = new Mk_EventStore(dpd, dtp, null);
        instance.saveSnapshot(ss);
    }
    

    @Test
    public void testSave_4args() throws Exception
    {
        System.out.println("save");
        String entityName = "testEntity";
        long id = 1L;
        long loadedVersion = 3L;
        Event[] events = new Event[]{event4, event5};
        DummyPageDirectory dpd = new DummyPageDirectory();
        DummyTransactionPage dtp = new DummyTransactionPage();
        Mk_EventStore instance = new Mk_EventStore(dpd, dtp, null);
        instance.save(entityName, id, loadedVersion, events);
        Queue<Event> pageEvents = new ArrayDeque<>(Arrays.asList(page.getEvents()));
        
        assertTrue(pageEvents.contains(events[0]));
        assertTrue(pageEvents.contains(events[1]));
        assertTrue(dpd.wasGetEntityUsed);
        assertNotNull(dtp.writeTransactions);
        assertTrue(dtp.writeTransactions.stream()
                .allMatch((t)->t.getType().equals(TransactionType.PUT_EVENT)));
        
        
    }

    @Test
    public void testSave_EntityToken() throws Exception
    {
        System.out.println("save");
        
        Event[] events = new Event[] {event4, event5};
        EntityToken token = new EntityToken(snapshot, events);
        
        DummyPageDirectory dpd = new DummyPageDirectory();
        DummyTransactionPage dtp = new DummyTransactionPage();
        Mk_EventStore instance = new Mk_EventStore(dpd, dtp, null);
        instance.save(token);
        Queue<Event> pageEvents = new ArrayDeque<>(Arrays.asList(dpd.createdPage.getEvents()));
        assertTrue(pageEvents.contains(events[0]));
        assertTrue(pageEvents.contains(events[1]));
        assertEquals(snapshot, dpd.createdPage.getSnapshot());
        assertTrue(dpd.wasGetEPRUsed);
        assertTrue(dpd.wasGetEntityUsed);
        assertTrue(dpd.wasCreatePendingUsed);
        assertTrue(dpd.wasConfirmPendingUsed);
        assertTrue(dtp.wasRefreshUsed);
        assertEquals(TransactionType.PUT_SNAPSHOT, dtp.writeTransactions.get(0).getType());
        assertEquals(TransactionType.PUT_EVENT, dtp.writeTransactions.get(1).getType());
        assertEquals(TransactionType.PUT_EVENT, dtp.writeTransactions.get(2).getType());
    }

    @Test
    public void testClose() throws Exception
    {
        System.out.println("close");
        
        TransactionWorker transactionWorker = new Mk_TransactionWorker(null, 
                new DummyTransactionPage(), new DummyPageDirectory(), null);
        Mk_EventStore instance = new Mk_EventStore(null, null, transactionWorker);
        transactionWorker.start();
        instance.close();
        assertTrue(transactionWorker.isShuttingDown());
        assertFalse(transactionWorker.isAlive());
    }
    
    @Test
    public void testEquals() throws Exception
    {
        System.out.println("equals");
        
        File transactionLog = new File("Entity/TL");
        File entityFile = transactionLog.getParentFile();
        entityFile.mkdirs();
        transactionLog.createNewFile();
        File enm = new File("Entity/ENM");
        enm.createNewFile();
        RelativeFileSystem rfs = PageFileSystem.ofRoot(Paths.get("Entity"));
        TransactionPage transactionPage = new Mk_TransactionPage(transactionLog, null);
        PageDirectory directory = Mk_PageDirectory.setup(rfs, null, transactionPage);
        
        Mk_EventStore es = new Mk_EventStore(directory, transactionPage, null);
        Mk_EventStore es2 = new Mk_EventStore(new DummyPageDirectory(), transactionPage, null);
        Mk_EventStore es3 = new Mk_EventStore(directory, new DummyTransactionPage(), null);
        
        assertEquals(es, es); //sanity check
        assertNotEquals(es, es2);
        assertNotEquals(es, es3);
        assertNotEquals(es, new Object());
        
    }
    
    
    class DummyPageDirectory implements PageDirectory
    {
        public boolean wasGetEntityUsed;
        public boolean wasGetEntityNameUsed;
        public boolean wasGetEntityPageUsed;
        public boolean wasGetEPRUsed;
        public boolean hasEntity;
        public boolean wasCreatePendingUsed;
        public boolean wasConfirmPendingUsed;
        public EntityPage createdPage;
        public boolean doesPageExist;

        @Override
        public String getEntityName(long entity)
        {
            wasGetEntityNameUsed = true;
            return "testEntity";
        }

        @Override
        public long getEntity(String entityName)
        {
            wasGetEntityUsed = true;
            return 0;
        }

        @Override
        public boolean hasEntity(String entityName)
        {
            return hasEntity;
        }

        @Override
        public int getEPR(long entity)
        {
            wasGetEPRUsed = true;
            return 10;
        }

        @Override
        public boolean doesPageExist(long entity, long entityId, long pageNo)
        {
            return doesPageExist;
        }

        @Override
        public EntityPage getEntityPage(long entity, long id) throws EventStoreException
        {
            wasGetEntityPageUsed = true;
            return page;
        }

        @Override
        public EntityPage getEntityPage(long entity, long id, long pageNo) throws EventStoreException
        {
            wasGetEntityPageUsed = true;
            return page;
        }

        @Override
        public List<EntityPage> getEntityPages(long entity, long id, long pageFrom) throws EventStoreException
        {
            return new ArrayList<EntityPage>(){{add(getEntityPage(entity, id, pageFrom));}};
        }

        @Override
        public List<EntityPage> getEntityPages(long entity, long id, long pageNo, long pageNo1) throws EventStoreException
        {
            return getEntityPages(entity, id, pageNo);
        }

        @Override
        public EntityPage createPendingEntityPage(long entity, long id, long pageNo, Snapshot snapshot)
        {
            wasCreatePendingUsed = true;
            createdPage = new Mk_EntityPage(0, entity, id, 10, snapshot);
            return createdPage;
        }

        @Override
        public EntityPage confirmPendingPage(EntityPage page) throws EventStoreException
        {
            wasConfirmPendingUsed = true;
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
    
    class DummyTransactionPage implements TransactionPage
    {
        
        public Transaction writeTransaction;
        public List<Transaction> writeTransactions;
        public boolean wasRefreshUsed;

        @Override
        public void writeTransaction(Transaction transaction) throws TransactionException
        {
            writeTransaction = transaction;
        }

        @Override
        public void writeTransaction(List<Transaction> transactions) throws TransactionException
        {
            writeTransactions = transactions;
        }

        @Override
        public boolean hasTransaction()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Transaction peek()
        {
            return null;
        }

        @Override
        public void confirmTransactionProcessed(Transaction transaction)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void truncateLog() throws IOException
        {}

        @Override
        public void refresh()
        {
            wasRefreshUsed = true;
        }
        
    }
    
}
