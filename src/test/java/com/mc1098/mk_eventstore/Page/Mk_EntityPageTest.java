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
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Event.Mk_Event;
import com.mc1098.mk_eventstore.Exception.AlreadyPendingChange;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
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
 * @author Max
 */
public class Mk_EntityPageTest
{
    
    public Mk_EntityPageTest()
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
    public void testGetPageId()
    {
        System.out.println("getPageId");
        
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        long expResult = 0L;
        long result = instance.getPageId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetEntity()
    {
        System.out.println("getEntity");
        
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        long expResult = 0L;
        long result = instance.getEntity();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetEntityId()
    {
        System.out.println("getEntityId");
        
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        long expResult = 1L;
        long result = instance.getEntityId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetVersion()
    {
        System.out.println("getVersion");
        
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        long expResult = 0L;
        long result = instance.getVersion();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetCleanVersion()
    {
        System.out.println("getCleanVersion");
        
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        long expResult = -1L;
        long result = instance.getCleanVersion();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testSetCleanVersion()
    {
        System.out.println("setCleanVersion");
        long version = 0L;
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        assertEquals(-1, instance.getCleanVersion());
        instance.setCleanVersion(version);
        assertEquals(0, instance.getCleanVersion());
        
    }

    @Test
    public void testGetSnapshot()
    {
        System.out.println("getSnapshot");
        
        Snapshot expResult = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, expResult);
        Snapshot result = instance.getSnapshot();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetEvents()
    {
        System.out.println("getEvents");
        
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Queue<Event> events = new ArrayDeque<>();
        events.add(new Mk_Event("testEvent", "testEntity", 1, 0, 
                LocalDateTime.now(), new HashMap<>()));
        EntityPage page = new Mk_EntityPage(0, 0, 1, 0, 10, snapshot, events);
        
        Object[] expResult = events.toArray();
        Event[] result = page.getEvents();
        
        assertArrayEquals(expResult, result);
        
    }
    

    @Test
    public void testGetEvents_NoEvents()
    {
        System.out.println("getEvents_NoEvents");
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        
        Event[] expResult = new Event[0];
        Event[] result = instance.getEvents();
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testGetEventPageRatio()
    {
        System.out.println("getEventPageRatio");
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        int expResult = 10;
        int result = instance.getEventPageRatio();
        assertEquals(expResult, result);
    }

    @Test
    public void testEvents()
    {
        System.out.println("events");
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        int expResult = 0;
        int result = instance.events();
        assertEquals(expResult, result);
    }

    @Test
    public void testAddAndConfirmPending() throws Exception
    {
        System.out.println("addAndConfirmPending");
        Event[] events = new Event[]{new Mk_Event("testEvent", "testEntity", 1, 
                0, LocalDateTime.now(), new HashMap<>())};
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        
        instance.addToPending(events);
        instance.confirmEvents(events);
        assertArrayEquals(events, instance.getEvents());
    }
    
    @Test (expected = AlreadyPendingChange.class)
    public void testAddToPendingMoreThanOnceBeforeConfirming() throws Exception
    {
        System.out.println("addToPendingMoreThanOnceBeforeConfirming");
        Event[] events = new Event[]{new Mk_Event("testEvent", "testEntity", 1, 
                0, LocalDateTime.now(), new HashMap<>())};
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        
        instance.addToPending(events);
        instance.addToPending(events);
    }

    @Test
    public void testConfirmEventsWithNonePending()
    {
        System.out.println("confirmEventsWithNonePending");
        Event[] events = null;
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        instance.confirmEvents(events);
        assertEquals(0, instance.events());
    }
    
    @Test
    public void testConfirmEventsWithNonValidEvents() throws Exception
    {
        System.out.println("confirmEventsWithNonValidEvents");
        
        Event[] events = new Event[]{new Mk_Event("testEvent", "testEntity", 1, 
                0, LocalDateTime.now(), new HashMap<>())};
        Event[] events2 = new Event[]{new Mk_Event("testEvent", "testEntity", 1, 
                1, LocalDateTime.now(), new HashMap<>())};
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Mk_EntityPage instance = new Mk_EntityPage(0, 0, 1, 10, snapshot);
        
        instance.addToPending(events);
        instance.confirmEvents(events2);
        assertEquals(0, instance.events());
    }

    @Test
    public void testEquals()
    {
        System.out.println("equals");
        
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{10});
        Snapshot snapshot2 = new Mk_Snapshot("testEntity", 1, 1, new byte[]{20});
        Queue<Event> events = new ArrayDeque<>();
        events.add(new Mk_Event("testEvent", "testEntity", 1, 0, LocalDateTime.now(), new HashMap<>()));
        EntityPage page = new Mk_EntityPage(0, 0, 1, 0, 10, snapshot, new ArrayDeque<>());
        EntityPage page2 = new Mk_EntityPage(1, 0, 1, 0, 10, snapshot, new ArrayDeque<>());
        EntityPage page3 = new Mk_EntityPage(0, 1, 1, 0, 10, snapshot, new ArrayDeque<>());
        EntityPage page4 = new Mk_EntityPage(0, 0, 2, 0, 10, snapshot, new ArrayDeque<>());
        EntityPage page5 = new Mk_EntityPage(0, 0, 1, 1, 10, snapshot, new ArrayDeque<>());
        EntityPage page6 = new Mk_EntityPage(0, 0, 1, 0, 20, snapshot2, new ArrayDeque<>());
        EntityPage page7 = new Mk_EntityPage(0, 0, 1, 0, 10, snapshot, events);
        
        assertEquals(page, page); //sanity check
        assertNotEquals(page, page2);
        assertNotEquals(page, page3);
        assertNotEquals(page, page4);
        assertNotEquals(page, page5);
        assertNotEquals(page, page6);
        assertNotEquals(page, page7);
        assertNotEquals(page, new Object());
        
    }
    
}
