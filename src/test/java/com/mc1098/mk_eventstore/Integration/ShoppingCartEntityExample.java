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

import com.mc1098.mk_eventstore.Entity.EventHandler;
import com.mc1098.mk_eventstore.Entity.Mk_Entity;
import com.mc1098.mk_eventstore.Event.Event;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class ShoppingCartEntityExample extends Mk_Entity
{
    private final ArrayList<String> items;
    
    public ShoppingCartEntityExample(long id)
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
        if(o == null || !(o instanceof ShoppingCartEntityExample))
            return false;
        
        ShoppingCartEntityExample sc = (ShoppingCartEntityExample) o;
        
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
