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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_Event implements Event
{
    
    private final String name;
    private final String targetEntityName;
    private final long targetEntityId;
    private final long version;
    private final LocalDateTime occurred;
    private final HashMap<String, Serializable> map;
    
    public Mk_Event(String name, String targetEntityName, long targetEntityId, 
            long version, LocalDateTime occurred, Map<String, Serializable> map)
    {
        this.name = name;
        this.targetEntityName = targetEntityName;
        this.targetEntityId = targetEntityId;
        this.version = version;
        this.occurred = occurred;
        this.map = new HashMap<>(map);
    }

    @Override
    public String getName() {return name;}

    @Override
    public String getTargetEntityName() {return targetEntityName;}

    @Override
    public long getTargetEntityId() {return targetEntityId;}
    
    @Override
    public long getVersion() {return version;}

    @Override
    public LocalDateTime getOccurred() {return occurred;}

    @Override
    public <T extends Serializable> T getValue(String key)
    {
        if(map.containsKey(key))
            return (T) map.get(key);
        return null;
    }

    @Override
    public void forEach(BiConsumer<String, Serializable> biConsumer)
    {
        map.forEach(biConsumer);
    }
    
    @Override
    public String toString() 
    {
        return String.format("%s[%s]", name, targetEntityName);
    }
    
    
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Mk_Event))
            return false;
        
        Mk_Event e = (Mk_Event) o;
        
        return (this.name.equals(e.name) && 
                this.targetEntityName.equals(e.targetEntityName) && 
                this.targetEntityId == e.targetEntityId && 
                this.occurred.equals(e.occurred) &&
                this.version == e.version &&
                this.map.equals(e.map));
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + Objects.hashCode(this.targetEntityName);
        hash = 47 * hash + (int) (this.targetEntityId ^ (this.targetEntityId >>> 32));
        hash = 47 * hash + Objects.hashCode(this.occurred);
        hash = 47 * hash + Objects.hashCode(this.map);
        return hash;
    }
    
}
