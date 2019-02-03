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
package com.mc1098.mk_eventstore.Event;

import com.mc1098.mk_eventstore.Entity.Entity;
import com.mc1098.mk_eventstore.Entity.Repository;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for building and applying an {@link Event} to an
 * {@link Entity} object.
 * 
 * This class can be used like other builders, using the 
 * {@link #put(java.lang.String, java.io.Serializable) } method to build up values
 * to be stored in the event. Though there is no particular limit it is recommended
 * to put minimum number of values in an event.
 * 
 * The building of an event will automatically apply the event to the entity 
 * object using it's {@link Entity#applyEvent(com.mc1098.mk_eventstore.Event.Event)}
 * method. The Entity is also checked against the {@link Repository}'s ERP for this
 * entity type and if required the repository will take a snapshot.
 * 
 * @param <T> - Entity Object.
 * 
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class EventDeveloper<T extends Entity>
{
    public static final Logger LOGGER = Logger.getLogger(EventDeveloper.class.getName());
    
    private final Repository<T> repository;
    private final Map<String, Serializable> eventMap;
    
    public EventDeveloper(Repository<T> repository)
    {
        this.repository = repository;
        this.eventMap = new HashMap<>();
    }
    
    public void put(String key, Serializable value) {eventMap.put(key, value);}
    
    public Event build(String name, T entity) throws EventStoreException
    {
        Event e = new Mk_Event(name, repository.getEntityName(), entity.getId(), 
                entity.getLoadedVersion() + entity.getNewEvents().length, 
                LocalDateTime.now(), eventMap);
        eventMap.clear();
        
        try
        {
            if(entity.getLoadedVersion() % 20 == 0)
                repository.takeSnapshot(entity);
            entity.applyEvent(e);
            return e;
        } catch(EventStoreException ex)
        {
            LOGGER.log(Level.INFO, null, ex);
            throw ex;
        }
    }
    
}
