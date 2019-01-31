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

import com.mc1098.mk_eventstore.Entity.EntityToken;
import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class TransactionBuilder
{
    
    public Transaction build(long pageId, long entity, long entityId, 
            Snapshot snapshot)
    {
        byte[] bytes = snapshot.getBytes();
        
        return new Transaction(TransactionType.PUT_SNAPSHOT, pageId, entity, 
                entityId, snapshot.getVersion(), bytes);
    }
    
    public Transaction build(long pageId, long entity, long entityId, 
            Event event) throws SerializationException
    {
        byte[] bytes = serialise(event);
        
        return new Transaction(TransactionType.PUT_EVENT, pageId, entity, 
                entityId, event.getVersion(), bytes);
    }
    
    public List<Transaction> build(long pageId, long entity, long entityId, 
            Event[] events) throws SerializationException
    {
        List<Transaction> list = new ArrayList<>();
        for (Event e : events)
            list.add(build(pageId, entity, entityId, e));
        return list;
    }
    
    private byte[] serialise(Serializable serializable) 
            throws SerializationException
    {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            oos.writeObject(serializable);
            oos.flush();
            return baos.toByteArray();
        } catch(IOException ex)
        {
            throw new SerializationException("Unable to serialize an event object "
                    + "when creating a transaction!", ex);
        }
    }
    
}
