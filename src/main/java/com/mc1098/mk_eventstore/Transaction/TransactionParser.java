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

import java.nio.ByteBuffer;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class TransactionParser
{
    
    /*
    Events:
    size type pageId   entity   entityId    version     data      
    0x14 0x01 0x01     0x01     0x01        0x00        0x0080 0x842 0x8421 
    Snapshot:
    size type pageId entity entityId    version    data
    0x14 0x02 0x01   0x01   0x01        0x00       0x0x00842182
    */
    
    public Transaction parse(ByteBuffer buffer)
    {
//        int size = buffer.getInt();
        TransactionType type = TransactionType.values()[buffer.get()];
        long pageId = buffer.getLong();
        long entity = buffer.getLong();
        long entityId = buffer.getLong();
        long version = buffer.getLong();
        int dataSize = buffer.getInt();
        byte[] data = new byte[dataSize];
        buffer.get(data);
        
        return new Transaction(type, pageId, entity, entityId, version, data);
    }
    
    public byte[] toBytes(Transaction transaction)
    {
        int size = Byte.BYTES + (Long.BYTES * 4) + Integer.BYTES + transaction.getData().length;
        
        byte[] bytes = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.rewind();
        //buffer.putInt(size);
        buffer.put((byte)transaction.getType().ordinal());
        buffer.putLong(transaction.getPageId());
        buffer.putLong(transaction.getEntity());
        buffer.putLong(transaction.getEntityId());
        buffer.putLong(transaction.getVersion());
        buffer.putInt(transaction.getData().length);
        buffer.put(transaction.getData());
        return buffer.array();
    }
}
