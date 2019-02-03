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
package com.mc1098.mk_eventstore.Transaction;

import java.util.Arrays;
import java.util.Objects;


/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Transaction
{
    private final TransactionType type;
    private final long pageId;
    private final long entity;
    private final long entityId;
    private final long version;
    private final byte[] data;
    
    public Transaction(TransactionType type, long pageId, long entity, 
            long entityId, long version, byte[] data)
    {
        this.type = type;
        this.pageId = pageId;
        this.entity = entity;
        this.entityId = entityId;
        this.version = version;
        this.data = data;
    }

    public TransactionType getType() {return type;}
    
    public long getPageId(){return pageId;}

    public long getEntity() {return entity;}

    public long getEntityId() {return entityId;}

    public long getVersion() {return version;}

    public byte[] getData() {return data;}
    
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Transaction))
            return false;
        
        Transaction t = (Transaction) o;
        
        return (this.type.equals(t.type) && 
                this.pageId == t.pageId &&
                this.entity == t.entity && 
                this.entityId == t.entityId && 
                this.version == t.version && 
                Arrays.equals(this.data, t.data));
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.type);
        hash = 71 * hash + (int) (this.pageId ^ (this.pageId >>> 32));
        hash = 71 * hash + (int) (this.entity ^ (this.entity >>> 32));
        hash = 71 * hash + (int) (this.entityId ^ (this.entityId >>> 32));
        hash = 71 * hash + (int) (this.version ^ (this.version >>> 32));
        hash = 71 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
}


