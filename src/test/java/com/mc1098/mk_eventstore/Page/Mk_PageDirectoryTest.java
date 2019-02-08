/*
 * Copyright (C) 2019 Max
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
package com.mc1098.mk_eventstore.Page;

import com.mc1098.mk_eventstore.Entity.Mk_Snapshot;
import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.SimpleEventConverter;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Exception.FileSystem.FileSystemException;
import com.mc1098.mk_eventstore.Exception.NoPageFoundException;
import com.mc1098.mk_eventstore.Exception.ParseException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import com.mc1098.mk_eventstore.Exception.TransactionException;
import com.mc1098.mk_eventstore.FileSystem.ByteParser;
import com.mc1098.mk_eventstore.FileSystem.ByteSerializer;
import com.mc1098.mk_eventstore.FileSystem.PageFileSystem;
import com.mc1098.mk_eventstore.FileSystem.RelativeFileSystem;
import com.mc1098.mk_eventstore.FileSystem.WriteOption;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionPage;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionConverter;
import com.mc1098.mk_eventstore.Transaction.Transaction;
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.mc1098.mk_eventstore.Transaction.TransactionConverter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *
 * @author Max
 */
public class Mk_PageDirectoryTest
{
    
    public Mk_PageDirectoryTest()
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
        
        File file = new File("Entity/0/1");
        if(file.exists())
        {
            for (File f : file.listFiles())
                f.delete();
            file.delete();
        }
        
        file = new File("Entity/0");
        if(file.exists())
        {
            for (File f : file.listFiles())
                f.delete();
            file.delete();
        }
        
