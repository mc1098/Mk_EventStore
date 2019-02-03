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
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Event.EventFormat;
import com.mc1098.mk_eventstore.Event.Mk_Event;
import com.mc1098.mk_eventstore.Event.SimpleEventFormat;
import com.mc1098.mk_eventstore.Page.Mk_PageDirectory;
import com.mc1098.mk_eventstore.Page.PageDirectory;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionPage;
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import com.mc1098.mk_eventstore.Transaction.TransactionParser;
import com.mc1098.mk_eventstore.Util.EventStoreUtils;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    private static EventStore eventStore;
    
    public Mk_EventStoreTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass() throws EventStoreException, IOException
    {
        eventStore =  Mk_EventStore.create();
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception
    {
        eventStore.close();
        File file = new File("Entity/1/1");
        
        for (File f : file.listFiles())
            f.delete();
        
        do{
            file = file.getParentFile();
            for (File f : file.listFiles())
                f.delete();
        } while(!file.getName().equals("Entity"));
        file.delete();
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

//    /**
//     * Test of getById method, of class Mk_EventStore.
//     */
//    @Test
//    public void testGetById_String_long() throws Exception
//    {
//        System.out.println("getById");
//        String entityName = "TestEntity";
//        long id = 1L;
//        
//        Snapshot ss = eventStore.getSnapshot(entityName, id, 0L);
//        Queue<Event> after = eventStore.getEventsById(entityName, id, 0);
//        
//        EntityToken result = eventStore.getById(entityName, id);
//        
//        assertArrayEquals(new Snapshot[]{ss}, result.getSnapshots());
//        Event[] afters = after.toArray(new Event[after.size()]);
//        for (int i = 0; i < result.getEvents().length; i++)
//            assertTrue(EventStoreUtils.isEqual(afters[i], result.getEvents()[i]));
//        
//    }

//    /**
//     * Test of getById method, of class Mk_EventStore.
//     */
//    @Test
//    public void testGetById_3args() throws Exception
//    {
//        System.out.println("getById");
//        String entityName = "";
//        long id = 0L;
//        long version = 0L;
//        Mk_EventStore instance = null;
//        EntityToken expResult = null;
//        EntityToken result = instance.getById(entityName, id, version);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEventsById method, of class Mk_EventStore.
////     */
//    @Test
//    public void testGetEventsById_3args() throws Exception
//    {
//        System.out.println("getEventsById");
//        String entityName = "TestEntity";
//        long id = 1L;
//        long fromVer = 0L;
//        
//        Event e = new Mk_Event("TestEvent", entityName, 1L, 0L, 
//                LocalDateTime.of(2019, 1, 24, 3, 56, 56, 592000000), 
//                new HashMap<String, Serializable>()
//                {
//                    {put("Key", "Value");}
//                });
//        Queue<Event> result = eventStore.getEventsById(entityName, id, fromVer);
//        
//        Event resultEvent = result.element();
//        
//        assertEquals(e.getName(), resultEvent.getName());
//        //assertTrue(e.getOccurred().isEqual(resultEvent.getOccurred()));
//        assertEquals(e.getTargetEntityName(), resultEvent.getTargetEntityName());
//        assertEquals(e.getTargetEntityId(), resultEvent.getTargetEntityId());
//        assertEquals((String)e.getValue("Key"), (String) resultEvent.getValue("Key"));
//    }
//
//    /**
//     * Test of getEventsById method, of class Mk_EventStore.
//     */
//    @Test
//    public void testGetEventsById_4args() throws Exception
//    {
//        System.out.println("getEventsById");
//        String entityName = "";
//        long id = 0L;
//        long fromVer = 0L;
//        long toVer = 0L;
//        Mk_EventStore instance = null;
//        Queue<Event> expResult = null;
//        Queue<Event> result = instance.getEventsById(entityName, id, fromVer, toVer);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSnapshot method, of class Mk_EventStore.
//     */
//    @Test
//    public void testGetSnapshot_String_long() throws Exception
//    {
//        System.out.println("getSnapshot");
//        String entityName = "";
//        long id = 0L;
//        Mk_EventStore instance = null;
//        Snapshot expResult = null;
//        Snapshot result = instance.getSnapshot(entityName, id);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSnapshot method, of class Mk_EventStore.
////     */
//    @Test
//    public void testGetSnapshot_3args() throws Exception
//    {
//        System.out.println("getSnapshot");
//        String entityName = "TestEntity";
//        long id = 1L;
//        long lteq = 0L;
//        Snapshot expResult = null;
//        Snapshot result = eventStore.getSnapshot(entityName, id, lteq);
//        assertEquals(expResult, result);
//    }

//    /**
//     * Test of saveSnapshot method, of class Mk_EventStore.
//     */
//    @Test
//    public void testSnapshot() throws Exception
//    {
//        System.out.println("saveSnapshot");
//        Snapshot ss = new Mk_Snapshot("TestEntity", 1, 0, new byte[]{20,30,40});
//        eventStore.saveSnapshot(ss);
//        
//        System.out.println("hh");
//    }

//    /**
//     * Test of save method, of class Mk_EventStore.
//     */
//    @Test
//    public void testSave_4args() throws Exception
//    {
//        System.out.println("save");
//        String entityName = "TestEntity";
//        long id = 1L;
//        long loadedVersion = 0L;
//        Event[] events = new Event[]{new Mk_Event("TestEvent", entityName, 
//                id, 0L, LocalDateTime.now(), 
//                new HashMap<String, Serializable>(){
//                    {put("Key", "Value");}
//                } )};
//        eventStore.save(entityName, id, loadedVersion, events);
//        
//        System.out.println("hh");
//    }
//
    /**
     * Test of save method, of class Mk_EventStore.
     */
    @Test
    public void testSave_EntityToken() throws Exception
    {
        System.out.println("save");
        
        String entityName = "TestEntity";
        long entityId = 1L;
        
        Snapshot snapshot = new Mk_Snapshot(entityName, entityId, 0L, new byte[]{20,30,40});
        Event[] events = new Event[]{new Mk_Event("TestEvent", entityName, 
                entityId, 0L, LocalDateTime.now(), 
                new HashMap<String, Serializable>(){
                    {put("Key", "Value");}
                } )};
        EntityToken token = new EntityToken(snapshot, events);
        eventStore.save(token);
    }
    
    
    
    
}
