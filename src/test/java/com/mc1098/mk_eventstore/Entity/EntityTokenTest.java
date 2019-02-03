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
package com.mc1098.mk_eventstore.Entity;

import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Event.Mk_Event;
import java.time.LocalDateTime;
import java.util.HashMap;
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
public class EntityTokenTest
{
    
    public EntityTokenTest()
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
    public void testGetSnapshots()
    {
        System.out.println("getSnapshots");
        
        Snapshot[] expResult = new Snapshot[]
        {
            new Mk_Snapshot("TestEntity", 1, 0, new byte[]{10})
        };
        EntityToken instance = new EntityToken(expResult, null);
        
        Snapshot[] result = instance.getSnapshots();
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testGetEvents()
    {
        System.out.println("getEvents");
        
        Event[] expResult = new Event[] 
        {
            new Mk_Event("TestEvent", "TestEntity", 1, 0, LocalDateTime.now(), 
                    new HashMap<>())
        };
        EntityToken instance = new EntityToken((Snapshot)null, expResult);
        
        Event[] result = instance.getEvents();
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testEquals()
    {
        System.out.println("equals");
        
        Snapshot snapshot = new Mk_Snapshot("TestEntity", 1, 0, new byte[]{33});
        Event e = new Mk_Event("TestEvent", "TestEntity", 1, 0, 
                LocalDateTime.now(), new HashMap<>());
        EntityToken token = new EntityToken(snapshot, new Event[0]);
        EntityToken tokenCopy = new EntityToken(new Snapshot[]{snapshot}, new Event[0]);
        EntityToken token2 = new EntityToken(snapshot, new Event[]{e});
        EntityToken token3 = new EntityToken(new Snapshot[0], new Event[]{e});
        
        assertEquals(token, token);//sanity check
        assertEquals("Expect these two tokens to be equal even when initialised "
                + "with different constructors.",token, tokenCopy);
        assertNotEquals(token, token2);
        assertNotEquals(token, token3);
        assertNotEquals(token, new Object());
    }

    
}