        file = new File("Entity");
        if(file.exists())
            file.delete();
        
        
    }

    @Test
    public void testSetup() throws Exception
    {
        System.out.println("setup");
        
        EntityMetaData emd = new EntityMetaData("name", 3, 20);
        DummyFileSystem dfs = new DummyFileSystem();
        dfs.recursiveReturnList = new ArrayList<EntityMetaData>(){{add(emd);}};
        TransactionPage transactionPage = new DummyTransactionPage();
        
        
        Mk_PageDirectory expResult = new Mk_PageDirectory(dfs, null, 
                transactionPage, 
                new HashMap<String, Long>(){{put(emd.getName(), emd.getEntity());}}, 
                new HashMap<Long, Integer>(){{put(emd.getEntity(), emd.getErp());}});
        Mk_PageDirectory result = Mk_PageDirectory.setup(dfs, null, transactionPage);
        
        assertEquals(expResult, result);
        assertTrue(dfs.readAndParseRecursivelyUsed);
        
    }
    
    @Test
    public void testSetup_NoENMData() throws Exception
    {
        System.out.println("setup_NoENMData");
        
        TransactionPage transactionPage = new DummyTransactionPage();
        DummyFileSystem dfs = new DummyFileSystem();
        Mk_PageDirectory expResult = new Mk_PageDirectory(dfs, null, 
                transactionPage, 
                new HashMap<>(), new HashMap<>());
        Mk_PageDirectory result = Mk_PageDirectory.setup(dfs, null, transactionPage);
        
        assertEquals(expResult, result);
        assertTrue(dfs.readAndParseRecursivelyUsed);
        
    }
    
    @Test(expected = FileSystemException.class)
    public void testSetup_NoENMFile() throws Exception
    {
        System.out.println("setup_NoENMFile");
        
        TransactionPage transactionPage = new DummyTransactionPage();
        RelativeFileSystem rfs = PageFileSystem.ofRoot(Paths.get("Entity"));
        Mk_PageDirectory.setup(rfs, null, transactionPage);
    }

    
    @Test
    public void testGetEntityName()
    {
        System.out.println("getEntityName");
        long entity = 1L;
        Map<String, Long> names = new HashMap<String, Long>(){{put("testEntity", 1L);}};
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, null, names, null);
        String expResult = "testEntity";
        String result = instance.getEntityName(entity);
        assertEquals(expResult, result);
        assertNull(instance.getEntityName(23L));
    }

    @Test
    public void testHasEntity()
    {
        System.out.println("hasEntity");
        String entityName = "testEntity";
        Map<String, Long> names = new HashMap<String, Long>(){{put(entityName, 1L);}};
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, null, names, null);
        boolean expResult = true;
        boolean result = instance.hasEntity(entityName);
        assertEquals(expResult, result);
        assertFalse(instance.hasEntity("NotValid"));
    }

    @Test
    public void testGetEntity()
    {
        System.out.println("getEntity");
        String entityName = "testEntity";
        Map<String, Long> names = new HashMap<String, Long>() {{put(entityName, 1L);}};
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, null, names, null);
        long expResult = 1L;
        long result = instance.getEntity(entityName);
        assertEquals(expResult, result);
        assertEquals(expResult + 1, instance.getEntity("newEntity"));
    }

    @Test
    public void testGetEPR()
    {
        System.out.println("getEPR");
        long entity = 1L;
        Map<Long, Integer> epr = new HashMap<Long, Integer>(){{put(entity, 10);}};
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, null, null, epr);
        int expResult = 10;
        int result = instance.getEPR(entity);
        assertEquals(expResult, result);
        assertEquals(20, instance.getEPR(22));
    }

    @Test
    public void testGetEntityPageEntityAndId() throws Exception
    {
        System.out.println("getEntityPageEntityAndId");
        
        //file setup
        File file = new File("Entity/0/1/0");
        file.getParentFile().mkdirs();
        file.createNewFile();
        
        long entity = 1L;
        long id = 1L;
        EntityPage expResult = new Mk_EntityPage(0, entity, id, 10, 
                new Mk_Snapshot("testEntity", entity, 0, new byte[]{10}));
        DummyFileSystem dfs = new DummyFileSystem();
        dfs.readAndParseResult = expResult;
        Mk_PageDirectory instance = Mk_PageDirectory.setup(dfs, null, null);
        
        EntityPage result = instance.getEntityPage(entity, id);
        assertEquals(expResult, result);
        assertTrue(dfs.readAndParseUsed);
        assertTrue(dfs.getDirectoryUsed);
    }
    
    @Test(expected = NoPageFoundException.class)
    public void testGetEntityPageEntityAndId_NoPageFound() throws Exception
    {
        System.out.println("getEntityPageEntityAndId_NoPageFound");
        
        long entity = 1L;
        long id = 1L;
        DummyFileSystem dfs = new DummyFileSystem();
        dfs.getDirectoryException = true;
        Mk_PageDirectory instance = Mk_PageDirectory.setup(dfs, null, null);
        
        instance.getEntityPage(entity, id);
    }
    
    @Test
    public void testDoesPageExist() throws Exception
    {
        System.out.println("doesPageExist");
        
        File file = new File("Entity/0/1/0");
        file.getParentFile().mkdirs();
        file.createNewFile();
        
        long entity = 0L;
        long id = 1L;
        long pageNo = 0L;
        RelativeFileSystem rfs = PageFileSystem.ofRoot(Paths.get("Entity"));
        Mk_PageDirectory instance = new Mk_PageDirectory(rfs, null, null, null, null);
        boolean expResult = true;
        boolean result = instance.doesPageExist(entity, id, pageNo);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testDoesPageExist_NoSuchFilePageExists() throws Exception
    {
        System.out.println("doesPageExist_NoSuchPageFileExists");
        
        long entity = 0L;
        long id = 1L;
        long pageNo = 0L;
        RelativeFileSystem rfs = PageFileSystem.ofRoot(Paths.get("Entity"));
        Mk_PageDirectory instance = new Mk_PageDirectory(rfs, null, null, null, null);
        boolean expResult = false;
        boolean result = instance.doesPageExist(entity, id, pageNo);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetEntityPageEntityIdPageNo() throws Exception
    {
        System.out.println("getEntityPageEntityIdPageNo");
        long entity = 0L;
        long id = 1L;
        long pageNo = 0L;
        EntityPage expResult = new Mk_EntityPage(pageNo, entity, id, 10, 
                new Mk_Snapshot("testEntity", id, 0, new byte[]{10}));
        DummyFileSystem dfs = new DummyFileSystem();
        dfs.readAndParseResult = expResult;
        Mk_PageDirectory instance = new Mk_PageDirectory(dfs, null, null, null, null);
        EntityPage result = instance.getEntityPage(entity, id, pageNo);
        assertEquals(expResult, result);
        assertTrue(dfs.readAndParseUsed);
    }
    
    //@Test
    public void testGetEntityPages_3args() throws Exception
    {
        System.out.println("getEntityPages");
        long entity = 0L;
        long id = 0L;
        long pageFrom = 0L;
        Mk_PageDirectory instance = null;
        List<EntityPage> expResult = null;
        List<EntityPage> result = instance.getEntityPages(entity, id, pageFrom);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

//    @Test
    public void testGetEntityPages_4args() throws Exception
    {
        System.out.println("getEntityPages");
        long entity = 0L;
        long id = 0L;
        long pageNo = 0L;
        long pageNo1 = 0L;
        Mk_PageDirectory instance = null;
        List<EntityPage> expResult = null;
        List<EntityPage> result = instance.getEntityPages(entity, id, pageNo, pageNo1);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testCreatePendingEntityPage()
    {
        System.out.println("createPendingEntityPage");
        long entity = 0L;
        long id = 1L;
        long pageNo = 0L;
        Snapshot snapshot = new Mk_Snapshot("testEntity", id, 0, new byte[]{10});
        Map<Long, Integer> epr = new HashMap<Long, Integer>(){{put(0L, 10);}};
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, null, null, epr);
        EntityPage expResult = new Mk_EntityPage(pageNo, entity, id, 10, snapshot);
        EntityPage result = instance.createPendingEntityPage(entity, id, pageNo, snapshot);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testConfirmPendingPage() throws Exception
    {
        System.out.println("confirmPendingPage");
        
        DummyFileSystem dfs = new DummyFileSystem();
        Mk_PageDirectory instance = new Mk_PageDirectory(dfs, 
                new SimpleEventConverter(), null, new HashMap<>(), 
                new HashMap<>());
        EntityPage expResult = instance.createPendingEntityPage(0, 1, 0, 
                new Mk_Snapshot("testEntity", 1, 0, new byte[]{10}));
        EntityPage result = instance.confirmPendingPage(expResult);
        assertEquals(expResult, result);
        assertEquals(2, dfs.writeUsed);
        assertTrue(dfs.doesFileExistUsed);
        assertTrue(dfs.createFileUsed);
        
    }
    
    @Test(expected = EventStoreException.class)
    public void testConfirmPendingPage_NoPendingPage() throws Exception
    {
        System.out.println("confirmPendingPage_NoPendingPage");
        
        DummyFileSystem dfs = new DummyFileSystem();
        Mk_PageDirectory instance = new Mk_PageDirectory(dfs, 
                new SimpleEventConverter(), null, new HashMap<>(), 
                new HashMap<>());
        EntityPage expResult = new Mk_EntityPage(0, 0, 1, 20, 
                new Mk_Snapshot("testEntity", 1, 0, new byte[]{10}));
        instance.confirmPendingPage(expResult);
    }
    
    @Test(expected = EventStoreException.class)
    public void testConfirmPendingPage_PageAlreadyExists() throws Exception
    {
        System.out.println("confirmPendingPage_PageAlreadyExists");
        
        DummyFileSystem dfs = new DummyFileSystem();
        dfs.doesFileExistResult = true;
        Mk_PageDirectory instance = new Mk_PageDirectory(dfs, 
                new SimpleEventConverter(), null, 
                new HashMap<String, Long>(){{put("testEntity", 1L);}}, 
                new HashMap<>());
        EntityPage expResult = instance.createPendingEntityPage(0, 1, 0, 
                new Mk_Snapshot("testEntity", 1, 0, new byte[]{10}));
        instance.confirmPendingPage(expResult);
    }
    
    @Test
    public void testGetTransactionPage()
    {
        System.out.println("getTransactionPage");
        
        TransactionPage expResult = new Mk_TransactionPage(new File("Entity/TL"), 
                        new Mk_TransactionConverter());
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, 
                expResult, null, null);
        
        TransactionPage result = instance.getTransactionPage();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testEquals()
    {
        System.out.println("equals");
        
        TransactionConverter tParser = new Mk_TransactionConverter();
        TransactionPage tp = new Mk_TransactionPage(new File("Entity/TL"), tParser);
        Map<String, Long> names = new HashMap<>();
        Map<String, Long> names2 = new HashMap<String, Long>() {{put("k", 1L);}};
        Map<Long, Integer> erp = new HashMap<>();
        Map<Long, Integer> erp2 = new HashMap<Long, Integer>() {{put(1L, 2);}};
        
        PageDirectory dir = new Mk_PageDirectory(null, null, tp, names, erp);
        PageDirectory dir2 = new Mk_PageDirectory(null, null, new DummyTransactionPage(), names, erp);
        PageDirectory dir3 = new Mk_PageDirectory(null, null, tp, names2, erp);
        PageDirectory dir4 = new Mk_PageDirectory(null, null, tp, names, erp2);
        
        assertEquals(dir, dir); //sanity check
        assertNotEquals(dir, dir2);
        assertNotEquals(dir, dir3);
        assertNotEquals(dir, dir4);
        assertNotEquals(dir, new Object());
        
    }
    
    class DummyFileSystem implements RelativeFileSystem
    {
        public Object readAndParseResult;
        public List recursiveReturnList = new ArrayList();
        public boolean readAndParseRecursivelyUsed;
        public boolean readAndParseUsed;
        public boolean getDirectoryUsed;
        public boolean getDirectoryException;
        public boolean createFileUsed;
        public boolean doesFileExistUsed;
        public boolean doesFileExistResult;
        public int writeUsed;

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
        
        public File getDirectory(String...strings) throws FileSystemException
        {
            if(getDirectoryException)
                throw new FileSystemException("Intentional test exception");
            this.getDirectoryUsed = true;
            return new File("Entity/0/1");
        }

        @Override
        public File getOrCreateDirectory(String... strings) throws FileSystemException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        @Override
        public File getFile(String...strings) throws FileSystemException
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void createFile(String... strings) throws FileSystemException
        {
            this.createFileUsed = true;
        }

        @Override
        public File getOrCreateFile(String... strings) throws FileSystemException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean doesFileExist(String... strings)
        {
            this.doesFileExistUsed = true;
            return doesFileExistResult;
        }

        @Override
        public byte[] read(String... strings) throws FileSystemException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> T readAndParse(ByteParser<T> parser, String... strings) throws FileSystemException, ParseException
        {
            this.readAndParseUsed = true;
            return (T) readAndParseResult;
        }

        @Override
        public <T> List<T> readAndParseRecursively(ByteParser<T> parser, String... strings) throws FileSystemException, ParseException
        {
            this.readAndParseRecursivelyUsed = true;
            return (List<T>) recursiveReturnList;
        }

        @Override
        public void write(WriteOption wo, byte[] bytes, String... strings) throws FileSystemException
        {
            this.writeUsed += 1;
        }

        @Override
        public <T> void serializeAndWrite(WriteOption wo, ByteSerializer<T> serializer, List<T> list, String... strings) throws FileSystemException, SerializationException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    class DummyTransactionPage implements TransactionPage
    {

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
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Transaction peek()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void confirmTransactionProcessed(Transaction transaction)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void truncateLog() throws IOException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void refresh()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
}
