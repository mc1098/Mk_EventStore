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
package com.mc1098.mk_eventstore.Event;

import com.mc1098.mk_eventstore.Entity.Entity;
import com.mc1098.mk_eventstore.Entity.Repository;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
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
public class EventDeveloperTest
{
    
    public EventDeveloperTest()
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
    public void testBuild() throws EventStoreException
    {
        System.out.println("build");
        String name = "testEvent";
        Entity_Impl entity = new Entity_Impl();
        DummyRepository repository = new DummyRepository();
        Event expResult = new Mk_Event(name, "testEntity", 1, 0, 
                LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 20);}});
        
        EventDeveloper instance = new EventDeveloper(repository);
        instance.put("value", 20);
        Event result = instance.build(name, entity);
        
        assertTrue(repository.wasSnapshotRequested);
        assertEquals(expResult.getName(), result.getName());
        assertEquals(expResult.getTargetEntityName(), result.getTargetEntityName());
        assertEquals(expResult.getTargetEntityId(), result.getTargetEntityId());
        assertEquals((int)expResult.getValue("value"), (int)result.getValue("value"));
    }
    
    @Test (expected = EventStoreException.class)
    public void testBuild_RepoThrowsException() throws EventStoreException
    {
        System.out.println("build");
        String name = "testEvent";
        Entity_Impl entity = new Entity_Impl();
        DummyRepository repository = new DummyRepository(true);
        Event expResult = new Mk_Event(name, "testEntity", 1, 0, 
                LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 20);}});
        
        EventDeveloper instance = new EventDeveloper(repository);
        instance.put("value", 20);
        instance.build(name, entity);
        
    }
    
    
    class DummyRepository implements Repository<Entity_Impl>
    {
        
        private final boolean throwException;
        public boolean wasSnapshotRequested;
        
        public DummyRepository()
        {
            this.throwException = false;
        }
        
        public DummyRepository(boolean throwException)
        {
            this.throwException = throwException;
        }

        @Override
        public String getEntityName() {return "testEntity";}

        @Override
        public int getERP()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Entity_Impl getById(long id) throws EventStoreException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Entity_Impl getById(long id, long version) throws EventStoreException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void save(Entity_Impl t) throws EventStoreException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void takeSnapshot(Entity_Impl t) throws EventStoreException
        {
            if(throwException)
                throw new EventStoreException("Dummy Repository exception to "
                        + "test exception handling.");
            this.wasSnapshotRequested = true;
        }
        
    }
    
    
    class Entity_Impl implements Entity
    {
        
        public Event e;
        
        @Override
        public long getId() {return 1;}

        @Override
        public long getLoadedVersion() {return 0;}

        @Override
        public LocalDateTime getLastEventStamp()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void applyEvent(Event e) {this.e = e;}

        @Override
        public Event[] getNewEvents() {return new Event[0];}

        @Override
        public void loadHistoricEvents(Event[] events)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void refresh() 
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    
    }
    
}

