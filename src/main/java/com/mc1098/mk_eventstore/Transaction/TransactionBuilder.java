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

import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import com.mc1098.mk_eventstore.Util.EventStoreUtils;
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
            Event event) throws EventStoreException
    {
        byte[] bytes = EventStoreUtils.serialise(event);
        
        return new Transaction(TransactionType.PUT_EVENT, pageId, entity, 
                entityId, event.getVersion(), bytes);
    }
    
    public List<Transaction> build(long pageId, long entity, long entityId, 
            Event[] events) throws EventStoreException
    {
        if(events.length == 0)
            throw new EventStoreException("Transaction cannot be made with "
                    + "no events.");
        List<Transaction> list = new ArrayList<>();
        for (Event e : events)
            list.add(build(pageId, entity, entityId, e));
        return list;
    }
    
}
