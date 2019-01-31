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
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * An interface representing the implementation requirements of an object to 
 * be source controlled using the {@link EventStore}.
 * 
 * It is highly recommended that users extend from the {@link Mk_Event} as this 
 * contains the intended implementation of this interface.
 * 
 * Implementations will be serialised to form snapshots therefore users should
 * be mindful of changes to class in the future that could cause past snapshots 
 * to fail deserialization. If the need for changes arrives then it is suggested
 * to call all events from the {@link EventStore} and load up the new implementation
 * from these events however the initial construction state will be lost. 
 * 
 * 
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public interface Entity extends Serializable
{
    
    /**
     * @return Returns the Id value for this Entity.
     */
    public long getId();
    /**
     * Returns the version number this entity was loaded from.
     * The loaded version is calculated based on the number of events that are 
     * loaded via the 
     * {@link #loadHistoricEvents(com.mc1098.mk_event.Event.Event[])} method.
     * 
     * Implementations of this Entity should be aware that for the EventStore 
     * version start at zero.
     * 
     * @return Retrieves a long value representing the loaded version of state for 
     * this entity.
     */
    public long getLoadedVersion();
    /**
     * @return Returns a LocalDateTime value representing when the last event 
     * was applied to this entity.
     */
    public LocalDateTime getLastEventStamp();
    
    /**
     * This method should be used for Event handling, implementations should 
     * then store the events and seek an implemented method with a 
     * {@link @EventHandler} annotation to pass the event to.
     * 
     * Implementations of this method should check that the events target
     * is this entity and the version of this Entity matches the event version, 
     * after that the event should be guaranteed not to fail and 
     * any failure of finding an annotated method for this given event or failure
     * within that method should not be handled.
     * 
     * @param e Event to be applied to this entity.
     */
    public void applyEvent(Event e);
    
    /**
     * This method returns an array of events that have been applied to this 
     * entity since loaded. The array should not be backed by the implementation 
     * and the events should not be cleared after this call.
     * @return Returns an array of events that have been applied to this entity since loaded.
     */
    public Event[] getNewEvents();
    
    /**
     * Load all historical events to the entity.
     * 
     * The Events should only be for historical events, events that have been
     * confirmed and saved by the Event Store and failure to adhere to this will
     * cause failure in saving the entity to the Event Store.

     * Implementations of this method should increment the loaded version value with each
     * historical event.
     * Therefore if the array has n elements then the loaded version from a new Entity
     * would be n. The events loaded into this method should be dealt with in 
     * the same manner as the {@link #applyEvent(com.mc1098.mk_event.Event.Event)} 
     * apart from storing the events.
     * 
     * @param events Contains historical events that need to be loaded into this entity.
     */
    public void loadHistoricEvents(Event[] events);
    
    /**
     * This method should update the loadedVersion value to match the current 
     * events that our saved and those events that are saved are cleared, this 
     * method is only expected to be called by the {@link EventStore}.
     */
    public void refresh();
}
