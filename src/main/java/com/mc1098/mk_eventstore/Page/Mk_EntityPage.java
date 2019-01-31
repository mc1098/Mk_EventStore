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

import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Exception.AlreadyPendingChange;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_EntityPage implements EntityPage
{
    private final long pageId;
    private final long entity;
    private final long entityId;
    private long version;
    private long cleanVersion;
    private final int eventPageRatio;
    private final Snapshot snapshot;
    private Event[] pending = null;
    private final Queue<Event> events;
    
    
    public Mk_EntityPage(long pageId, long entity, long entityId, long version, 
            int eventPageRatio, Snapshot snapshot, Queue<Event> events)
    {
        this.pageId = pageId;
        this.entity = entity;
        this.entityId = entityId;
        this.version = version;
        this.cleanVersion = version;
        this.eventPageRatio = eventPageRatio;
        this.snapshot = snapshot;
        this.events = new ArrayDeque<>(events);
    }
    
    public Mk_EntityPage(long pageId, long entity, long entityId,
            int eventPageRatio, Snapshot snapshot)
    {
        this.pageId = pageId;
        this.entity = entity;
        this.entityId = entityId;
        this.version = snapshot.getVersion();
        this.cleanVersion = -1;
        this.eventPageRatio = eventPageRatio;
        this.snapshot = snapshot;
        this.events = new ArrayDeque<>();
    }
    
    @Override
    public long getPageId() {return pageId;}
    
    @Override
    public long getEntity() {return entity;}
    
    @Override
    public long getEntityId() {return entityId;}
    
    @Override
    public long getVersion() {return version;}
    
    @Override
    public long getCleanVersion() {return cleanVersion;}
    
    @Override
    public void setCleanVersion(long version) 
    {
        if(version > this.cleanVersion && 
                version <= this.version)
        {
            cleanVersion = version;
        }
    }

    @Override
    public Snapshot getSnapshot() {return snapshot;}

    @Override
    public Event[] getEvents() {return events.toArray(new Event[events.size()]);}
    
    @Override
    public int getEventPageRatio() {return eventPageRatio;}

    @Override
    public int events() {return eventPageRatio;}
    
    @Override
    public void addToPending(Event... events) throws AlreadyPendingChange
    {
        if(pending != null)
            throw new AlreadyPendingChange(String.format("Page %d of Entity %d "
                    + "of ID %d is already pending a change.", pageId, entity, 
                    entityId));
        pending = events;
    }

    @Override
    public void confirmEvents(Event... events)
    {
        if(pending == null || !Arrays.equals(pending, events))
            return;
        version += events.length;
        this.events.addAll(Arrays.asList(pending));
        pending = null;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof Mk_EntityPage))
            return false;
        
        Mk_EntityPage ep = (Mk_EntityPage) o;
        
        return (this.entity == ep.entity && 
                this.entityId == ep.entityId && 
                this.version == ep.version && 
                this.eventPageRatio == ep.eventPageRatio && 
                this.snapshot.equals(ep.snapshot) && 
                this.events.containsAll(ep.events));
                //this.events.equals(ep.events)); 
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 67 * hash + (int) (this.entity ^ (this.entity >>> 32));
        hash = 67 * hash + (int) (this.entityId ^ (this.entityId >>> 32));
        hash = 67 * hash + (int) (this.version ^ (this.version >>> 32));
        hash = 67 * hash + this.eventPageRatio;
        hash = 67 * hash + Objects.hashCode(this.snapshot);
        hash = 67 * hash + Objects.hashCode(this.events);
        return hash;
    }

    
    
    
}
