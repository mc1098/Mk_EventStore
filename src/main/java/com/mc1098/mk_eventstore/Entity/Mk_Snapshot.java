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

import java.util.Arrays;
import java.util.Objects;


/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_Snapshot implements Snapshot
{

    private final String entityName;
    private final long entityId;
    private final long version;
    private final byte[] data;
    
    public Mk_Snapshot(String entityName, long entityId, long version,
            byte[] data)
    {
        this.entityName = entityName;
        this.entityId = entityId;
        this.version = version;
        this.data = data;
    }

    @Override
    public String getEntityName() {return entityName;}

    @Override
    public long getEntityId() {return entityId;}

    @Override
    public long getVersion() {return version;}

    @Override
    public byte[] getBytes() {return data;}
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof Mk_Snapshot))
            return false;
        
        Mk_Snapshot ss = (Mk_Snapshot)o;
        
        return (this.entityName.equals(ss.entityName) && 
                this.entityId == ss.entityId && 
                this.version == ss.version && 
                Arrays.equals(this.data, ss.data));
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.entityName);
        hash = 67 * hash + (int) (this.entityId ^ (this.entityId >>> 32));
        hash = 67 * hash + (int) (this.version ^ (this.version >>> 32));
        hash = 67 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
}
