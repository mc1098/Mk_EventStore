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
import com.mc1098.mk_eventstore.Entity.Mk_Entity;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Event.EventDeveloper;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Event.EventFormat;
import com.mc1098.mk_eventstore.Event.SimpleEventFormat;
import com.mc1098.mk_eventstore.Page.Mk_PageDirectory;
import com.mc1098.mk_eventstore.Page.PageDirectory;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionPage;
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import com.mc1098.mk_eventstore.Transaction.TransactionParser;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.mc1098.mk_eventstore.Entity.EventHandler;
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
        TransactionPage transactionPage;
        TransactionParser tp = new TransactionParser();
        File file = new File("./Entity/TL");
        
        if(!file.exists())
        {
            file.getParentFile().mkdirs();
            file.createNewFile();
            transactionPage = new Mk_TransactionPage(file, tp);
        }
        else
        {
            ByteBuffer buffer;
            try(FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.READ))
            {
                buffer = ByteBuffer.allocate((int)fc.size());
                while (fc.read(buffer) > 0) {};
            }
            transactionPage = Mk_TransactionPage.parse(file, buffer, tp);
        }
        
        file = new File("./Entity/ENM");
        
        if(!file.exists())
            file.createNewFile();
        
        EventFormat ef = new SimpleEventFormat();
        PageDirectory directory = Mk_PageDirectory.setup(ef, transactionPage);
        
        eventStore =  Mk_EventStore.create(directory, transactionPage);
        
    }
    
    @AfterClass
    public static void tearDownClass() throws IOException
    {
        File file = new File("./Entity/1/1");
        
        for (File f : file.listFiles())
            f.delete();
        
        file = new File("./Entity");
        for (File f : file.listFiles())
            f.delete();
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
         
        EntityRepository<ShoppingCartEntity> repository = new EntityRepository("cart", eventStore);
        EventDeveloper<ShoppingCartEntity> eventDev = new EventDeveloper(repository);
        ShoppingCartEntity cart = new ShoppingCartEntity(1);

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

        ShoppingCartEntity loadedCart1 = repository.getById(1);
        assertEquals(cart, loadedCart1);
        
        ShoppingCartEntity loadedCart2 = repository.getById(1, 40);
        assertEquals(40, loadedCart2.getLoadedVersion());
        
    }
}



class ShoppingCartEntity extends Mk_Entity
{
    
    private final ArrayList<String> items;
    
    public ShoppingCartEntity(long id)
    {
        super(id);
        this.items = new ArrayList<>();
    }
    
    public boolean canAddItem(String item)
    {
        return !(item == null || item.isEmpty());
    }
    
    public boolean canAddItems(String...items)
    {
        for (String item : items)
            if(!canAddItem(item))
                return false;
        return true;
    }
    
    @EventHandler("AddItemEvent")
    public void addItem(Event e)
    {
        items.add(e.getValue("item"));
    }
    
    @EventHandler("AddItemsEvent")
    public void addItems(Event e)
    {
        items.addAll(Arrays.asList(e.getValue("items")));
    }
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof ShoppingCartEntity))
            return false;
        
        ShoppingCartEntity sc = (ShoppingCartEntity) o;
        
        return this.items.equals(sc.items);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.items);
        return hash;
    }
    
    
}
