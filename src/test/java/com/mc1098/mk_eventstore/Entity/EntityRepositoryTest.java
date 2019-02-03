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
import com.mc1098.mk_eventstore.EventStore.EventStore;
import com.mc1098.mk_eventstore.Exception.EntityChronologicalException;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import com.mc1098.mk_eventstore.Exception.ThreadingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * EntityRepository tests are to check the implementation of the {@link Repository}
 * interface. The tests when required use a dummy EventStore as seen below, as 
 * the tests are confirming the implementation of the repository and what it supplies
 * to the EventStore. 
 * GetBy Methods are not included in these tests as they simple call a respective
 * method in the EventStore, however the 
 * {@link EntityRepository#tokenToEntity(com.mc1098.mk_eventstore.Entity.EntityToken)}
 * method is tested.
 * 
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class EntityRepositoryTest
{
    
    public EntityRepositoryTest()
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

    @Test(expected = EventStoreException.class)
    public void testTokenToEntityEmptySnapshotArray() throws Exception
    {
        System.out.println("TokenToEntityEmptySnapshotArray");
        
        Event[] events = new Event[] {new Mk_Event("event", "testEntity", 1, 0, 
                LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 20);}})};
        
        EntityToken token = new EntityToken(new Snapshot[0], events);
        EntityRepository instance = new EntityRepository(null, null);
        instance.tokenToEntity(token);
        fail("Expected EventStoreException to be thrown as a result of no snapshot"
                + "being in the EntityToken.");
    }
    
    @Test(expected = SerializationException.class)
    public void testTokenToEntityCorruptedSnapshotData() throws Exception
    {
        System.out.println("TokenToEntityCorruptedSnapshotData");
        
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, new byte[]{20, 30 , 40});
        
        Event[] events = new Event[] {new Mk_Event("event", "testEntity", 1, 0, 
                LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 20);}})};
        
        EntityToken token = new EntityToken(snapshot, events);
        EntityRepository instance = new EntityRepository(null, null);
        instance.tokenToEntity(token);
        fail("Expected SerializationException to be thrown as a result of a "
                + "corrupt snapshot being in the EntityToken.");
    }
    
    public void testTokenToEntityEmptyEventsArray() throws Exception
    {
        System.out.println("TokenToEntityEmptyEventsArray");
        
        Entity entity = new TestEntity(1);
        byte[] entityBytes;
        
        entityBytes = serialise(entity);
        
        Snapshot ss = new Mk_Snapshot("testEntity", 1, 0, entityBytes);
        EntityToken token = new EntityToken(ss, new Event[0]);
        EntityRepository instance = new EntityRepository(null, null);
        Entity entity1 = instance.tokenToEntity(token);
        
        assertEquals(entity, entity1);
        
    }

    private byte[] serialise(Entity entity) throws IOException
    {
        byte[] entityBytes;
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            oos.writeObject(entity);
            oos.flush();
            entityBytes = baos.toByteArray();
        }
        return entityBytes;
    }
    
    @Test
    public void testSaveNoSnapshotOrEvents() throws Exception
    {
        System.out.println("saveNoSnapshotOrEvents");
        TestEntity entity = new TestEntity(1);
        DummyEventStore des = new DummyEventStore();
        EntityRepository<TestEntity> instance = new EntityRepository("testEntity", des);
        instance.save(entity);
        Snapshot expResult = new Mk_Snapshot("testEntity", 1, 0, serialise(entity));
        
        assertNull(des.events);
        assertEquals(expResult, des.snapshot);
        
    }
    
    @Test
    public void testSaveWithRepoSnapshotPrior() throws Exception
    {
        System.out.println("saveWithRepoSnapshotPrior");
        TestEntity entity = new TestEntity(1);
        DummyEventStore des = new DummyEventStore();
        EntityRepository<TestEntity> instance = new EntityRepository("testEntity", des);
        instance.takeSnapshot(entity);
        instance.save(entity);
        Snapshot expResult = new Mk_Snapshot("testEntity", 1, 0, serialise(entity));
        
        assertNull(des.events);
        assertEquals(expResult, des.snapshot);
    }
    
    @Test
    public void testSaveWithMultipleRepoSnapshotPrior() throws Exception
    {
        System.out.println("saveWithMultiplRepoSnapshotPrior");
        TestEntity entity = new TestEntity(1);
        DummyEventStore des = new DummyEventStore();
        EntityRepository<TestEntity> instance = new EntityRepository("testEntity", des);
        Snapshot snapshot1 = new Mk_Snapshot("testEntity", 1, 0, serialise(entity));
        instance.takeSnapshot(entity);
        
        
        for (int i = 0; i < 10; i++) //load entity with events to reach next version for snapshot.
        {
            Map map = new HashMap();
            map.put("value", i);
            Event e = new Mk_Event("valueEvent", "testEntity", 1, i, 
                    LocalDateTime.now(), map);
            entity.applyEvent(e);
        }
        
        instance.takeSnapshot(entity);
        Snapshot snapshot2 = new Mk_Snapshot("testEntity", 1, 10, serialise(entity));
        instance.save(entity);
        
        Snapshot[] expResult = new Snapshot[]{snapshot1, snapshot2};
        
        assertNull(des.events);
        assertNull(des.snapshot);
        assertArrayEquals(expResult, des.token.getSnapshots());
    }
    
    @Test (expected = EventStoreException.class)
    public void testTakeSnapshotWithSameEntityState_Exception() throws EventStoreException
    {
        System.out.println("takeSnapshotWithSameEntityState_Exception");
        
        TestEntity entity = new TestEntity(1);
        DummyEventStore des = new DummyEventStore();
        EntityRepository<TestEntity> instance = new EntityRepository("testEntity", des);
        
        instance.takeSnapshot(entity);
        instance.takeSnapshot(entity); //exception as no snapshot required.
    }
    
    
    
    @Test
    public void testSaveEventsOnly() throws Exception
    {
        System.out.println("SaveEventsOnly");
        
        String entityName = "testEntity";
        long entityId = 1;
        long entityVersion = 0;
        
        TestEntity entity = new TestEntity(entityId);
        DummyEventStore des = new DummyEventStore();
        EntityRepository<TestEntity> instance = new EntityRepository(entityName, des);
        instance.save(entity); //save once for saveSnapshot
        des.snapshot = null; // remove saveSnapshot before saving entity again.
        
        Event[] events = new Event[]{new Mk_Event("valueEvent", entityName, 
                entityId, entityVersion, LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 20);}})};

        entity.applyEvent(events[0]);
        instance.save(entity);
        
        assertNull(des.snapshot);
        assertEquals(entityName, des.entityNameUsed);
        assertEquals(entityId, des.entityIdUsed);
        assertEquals(entityVersion, des.versionUsed);
        assertArrayEquals(events, des.events);
    }
    
    @Test
    public void testSaveSnapshotAndEvents() throws Exception
    {
        System.out.println("saveSnapshotAndEvents");
        
        String entityName = "testEntity";
        long entityId = 1;
        long entityVersion = 0;
        
        TestEntity entity = new TestEntity(entityId);
        DummyEventStore des = new DummyEventStore();
        EntityRepository<TestEntity> instance = new EntityRepository(entityName, des);
        
        //Instruct repository to take saveSnapshot as we are apply the event directly
        //to the entity. If building the event with the EventDeveloper this 
        //would be taken care of while building the event.
        instance.takeSnapshot(entity);
        
        Snapshot snapshot = new Mk_Snapshot("testEntity", 1, 0, serialise(entity));
        
        Event[] events = new Event[]{new Mk_Event("valueEvent", entityName, 
                entityId, entityVersion, LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 20);}})};
        
        EntityToken expResult = new EntityToken(snapshot, events);

        entity.applyEvent(events[0]);
        instance.save(entity);
        
        assertEquals(expResult, des.token);
    }
    
    
}

