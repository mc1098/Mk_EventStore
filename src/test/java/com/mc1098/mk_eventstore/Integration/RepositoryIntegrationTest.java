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
package com.mc1098.mk_eventstore.Integration;

import com.mc1098.mk_eventstore.Entity.EntityRepository;
import com.mc1098.mk_eventstore.Event.EventDeveloper;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.mc1098.mk_eventstore.EventStore.EventStore;
import com.mc1098.mk_eventstore.EventStore.Mk_EventStore;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class RepositoryIntegrationTest
{
    
    private static EventStore eventStore;
    
    public RepositoryIntegrationTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException, EventStoreException
    {
        eventStore =  Mk_EventStore.create();
    }
    
    @AfterClass
    public static void tearDownClass() throws IOException, Exception
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
    
    
    
    
    @Test
    public void repositoryTest() throws EventStoreException 
    {
        System.out.println("repositoryTest");
         
        EntityRepository<ShoppingCartEntityExample> repository = new EntityRepository("cart", eventStore);
        EventDeveloper<ShoppingCartEntityExample> eventDev = new EventDeveloper(repository);
        ShoppingCartEntityExample cart = new ShoppingCartEntityExample(1);

        for (int i = 0; i < 100; i++)
        {
            String item = String.format("item%d", i);
            if(cart.canAddItem(item))
            {
                eventDev.put("item", item);
                eventDev.build("AddItemEvent", cart);
            }
            repository.save(cart);
        }

        ShoppingCartEntityExample loadedCart1 = repository.getById(1);
        assertEquals(cart, loadedCart1);
        
        ShoppingCartEntityExample loadedCart2 = repository.getById(1, 40);
        assertEquals(40, loadedCart2.getLoadedVersion());
        
    }
}



