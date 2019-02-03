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
import java.util.Arrays;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class EntityToken implements Serializable
{
    private final Snapshot[] snapshots;
    private final Event[] events;
    
    public EntityToken(Snapshot snapshot, Event[] events)
    {
        this.snapshots = new Snapshot[]{snapshot};
        this.events = events;
    }
    
    public EntityToken(Snapshot[] snapshots, Event[] events)
    {
        this.snapshots = snapshots;
        this.events = events;
    }
    
    public Snapshot[] getSnapshots() {return snapshots;}
    
    public Event[] getEvents() {return events;}
    
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof EntityToken))
            return false;
        
        EntityToken et = (EntityToken) o;
        
        return (Arrays.equals(this.snapshots, et.snapshots) && 
                Arrays.equals(this.events, et.events));
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + Arrays.deepHashCode(this.snapshots);
        hash = 17 * hash + Arrays.deepHashCode(this.events);
        return hash;
    }
}
