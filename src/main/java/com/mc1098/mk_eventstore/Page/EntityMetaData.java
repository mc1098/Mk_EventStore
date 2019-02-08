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
package com.mc1098.mk_eventstore.Page;

import com.mc1098.mk_eventstore.Exception.ParseException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import com.mc1098.mk_eventstore.FileSystem.ByteParser;
import com.mc1098.mk_eventstore.FileSystem.ByteSerializer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class EntityMetaData
{
    public static final EntityMetaDataConverter CONVERTER = new EntityMetaDataConverter();
    
    public static class EntityMetaDataConverter implements ByteParser<EntityMetaData>, 
            ByteSerializer<EntityMetaData>
    {

        @Override
        public EntityMetaData parse(ByteBuffer buffer) throws ParseException
        {
            long entity = buffer.getLong();
            int erp = buffer.getInt();
            int nameSize = buffer.getInt();
            byte[] nameBytes = new byte[nameSize];
            buffer.get(nameBytes);
            String name = new String(nameBytes, StandardCharsets.UTF_8);
            return new EntityMetaData(name, entity, erp);
        }

        @Override
        public byte[] toBytes(EntityMetaData t) throws SerializationException
        {
            byte[] nameBytes = t.getName().getBytes(StandardCharsets.UTF_8);
            int size = Long.BYTES + (Integer.BYTES * 2) + nameBytes.length;
            ByteBuffer buffer = ByteBuffer.allocate(size);
            buffer.putLong(t.getEntity());
            buffer.putInt(t.getErp());
            buffer.putInt(nameBytes.length);
            buffer.put(nameBytes);
            return buffer.array();
        }
        
    }
    
    private final String name;
    private final long entity;
    private final int erp;
    
    public EntityMetaData(String name, long entity, int erp)
    {
        this.name = name;
        this.entity = entity;
        this.erp = erp;
    }

    public String getName()
    {
        return name;
    }

    public long getEntity()
    {
        return entity;
    }

    public int getErp()
    {
        return erp;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof EntityMetaData))
            return false;
        
        EntityMetaData emd = (EntityMetaData)o;
        
        return (this.name.equals(emd.name) && 
                this.entity == emd.entity && 
                this.erp == emd.erp);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + (int) (this.entity ^ (this.entity >>> 32));
        hash = 53 * hash + this.erp;
        return hash;
    }
    
    
}
