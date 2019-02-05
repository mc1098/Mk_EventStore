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
import com.mc1098.mk_eventstore.Event.EventFormat;
import com.mc1098.mk_eventstore.Event.SimpleEventFormat;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionPage;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionParser;
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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

//    @Test
//    public void testSetup() throws Exception
//    {
//        System.out.println("setup");
//        EventFormat ef = null;
//        TransactionPage transactionPage = null;
//        Mk_PageDirectory expResult = null;
//        Mk_PageDirectory result = Mk_PageDirectory.setup(ef, transactionPage);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    
    @Test
    public void testGetEntityName()
    {
        System.out.println("getEntityName");
        long entity = 1L;
        Map<String, Long> names = new HashMap<String, Long>(){{put("testEntity", 1L);}};
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, names, null);
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
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, names, null);
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
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, names, null);
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
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, null, epr);
        int expResult = 10;
        int result = instance.getEPR(entity);
        assertEquals(expResult, result);
        assertEquals(20, instance.getEPR(22));
    }

    //@Test
    public void testGetEntityPage_long_long() throws Exception
    {
        System.out.println("getEntityPage");
        long entity = 0L;
        long id = 0L;
        Mk_PageDirectory instance = null;
        EntityPage expResult = null;
        EntityPage result = instance.getEntityPage(entity, id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test (expected = EventStoreException.class)
    public void testGetEntityPageWhenNoSuchPageFileExists() throws Exception 
    {
        System.out.println("getEntityPageWhenNoSuchPageFileExists");
        
        long entity = 0L;
        long id = 1L;
        
        Map<Long, Integer> epr = new HashMap<Long, Integer>(){{put(entity, 10);}};
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, null, epr);
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
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, null, null);
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
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, null, null);
        boolean expResult = false;
        boolean result = instance.doesPageExist(entity, id, pageNo);
        assertEquals(expResult, result);
    }
    
    //@Test
    public void testGetEntityPage_3args() throws Exception
    {
        System.out.println("getEntityPage");
        long entity = 0L;
        long id = 0L;
        long pageNo = 0L;
        Mk_PageDirectory instance = null;
        EntityPage expResult = null;
        EntityPage result = instance.getEntityPage(entity, id, pageNo);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
        Mk_PageDirectory instance = new Mk_PageDirectory(null, null, null, epr);
        EntityPage expResult = new Mk_EntityPage(pageNo, entity, id, 10, snapshot);
        EntityPage result = instance.createPendingEntityPage(entity, id, pageNo, snapshot);
        assertEquals(expResult, result);
    }
    
    //@Test
    public void testConfirmPendingPage() throws Exception
    {
        System.out.println("confirmPendingPage");
        EntityPage page = null;
        Mk_PageDirectory instance = null;
        EntityPage expResult = null;
        EntityPage result = instance.confirmPendingPage(page);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    public void testGetTransactionPage()
    {
        System.out.println("getTransactionPage");
        
        TransactionPage expResult = new Mk_TransactionPage(new File("Entity/TL"), 
                        new Mk_TransactionParser());
        Mk_PageDirectory instance = new Mk_PageDirectory(null, 
                expResult, null, null);
        
        TransactionPage result = instance.getTransactionPage();
        assertEquals(expResult, result);
    }
    
    //@Test
    public void testSetEntityPageParser()
    {
        System.out.println("setEntityPageParser");
        EntityPageParser parser = null;
        Mk_PageDirectory instance = null;
        instance.setEntityPageParser(parser);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

//    @Test
    public void testEquals()
    {
        System.out.println("equals");
        Object o = null;
        Mk_PageDirectory instance = null;
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }
    
}
