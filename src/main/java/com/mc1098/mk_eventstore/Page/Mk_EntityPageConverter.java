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

import com.mc1098.mk_eventstore.Entity.Mk_Snapshot;
import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Exception.ParseException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import com.mc1098.mk_eventstore.Event.EventConverter;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_EntityPageConverter implements EntityPageConverter
{
    private final PageDirectory directory;
    private final EventConverter eventConverter;
    
    public Mk_EntityPageConverter(PageDirectory directory, EventConverter ec)
    {
        this.directory = directory;
        this.eventConverter = ec;
    }

    @Override
    public EntityPage parse(ByteBuffer buffer) throws ParseException
    {
        long pageId = buffer.getLong();
        long version = buffer.getLong();
        long entity = buffer.getLong();
        String entityName = directory.getEntityName(entity);
        int epr = directory.getEPR(entity);
        long entityId = buffer.getLong();
        
        int snapSize = buffer.getInt();
        byte[] snapData = new byte[snapSize];
        buffer.get(snapData);
        
        Queue<Event> events = new ArrayDeque<>();
        
        while(buffer.hasRemaining())
            events.add(eventConverter.parse(buffer));
        
        long snapshotVersion  = version - events.size();
        Snapshot snapshot = new Mk_Snapshot(entityName, entityId, 
                snapshotVersion, snapData);
        
        return new Mk_EntityPage(pageId, entity, entityId, version, epr, 
                snapshot, events);
    
    }

    @Override
    public byte[] toBytes(EntityPage page) throws SerializationException
    {
        int size = 0;
        
        //pageId, version, entity, entityId (long)
        size += (Long.BYTES * 4);
        //snapsize (int)
        size += Integer.BYTES;
        byte[] snapBytes = page.getSnapshot().getBytes();
        size += snapBytes.length;
        
        byte[][] bytes = new byte[page.getEvents().length][0];
        Event[] events = page.getEvents();
        
        for (int i = 0; i < events.length; i++)
        {
            byte[] data = eventConverter.toBytes(events[i]);
            size+= Integer.BYTES + data.length;
            bytes[i] = data;
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putLong(page.getPageId());
        buffer.putLong(page.getVersion());
        buffer.putLong(page.getEntity());
        buffer.putLong(page.getEntityId());
        buffer.putInt(snapBytes.length);
        buffer.put(snapBytes);
        
        for (byte[] bs : bytes)
        {
            buffer.putInt(bs.length);
            buffer.put(bs);
        }
        return buffer.array();
    }
    
}
