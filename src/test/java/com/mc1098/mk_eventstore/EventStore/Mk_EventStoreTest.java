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
import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Event.SimpleEventFormat;
import com.mc1098.mk_eventstore.Page.EntityPageParser;
import com.mc1098.mk_eventstore.Page.Mk_EntityPageParser;
import com.mc1098.mk_eventstore.Page.Mk_PageDirectory;
import com.mc1098.mk_eventstore.Page.PageDirectory;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionPage;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionParser;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionWorker;
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import com.mc1098.mk_eventstore.Transaction.TransactionParser;
import com.mc1098.mk_eventstore.Transaction.TransactionWorker;
import java.io.File;
import java.util.Queue;
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
public class Mk_EventStoreTest
{
    
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
    }

    @Test
    public void testCreate_Default() throws Exception
    {
        System.out.println("create_Default");
        
        EventStore result = Mk_EventStore.create();
        
        File transactionLog = new File("Entity/TL");
        File enmFile = new File("Entity/ENM");
        TransactionParser parser = new Mk_TransactionParser();
        TransactionPage transactionPage = new Mk_TransactionPage(transactionLog, parser);
        PageDirectory directory = Mk_PageDirectory.setup(new SimpleEventFormat(), transactionPage);
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
        transactionLog.mkdirs();
        transactionLog.createNewFile();
        File enmFile = new File("Entity/ENM");
        enmFile.createNewFile();
        TransactionParser parser = new Mk_TransactionParser();
        TransactionPage transactionPage = new Mk_TransactionPage(transactionLog, parser);
        PageDirectory directory = Mk_PageDirectory.setup(new SimpleEventFormat(), transactionPage);
        EventStore expResult = new Mk_EventStore(directory, transactionPage, null);
        
        EventStore result = Mk_EventStore.create(directory, transactionPage);
        
        assertEquals(expResult, result);
        assertTrue(transactionLog.exists());
        assertTrue(enmFile.exists());
    }

    //@Test
    public void testGetERP()
    {
        System.out.println("getERP");
        String entityName = "";
        Mk_EventStore instance = null;
        int expResult = 0;
        int result = instance.getERP(entityName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    //@Test
    public void testGetById_String_long() throws Exception
    {
        System.out.println("getById");
        String entityName = "";
        long id = 0L;
        Mk_EventStore instance = null;
        EntityToken expResult = null;
        EntityToken result = instance.getById(entityName, id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    //@Test
    public void testGetById_3args() throws Exception
    {
        System.out.println("getById");
        String entityName = "";
        long id = 0L;
        long version = 0L;
        Mk_EventStore instance = null;
        EntityToken expResult = null;
        EntityToken result = instance.getById(entityName, id, version);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    //@Test
    public void testGetEventsById_3args() throws Exception
    {
        System.out.println("getEventsById");
        String entityName = "";
        long id = 0L;
        long fromVer = 0L;
        Mk_EventStore instance = null;
        Queue<Event> expResult = null;
        Queue<Event> result = instance.getEventsById(entityName, id, fromVer);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    //@Test
    public void testGetEventsById_4args() throws Exception
    {
        System.out.println("getEventsById");
        String entityName = "";
        long id = 0L;
        long fromVer = 0L;
        long toVer = 0L;
        Mk_EventStore instance = null;
        Queue<Event> expResult = null;
        Queue<Event> result = instance.getEventsById(entityName, id, fromVer, toVer);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    //@Test
    public void testGetSnapshot_String_long() throws Exception
    {
        System.out.println("getSnapshot");
        String entityName = "";
        long id = 0L;
        Mk_EventStore instance = null;
        Snapshot expResult = null;
        Snapshot result = instance.getSnapshot(entityName, id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    //@Test
    public void testGetSnapshot_3args() throws Exception
    {
        System.out.println("getSnapshot");
        String entityName = "";
        long id = 0L;
        long lteq = 0L;
        Mk_EventStore instance = null;
        Snapshot expResult = null;
        Snapshot result = instance.getSnapshot(entityName, id, lteq);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    //@Test
    public void testSaveSnapshot() throws Exception
    {
        System.out.println("saveSnapshot");
        Snapshot ss = null;
        Mk_EventStore instance = null;
        instance.saveSnapshot(ss);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    //@Test
    public void testSave_4args() throws Exception
    {
        System.out.println("save");
        String entityName = "";
        long id = 0L;
        long loadedVersion = 0L;
        Event[] events = null;
        Mk_EventStore instance = null;
        instance.save(entityName, id, loadedVersion, events);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    //@Test
    public void testSave_EntityToken() throws Exception
    {
        System.out.println("save");
        EntityToken token = null;
        Mk_EventStore instance = null;
        instance.save(token);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    //@Test
    public void testClose() throws Exception
    {
        System.out.println("close");
        Mk_EventStore instance = null;
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
