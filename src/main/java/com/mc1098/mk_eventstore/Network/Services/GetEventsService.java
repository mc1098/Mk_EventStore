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
package com.mc1098.mk_eventstore.Network.Services;

import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.EventStore.EventStore;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import java.util.Queue;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class GetEventsService implements EventStoreService
{
    private final String entityName;
    private final long entityId;
    private final long versionFrom;
    private final long versionTo;
    
    public GetEventsService(String entityName, long entityId, long versionFrom, 
            long versionTo)
    {
        this.entityName = entityName;
        this.entityId = entityId;
        this.versionFrom = versionFrom;
        this.versionTo = versionTo;
    }
    
    public GetEventsService(String entityName, long entityId, long versionFrom)
    {
        this(entityName, entityId, versionFrom, -1);
    }

    @Override
    public ServiceResult execute(EventStore store)
    {
        try
        {
            Queue<Event> events;
            if(versionTo == -1)
                events = store.getEventsById(entityName, entityId, versionFrom);
            else 
                events = store.getEventsById(entityName, entityId, versionFrom, versionTo);
            
            return new ServiceResult(this, null, 
                    events.toArray(new Event[events.size()]));
            
        } catch (EventStoreException e)
        {
            return ServiceResult.errorResult(this, e.getLocalizedMessage());
        }
    }
    
}
