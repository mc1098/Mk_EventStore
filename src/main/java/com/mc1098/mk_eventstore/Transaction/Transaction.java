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


/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Transaction
{
    /*
    Events:
    size type entity entityId version data      
    0x14     0x01 0x01   0x01     0x00    0x0080 0x842 0x8421 
    Snapshot:
    size type entity entityId version data
    0x14 0x02 0x01   0x01    0x00    0x0x00842182
    */
    
//    public static Transaction fromBuffer(ByteBuffer buffer)
//    {
//        int size = buffer.getInt();
//        TransactionType type = TransactionType.values()[buffer.get()];
//        long entity = buffer.getLong();
//        long entityId = buffer.getLong();
//        long version = buffer.getLong();
//        byte[] data = new byte[size - (Byte.BYTES + (Long.BYTES * 3))];
//        buffer.get(data);
//        
//        return new Transaction(type, entity, entityId, version, data);
//    }
    
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
    
}