class DummyEventStore implements EventStore
{
    public String entityNameUsed;
    public long entityIdUsed;
    public long versionUsed;
    
    public Snapshot snapshot;
    public Event[] events;
    public EntityToken token;
    

    @Override
    public int getERP(String entityName) {return 10;}

    @Override
    public EntityToken getById(String entityName, long id) 
            throws EventStoreException {throw new UnsupportedOperationException("Not supported yet.");}

    @Override
    public EntityToken getById(String entityName, long id, long version) 
            throws EventStoreException {throw new UnsupportedOperationException("Not supported yet.");} 

    @Override
    public Queue<Event> getEventsById(String entityName, long id, long fromVer) 
            throws EventStoreException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Queue<Event> getEventsById(String entityName, long id, long fromVer, long toVer) throws EventStoreException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Snapshot getSnapshot(String entityName, long id) throws EventStoreException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Snapshot getSnapshot(String entityName, long id, long lteq) throws EventStoreException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveSnapshot(Snapshot ss) throws EntityChronologicalException, EventStoreException
    {
        this.snapshot = ss;
    }

    @Override
    public void save(String entityName, long id, long loadedVersion, Event[] events) throws EntityChronologicalException, SerializationException, ThreadingException, EventStoreException
    {
        this.entityNameUsed = entityName;
        this.entityIdUsed = id;
        this.versionUsed = loadedVersion;
        this.events = events;
    }

    @Override
    public void save(EntityToken token) throws EntityChronologicalException, SerializationException, EventStoreException
    {
        this.token = token;
    }

    @Override
    public void close() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}


class TestEntity extends Mk_Entity
{
    
    private int value;
    
    public TestEntity(long id)
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
        if(o == null || !(o instanceof TestEntity))
            return false;
        
        return this.value == ((TestEntity)o).value;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 53 * hash + this.value;
        return hash;
    }
    
    
}