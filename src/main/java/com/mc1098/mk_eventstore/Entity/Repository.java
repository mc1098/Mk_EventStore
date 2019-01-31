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

import com.mc1098.mk_eventstore.Exception.EventStoreException;

/**
 * The repository is a convenient class with interacts with the {@link EventStore}
 * in order to return {@link Entity} values by query.
 * 
 * Implementations of the repository hide the detail behind retrieving and 
 * saving Entity snapshots and events. 
 * 
 * 
 * 
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 * @param <T> This is the Entity type to be retrieved or saved via this repository.
 */
public interface Repository<T extends Entity>
{
    /**
     * @return Returns the dedicated entity name known by the {@link EventStore}
     * The Entity name value is not guaranteed to be class.getName() or simple name
     * it is also highly recommended that this should not be the case.
     */
    public String getEntityName();
    
    /**
     * @return Returns the Event Ratio per Page (ERP) for this Entity type.
     * 
     * The ERP is the ratio of which snapshots occur. A snapshot will occur
     * when the version of the entity is a multiple of the ERP for that entity type.
     * Or simply:
     * Version % ERP == 0 ? snapshot : null.
     * 
     * The ERP also controls how many events are saved together in a logical 
     * group by the {@link EventStore}. The default ERP is set to 20.
     * 
     * 
     */
    public int getERP();
    
    /**
     * Retrieves the most upto date entity object with the id provided.
     * @param id - number representing a unique identifier.
     * @return Returns the most upto date entity object with the id provided.
     * @throws EventStoreException when the entity with the given id does not exist
     * or an error occurred trying to retrieve such entity.
     */
    public T getById(long id) throws EventStoreException;
    
    
    /**
     * Retrieves an entity object with the id provided and with a version equal 
     * or less than the version provided.
     * @param id number representing a unique identifier.
     * @param version number representing the version.
     * @return Returns an entity object with the id provided and with a version 
     * equal or less than the version provided.
     * @throws EventStoreException EventStoreException when the entity with 
     * the given id does not exist or an error occurred trying to retrieve 
     * such entity.
     */
    public T getById(long id, long version) throws EventStoreException;
    
    /**
     * Method responsible for saving the Entity using the {@link EventStore}.
     * 
     * This method will save the Entity and should call the 
     * {@link Entity#refresh()} method on that entity so that the entity can be
     * used and saved without causing a concurrency update failure.
     * 
     * Implementations should adhere to the expectation that snapshots are 
     * expected on versions which are multiples of that entity ERP. {@link
     * EntityTokens} are expected to have both snapshots and events and if only
     * one of those is required for the save then the implementation should use 
     * the respective event store methods.
     * @param t Entity object.
     * @throws EventStoreException - When an error has occurred saving this entity. 
     * Exceptions are not expected even if this is the first entity of this type to
     * be saved.
     */
    public void save(T t) throws EventStoreException;
    
    /**
     * Method responsible for taking a snapshot of an entity and storing it locally ready for 
     * a save of the Entity.
     * 
     * The {@link EventDeveloper} will use this method when creating events for an
     * entity if the version of that entity is a multiple of the ERP.
     * 
     * @param t
     * @throws EventStoreException 
     */
    public void takeSnapshot(T t) throws EventStoreException;
}
