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
package com.mc1098.mk_eventstore.Event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
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
public class Mk_EventTest
{
    
    public Mk_EventTest()
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
    public void testGetName()
    {
        System.out.println("getName");
        
        String expResult = "testEvent";
        Mk_Event instance = new Mk_Event(expResult, "testEntity", 1, 0, 
                LocalDateTime.now(), new HashMap<>());
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTargetEntityName()
    {
        System.out.println("getTargetEntityName");
        String expResult = "testEntity";
        Mk_Event instance = new Mk_Event("testEvent", expResult, 1, 0, 
                LocalDateTime.now(), new HashMap<>());
        String result = instance.getTargetEntityName();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTargetEntityId()
    {
        System.out.println("getTargetEntityId");
        
        long expResult = 1L;
        Mk_Event instance = new Mk_Event("testEvent", "testEntity", expResult, 0, 
                LocalDateTime.now(), new HashMap<>());
        long result = instance.getTargetEntityId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetVersion()
    {
        System.out.println("getVersion");
        
        long expResult = 0L;
        Mk_Event instance = new Mk_Event("testEvent", "testEntity", 1L, expResult, 
                LocalDateTime.now(), new HashMap<>());
        long result = instance.getVersion();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetOccurred()
    {
        System.out.println("getOccurred");
        
        LocalDateTime before = LocalDateTime.now();
        Mk_Event instance = new Mk_Event("testEvent", "testEntity", 1, 0, 
                LocalDateTime.now(), new HashMap<>());
        LocalDateTime result = instance.getOccurred();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertTrue(before.isBefore(result));
        assertTrue(after.isAfter(result));
    }

    @Test
    public void testGetValue()
    {
        System.out.println("getValue");
        String key = "key";
        String expResult = "value";
        Map map = new HashMap();
        map.put(key, expResult);
        Mk_Event instance = new Mk_Event("testEvent", "testEntity", 1, 0, 
                LocalDateTime.now(), map);
        Object result = instance.getValue(key);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetValue_InvalidKey()
    {
        System.out.println("getValue");
        String key = "key";
        String expResult = "value";
        Map map = new HashMap();
        map.put(key, expResult);
        Mk_Event instance = new Mk_Event("testEvent", "testEntity", 1, 0, 
                LocalDateTime.now(), map);
        Object result = instance.getValue("key2");
        
        assertNull(result);
    }
    
    @Test
    public void testForEach()
    {
        System.out.println("forEach");
        
        Map map = new HashMap();
        map.put("key", "value");
        map.put("key2", "value2");
        
        BiConsumer<String, Serializable> biConsumer = (k, v) ->
        {
            assertEquals(map.get(k), v);
        };
        Mk_Event instance = new Mk_Event("eventName", "entityName", 1, 0, 
                LocalDateTime.now(), map);
        instance.forEach(biConsumer);
    }

    @Test
    public void testToString()
    {
        System.out.println("toString");
        Mk_Event instance = new Mk_Event("testEvent", "testEntity", 1, 0, 
                LocalDateTime.now(), new HashMap<>());
        String expResult = "testEvent[testEntity]";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testEquals()
    {
        System.out.println("equals");
        
        LocalDateTime ldt = LocalDateTime.now();
        
        Event event = new Mk_Event("testEvent", "testEntity", 0, 0, ldt, new HashMap<>());
        Event event2 = new Mk_Event("Event", "testEntity", 0, 0, ldt, new HashMap<>());
        Event event3 = new Mk_Event("testEvent", "entity", 0, 0, ldt, new HashMap<>());
        Event event4 = new Mk_Event("testEvent", "testEntity", 1, 0, ldt, new HashMap<>());
        Event event5 = new Mk_Event("testEvent", "testEntity", 0, 1, ldt, new HashMap<>());
        Event event6 = new Mk_Event("testEvent", "testEntity", 0, 0, ldt.plusDays(1), new HashMap<>());
        Event event7 = new Mk_Event("testEvent", "testEntity", 0, 0, 
                LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("key", "value");}});
        
        assertEquals(event, event); //sanity check
        assertNotEquals(event, event2);
        assertNotEquals(event, event3);
        assertNotEquals(event, event4);
        assertNotEquals(event, event5);
        assertNotEquals(event, event6);
        assertNotEquals(event, event7);
        assertNotEquals(event, new Object());
        
    }
    
}
