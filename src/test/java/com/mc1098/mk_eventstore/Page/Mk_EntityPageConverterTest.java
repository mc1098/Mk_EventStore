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
package com.mc1098.mk_eventstore.Page;

import com.mc1098.mk_eventstore.Entity.Mk_Snapshot;
import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Event.Mk_Event;
import com.mc1098.mk_eventstore.Event.SimpleEventConverter;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
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
public class Mk_EntityPageConverterTest
{
    
    public Mk_EntityPageConverterTest()
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
    public void testToBytesAndParse() throws Exception
    {
        System.out.println("toBytesAndParse");
        
        Queue<Event> events = new ArrayDeque<>();
        events.add(new Mk_Event("TestEvent", "TestEntity", 1, 0, 
                LocalDateTime.now(), new HashMap<>()));
        EntityPage page = new Mk_EntityPage(0, 0, 1, 1, 20, 
                new Mk_Snapshot("TestEntity", 1, 0, new byte[]{10,20,55}), events);
        
        Mk_EntityPageConverter instance = new Mk_EntityPageConverter(new DummyDirectory(), 
                new SimpleEventConverter());
        
        byte[] bytes = instance.toBytes(page);
        EntityPage result = instance.parse(ByteBuffer.wrap(bytes));
        
        assertEquals(page, result);
        
    }
    
    class DummyDirectory implements PageDirectory
    {

        @Override
        public String getEntityName(long entity) {return "TestEntity";}

        @Override
        public long getEntity(String entityName) {return 0L;}

        @Override
        public boolean hasEntity(String entityName)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getEPR(long entity) {return 20;}

        @Override
        public boolean doesPageExist(long entity, long entityId, long pageNo)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EntityPage getEntityPage(long entity, long id) throws EventStoreException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EntityPage getEntityPage(long entity, long id, long pageNo) throws EventStoreException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<EntityPage> getEntityPages(long entity, long id, long pageFrom) throws EventStoreException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<EntityPage> getEntityPages(long entity, long id, long pageNo, long pageNo1) throws EventStoreException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EntityPage createPendingEntityPage(long entity, long id, long pageNo, Snapshot snapshot)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EntityPage confirmPendingPage(EntityPage page) throws EventStoreException
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public TransactionPage getTransactionPage()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EntityPageConverter getEntityPageConverter()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        
    }
    
}
