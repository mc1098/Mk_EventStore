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
import com.mc1098.mk_eventstore.Exception.EventStoreError;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * This abstract class includes all the required implementations for the 
 * {@link Entity} interface and is the recommended implementation to use. 
 * 
 * This abstract class does require a single variable constructor for the 
 * unique identification for this Entity type. The {@link EventStore} currently
 * does not generate ids so it is up to the users of this class to create and 
 * track id creations.
 * 
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public abstract class Mk_Entity implements Entity
{
    private final long id;
    private long loadedVersion;
    private LocalDateTime lastEventStamp;
    private transient Queue<Event> events;
    
    public Mk_Entity(long id)
    {
        this.id = id;
        this.lastEventStamp = LocalDateTime.now();
        this.events = new ArrayDeque<>();
    }

    @Override
    public long getId() {return this.id;}

    @Override
    public long getLoadedVersion() {return this.loadedVersion;}

    @Override
    public LocalDateTime getLastEventStamp() {return this.lastEventStamp;}

    @Override
    public void applyEvent(Event e) {applyEvent(e, true);}
    
    protected void applyEvent(Event e, boolean add)
    {
        Method m = null;
        for (Method dm : getClass().getDeclaredMethods())
        {
            if(dm.isAnnotationPresent(EventHandler.class) && 
                    dm.getAnnotation(EventHandler.class).value().equals(e.getName()))
                m = dm;
        }
        
        if(m == null)
            throw new EventStoreError(String.format("Entity %s, has no "
                    + "method that handles the Event %s", 
                    getClass().getSimpleName(), e.getName()));
        
        m.setAccessible(true);
        try
        {
            m.invoke(this, e);
            lastEventStamp = e.getOccurred();
        } catch (Exception ex)
        {
            throw new EventStoreError(ex);
        }
        
        if(add)
        {
            if(events == null)
                events = new ArrayDeque<>();
            events.add(e);
        }
    }

    @Override
    public Event[] getNewEvents() 
    {
        if(events == null)
            events = new ArrayDeque<>();
        return events.toArray(new Event[events.size()]);
    }

    @Override
    public void loadHistoricEvents(Event[] events)
    {
        for (Event e : events)
        {
            if(e.getVersion() == loadedVersion)
                applyEvent(e, false);
            else 
                throw new EventStoreError(String.format("Chronological error "
                        + "when loading historical events to entity. Entity "
                        + "\'%s\' was at version %d when attempting to load "
                        + "Event \'%s\' which has a version of %d", 
                        this.toString(), loadedVersion, e.getName(), 
                        e.getVersion()));
            loadedVersion+=1;
        }
    }
    
    @Override
    public void refresh()
    {
        if(events == null)
            events = new ArrayDeque<>();
        loadedVersion+= events.size();
        events.clear();
    }
    
}
