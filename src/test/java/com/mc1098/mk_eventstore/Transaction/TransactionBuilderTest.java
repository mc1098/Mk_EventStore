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
package com.mc1098.mk_eventstore.Transaction;

import com.mc1098.mk_eventstore.Entity.Mk_Snapshot;
import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Event.Mk_Event;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Util.EventStoreUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class TransactionBuilderTest
{
    
    public TransactionBuilderTest()
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
    public void testBuild_4args_Snapshot()
    {
        System.out.println("build_4args_snapshot");
        long pageId = 0L;
        long entity = 1L;
        long entityId = 1L;
        Snapshot snapshot = new Mk_Snapshot("TestEntity", entityId, 0, new byte[]{10,20});
        TransactionBuilder instance = new TransactionBuilder();
        Transaction expResult = new Transaction(TransactionType.PUT_SNAPSHOT, 
                pageId, entity, entityId, 0, snapshot.getBytes());
        Transaction result = instance.build(pageId, entity, entityId, snapshot);
        
        assertEquals(expResult, result);
    }

    @Test
    public void testBuild_4args_Event() throws Exception
    {
        System.out.println("build_4args_event");
        long pageId = 0L;
        long entity = 1L;
        long entityId = 1L;
        Map map = new HashMap();
        map.put("key", "value");
        Event event = new Mk_Event("TestEvent", "TestEntity", entityId, 1, 
                LocalDateTime.now(), map);
        TransactionBuilder instance = new TransactionBuilder();
        Transaction expResult = new Transaction(TransactionType.PUT_EVENT, 
                pageId, entity, entityId, 1, EventStoreUtils.serialise(event));
        Transaction result = instance.build(pageId, entity, entityId, event);
        assertEquals(expResult, result);
    }

    @Test
    public void testBuild_4args_EventArray() throws Exception
    {
        System.out.println("build_4args_eventArray");
        long pageId = 0L;
        long entity = 1L;
        long entityId = 1L;
        Event[] events = new Event[] 
        {
            new Mk_Event("TestEvent", "TestEntity", entityId, 1, 
                    LocalDateTime.now(), new HashMap<>())
        };
        TransactionBuilder instance = new TransactionBuilder();
        List<Transaction> expResult = new ArrayList<>();
        expResult.add(new Transaction(TransactionType.PUT_EVENT, pageId, entity,
                entityId, 1, EventStoreUtils.serialise(events[0])));
        List<Transaction> result = instance.build(pageId, entity, entityId, events);
        assertEquals(expResult, result);
    }
    
    @Test(expected = EventStoreException.class)
    public void testBuild_4args_EmptyEventArray() throws Exception
    {
        System.out.println("build_4args_EventArray");
        long pageId = 0L;
        long entity = 1L;
        long entityId = 1L;
        Event[] events = new Event[0];
        TransactionBuilder instance = new TransactionBuilder();
        instance.build(pageId, entity, entityId, events);
    }
    
}
