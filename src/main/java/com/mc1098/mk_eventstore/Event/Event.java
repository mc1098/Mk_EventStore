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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;

/**
 * The Event interface defines the basic requirements of events for the Event Store 
 * to use, Events should contain values which represent changes in state for an 
 * {@link Entity} object.
 * 
 * Events are essentially treated as key-value stores making it easier to 
 * convert to non-language dependent data object such as JSON, XML etc.
 * The key-value store nature of events allows remote users who know the 
 * implementation of given events to use the event store to listen to certain events
 * and get the information within them without needing class files for the target 
 * entity. Therefore external Java applications need only depend on these base interfaces
 * when they do not require explicit need of the entity.
 * 
 * The target entity name and id are required. The entity name as described in the 
 * {@link EventStore} documentation is not recommended to be a class or interface 
 * name rather a meta-data name. Therefore implementations of this interface should
 * avoid a method accepting an Entity and using reflection for the class name for the
 * use of this entity name.
 * The occurred LocalDateTime should initiated upon construction of the event.
 * 
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public interface Event extends Serializable
{
    
    /**
     * @return Returns the name of this event.
     */
    public String           getName();
    
    /**
     * @return Returns the target entity name.
     */
    public String           getTargetEntityName();
    
    /**
     * @return Returns the target entity id.
     */
    public long             getTargetEntityId();
    
    /**
     * This method gets the version of the entity on which this event should be 
     * applied to.
     * @return Returns the entity version on which this event should be applied to.
     */
    public long             getVersion();
    
    
    /**
     * @return Returns the LocalDateTime this event occurred.
     */
    public LocalDateTime    getOccurred();
    
    /**
     * Retrives a Serializable value for the given key. The implementation of this
     * method may either choose to return null or throw a {@link NullPointerException}
     * in the event that no value corresponds to the key given.
     * 
     * @param <T> This allows for easy casting when using the method, the user
     * of this method should be certain of the type expected when requesting the object
     * with the given key.
     * @param key Used to identify the {@link Serializable} object in this key-value
     * store.
     * @return Returns a Serializable value for the given key.
     * 
     * @throws ClassCastException - This is thrown when trying to cast the return 
     * variable to class type it's not an instance of.
     * @throws NullPointerException - This is implementation specific but may
     * be thrown when no value corresponds to the key value given.
     * 
     */
    public <T extends Serializable> T getValue(String key);
    
    public void forEach(BiConsumer<String, Serializable> biConsumer);
    
}
