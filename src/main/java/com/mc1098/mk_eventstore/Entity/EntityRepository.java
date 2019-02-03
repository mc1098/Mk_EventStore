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
import com.mc1098.mk_eventstore.EventStore.EventStore;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Util.EventStoreUtils;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class EntityRepository<T extends Entity> implements Repository<T>
{

    private final String entityName;
    private final EventStore eventStore;
    private final Map<Long, Queue<Snapshot>> snapshots;
    
    public EntityRepository(String entityName, EventStore es)
    {
        this.entityName = entityName;
        this.eventStore = es;
        this.snapshots = new HashMap<>();
    }
    
    @Override
    public String getEntityName() {return entityName;}
    
    @Override
    public int getERP() {return eventStore.getERP(entityName);}

    @Override
    public T getById(long id) throws EventStoreException
    {
        EntityToken token = eventStore.getById(entityName, id);
        return tokenToEntity(token);
    }

    @Override
    public T getById(long id, long version) throws EventStoreException
    {
        EntityToken token = eventStore.getById(entityName, id, version);
        return tokenToEntity(token);
    }
    
    protected T tokenToEntity(EntityToken token) 
            throws EventStoreException
    {
        if(token.getSnapshots().length != 1)
            throw new EventStoreException("Only expecting one snapshot from "
                    + "EntityToken request");
        
        T t = (T) EventStoreUtils.deserialise(token.getSnapshots()[0].getBytes());
        if(token.getEvents().length > 0)
            t.loadHistoricEvents(token.getEvents());
        
        return t;
    }
    
    @Override
    public void save(T t) throws EventStoreException
    {
        if(!snapshots.containsKey(t.getId()))
            saveWithoutSnapshot(t);
        else
            saveWithSnapshot(t);
        
        snapshots.remove(t.getId());
        t.refresh();
    }

    public void saveWithSnapshot(T t) throws EventStoreException
    {
        Event[] events = t.getNewEvents();
        if(events.length == 0)
            eventStore.saveSnapshot(snapshots.get(t.getId()).peek());
        else
        {
            //requires token
            Snapshot[] snaps = snapshots.get(t.getId())
                    .toArray(new Snapshot[snapshots.get(t.getId()).size()]);
            EntityToken token = new EntityToken(snaps, events);
            eventStore.save(token);
        }
    }

    public void saveWithoutSnapshot(T t) throws EventStoreException
    {
        Event[] events = t.getNewEvents();
        
        if(events.length == 0)
            eventStore.saveSnapshot(new Mk_Snapshot(entityName, t.getId(), 0,
                    EventStoreUtils.serialise(t)));
        else 
            eventStore.save(entityName, t.getId(), t.getLoadedVersion(), events);
    }
    
    @Override
    public void takeSnapshot(T t) throws EventStoreException
    {
        long version = t.getLoadedVersion() + t.getNewEvents().length;
        byte[] data = EventStoreUtils.serialise(t);
        Snapshot ss = new Mk_Snapshot(entityName, t.getId(), version, data);
        
        if(snapshots.containsKey(t.getId()))
            snapshots.get(t.getId()).add(ss);
        else 
        {
            ArrayDeque<Snapshot> deque = new ArrayDeque<>();
            deque.add(ss);
            snapshots.put(t.getId(), deque);
        }
        
    }
    
}
