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
import java.io.Serializable;
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
public class Mk_EntityTest
{
    
    public Mk_EntityTest()
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



    /**
     * Test of applyEvent method, of class Mk_Entity.
     */
    @Test
    public void testApplyEvent_Event()
    {
        System.out.println("applyEvent");
        
        Event e = new Mk_Event("valueEvent", "testEvent", 1, 0, 
                LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 20);}});
        Mk_EntityImpl instance = new Mk_EntityImpl(1);
        instance.applyEvent(e);
        
        //fresh events should not increment loaded version.
        assertEquals(0, instance.getLoadedVersion());
        assertEquals(1, instance.getNewEvents().length);
        assertEquals(e, instance.getNewEvents()[0]);
        assertEquals(20, instance.value);
        
    }

    /**
     * Test of applyEvent method, of class Mk_Entity.
     */
    //@Test
    public void testApplyEvent_Event_boolean()
    {
        System.out.println("applyEvent");
        
        Event e1 = new Mk_Event("valueEvent", "testEvent", 1, 0, 
                LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 30);}});
        
        Event e2 = new Mk_Event("valueEvent", "testEvent", 1, 1, 
                LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 20);}});
        
        
        Mk_EntityImpl instance = new Mk_EntityImpl(1);
        
        instance.applyEvent(e1, false);
        
        assertEquals(1, instance.getLoadedVersion());
        assertEquals(0, instance.getNewEvents().length);
        assertEquals(30, instance.value);
        
        instance.applyEvent(e2, true);
        
        assertEquals(1, instance.getLoadedVersion());
        assertEquals(1, instance.getNewEvents().length);
        assertEquals(e1, instance.getNewEvents()[0]);
        assertEquals(20, instance.value);
    }

    /**
     * Test of loadHistoricEvents method, of class Mk_Entity.
     */
    @Test
    public void testLoadHistoricEvents()
    {
        System.out.println("loadHistoricEvents");
        Event[] events = {new Mk_Event("valueEvent", "testEvent", 1, 0, 
                LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 20);}})};
        Mk_EntityImpl instance = new Mk_EntityImpl(1);
        instance.loadHistoricEvents(events);
        
        assertEquals(1, instance.getLoadedVersion());
        assertEquals(0, instance.getNewEvents().length);
        assertEquals(20, instance.value);
    }

    /**
     * Test of refresh method, of class Mk_Entity.
     */
    @Test
    public void testRefresh()
    {
        System.out.println("refresh");
        
        Event e = new Mk_Event("valueEvent", "testEvent", 1, 0, 
                LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 20);}});
        Mk_EntityImpl instance = new Mk_EntityImpl(1);
        instance.applyEvent(e);
        
        //fresh events should not increment loaded version.
        assertEquals(0, instance.getLoadedVersion());
        assertEquals(1, instance.getNewEvents().length);
        assertEquals(e, instance.getNewEvents()[0]);
        assertEquals(20, instance.value);
        
        instance.refresh();
        
        assertEquals(1, instance.getLoadedVersion());
        assertEquals(0, instance.getNewEvents().length);
        assertEquals(20, instance.value);
        
    }

    public class Mk_EntityImpl extends Mk_Entity
    {

        public int value;
    
        public Mk_EntityImpl(long id)
        {
            super(id);
            this.value = 0;
        }

        public int getValue() {return value;}

        @EventHandler("valueEvent")
        public void applyValue(Event e)
        {
            value = e.getValue("value");
        }

        @Override
        public boolean equals(Object o)
        {
            if(o == null || !(o instanceof Mk_EntityImpl))
                return false;

            return this.value == ((Mk_EntityImpl)o).value;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 53 * hash + this.value;
            return hash;
        }
    }
    
}
