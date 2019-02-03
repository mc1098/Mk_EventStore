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
package com.mc1098.mk_eventstore.EventStore;

import com.mc1098.mk_eventstore.Entity.EntityToken;
import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Exception.EntityChronologicalException;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import java.util.Queue;

/**
 * The EventStore is one of the main classes of the mk_EventStore project, it is
 * responsible for receiving get and save requests for {@link Entity} objects. 
 * 
 * Implementations of this interface should be mindful of the requirements of 
 * the implemented entities and their respective supporting classes. It is recommended 
 * that implementations do not assume that an EntityName == class.getName() on that
 * entity as other documentation in this projects recommends against it.
 * 
 * {@link Mk_EventStore} is the recommended implementation for this interface.
 * 
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public interface EventStore extends AutoCloseable
{
    /**
     * Retrieves the Events Ratio per Page (ERP) for the given EntityName. 
     * If the EntityName cannot be found, therefore assumed to be a new Entity, then 
     * a default value is returned. Implementations of this method will decide on this
     * default value. 
     * 
     * The ERP is part of a calculation that is used to seek the correct information, 
     * therefore the value is set and cannot be changed. 
     * @param entityName The dedicated meta-name for this Entity type.
     * @return Returns the Event Ration per Page (ERP) for the given EntityName.
     */
    public int getERP(String entityName);
    
    /**
     * Retrieves an {@link EntityToken} representing the current state of the {@link Entity} that matches the given 
     * parameters.
     * 
     * The EntityToken should not be assumed to be backed by the EventStore, therefore
     * it is recommended to reduce the amount of different process that could change the
     * Entity state in the EventStore, see 
     * {@link #save(com.mc1098.mk_eventstore.Entity.EntityToken)} for more info regarding
     * concurrent updates.
     * <br>
     * Unlike the requirements of the {@link #save(com.mc1098.mk_eventstore.Entity.EntityToken)} 
     * the EntityToken returned by this method does not guarantee that at least 
     * one {@link Event} is present. This method does guarantee that the EntityToken
     * returned will have only one {@link Snapshot}. 
     * 
     * @param entityName The dedicated meta-name for this Entity type.
     * @param id The unique identification for the Entity type.
     * @return Returns an {@link EntityToken} representing the current state of 
     * the {@link Entity} that matches the given parameters.
     * @throws EventStoreException thrown when the entity does not exist or 
     * an error has occurred while retrieving the entity data.
     */
    public EntityToken getById(String entityName, long id) throws EventStoreException;
    
    /**
     * Retrieves an {@link EntityToken} representing the current state of the 
     * {@link Entity} that matches the given parameters, the version of the Entity
     * will match the given parameter if available or will return the most current 
     * Entity under that version.
     * 
     * The EntityToken should not be assumed to be backed by the EventStore, therefore
     * it is recommended to reduce the amount of different process that could change the
     * Entity state in the EventStore, see 
     * {@link #save(com.mc1098.mk_eventstore.Entity.EntityToken)} for more info regarding
     * concurrent updates.
     * <br>
     * Unlike the requirements of the {@link #save(com.mc1098.mk_eventstore.Entity.EntityToken)} 
     * the EntityToken returned by this method does not guarantee that at least 
     * one {@link Event} is present. This method does guarantee that the EntityToken
     * returned will have only one {@link Snapshot}. 
     * 
     * @param entityName The dedicated meta-name for this Entity type.
     * @param id The unique identification for the Entity type.
     * @param version The version of the Entity type
     * @return Returns an {@link EntityToken} representing the current state of 
     * the {@link Entity} that matches the given parameters.
     * @throws EventStoreException thrown when the entity does not exist or 
     * an error has occurred while retrieving the entity data.
     */
    public EntityToken getById(String entityName, long id, long version) throws EventStoreException;
    
    /**
     * Retrieves a Queue of {@link Event} objects which represent the changes to
     * state for the {@link Entity} matching the parameters.
     * @param entityName The dedicated meta-name for this Entity type.
     * @param id The unique identification for the Entity type.
     * @param fromVer The version from which the first event in the queue should match (inclusive).
     * @return Returns a Queue of {@link Event} objects which represent the changes to
     * state for the {@link Entity} matching the parameters.
     * @throws EventStoreException thrown when the entity does not exist or 
     * an error has occurred while retrieving the entity data.
     */
    public Queue<Event> getEventsById(String entityName, long id, long fromVer) throws EventStoreException;
    
    /**
     * Retrieves a Queue of {@link Event} objects which represent the changes to
     * state for the {@link Entity} matching the parameters.
     * @param entityName The dedicated meta-name for this Entity type.
     * @param id The unique identification for the Entity type.
     * @param fromVer The version from which the first event in the queue should match (inclusive).
     * @param toVer The maximum version of event to be returned (inclusive).
     * @return Returns a Queue of {@link Event} objects which represent the changes to
     * state for the {@link Entity} matching the parameters.
     * @throws EventStoreException thrown when the entity does not exist or 
     * an error has occurred while retrieving the entity data.
     */
    public Queue<Event> getEventsById(String entityName, long id, long fromVer, 
            long toVer) throws EventStoreException;
    
    /**
     * Retrieves the most recent {@link Snapshot} object for the {@link Entity} which 
     * matches the parameters.
     * <br>
     * The saveSnapshot returned by this method may represent the most recent state for
     * the Entity, however this is not guaranteed and events should be used to 
     * build state on top of the most recent saveSnapshot.
     * @param entityName The dedicated meta-name for this Entity type.
     * @param id The unique identification for the Entity type.
     * @return Returns the most recent {@link Snapshot} object for the 
     * {@link Entity} which matches the parameters.
     * @throws EventStoreException thrown when the entity does not exist or 
     * an error has occurred while retrieving the entity data.
     */
    public Snapshot getSnapshot(String entityName, long id) throws EventStoreException;
    
    
    /**
     * Retrieves the most recent {@link Snapshot} object for the {@link Entity} which 
     * matches the parameters.
     * <br>
 The saveSnapshot returned by this method may represent the most recent state for
 the Entity, however this is not guaranteed and events should be used to 
 build state on top of the most recent saveSnapshot.
     * @param entityName The dedicated meta-name for this Entity type.
     * @param id The unique identification for the Entity type.
     * @param lteq version for saveSnapshot to match or be less than.
     * @return Returns the most recent {@link Snapshot} object for the 
     * {@link Entity} which matches the parameters.
     * @throws EventStoreException thrown when the entity does not exist or 
     * an error has occurred while retrieving the entity data.
     */
    public Snapshot getSnapshot(String entityName, long id, long lteq) throws EventStoreException;
    
    /**
     * 
     * @param ss
     * @throws EntityChronologicalException
     * @throws EventStoreException 
     */
    public void saveSnapshot(Snapshot ss) throws  EventStoreException;
    
    public void save(String entityName, long id, long loadedVersion, 
            Event[] events) throws EventStoreException;
    
    public void save(EntityToken token) throws EventStoreException;
    
    
    
    
}
