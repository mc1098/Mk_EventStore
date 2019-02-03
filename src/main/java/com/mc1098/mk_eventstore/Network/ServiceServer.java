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
package com.mc1098.mk_eventstore.Network;

import com.mc1098.mk_eventstore.EventStore.EventStore;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import com.mc1098.mk_eventstore.Network.Services.EventStoreService;
import com.mc1098.mk_eventstore.Network.Services.ServiceResult;
import com.mc1098.mk_eventstore.Exception.ServerReadException;
import com.mc1098.mk_eventstore.Util.EventStoreUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class ServiceServer extends Server
{
    
    private final EventStore store;
    
    public ServiceServer(EventStore store)
    {
        this.store = store;
    }
    
    @Override
    protected void processRead(SelectionKey key)
    {
        EventStoreService service = null;
        try
        {
            service = read(key);
            ServiceResult result = service.execute(store);
            write(key, result);
            
        } catch (ServerReadException e)
        {
            ServiceResult.errorResult(service, e.getLocalizedMessage());
        }
    }
    
    protected EventStoreService read(SelectionKey key) 
            throws ServerReadException
    {
        List<byte[]> bytes = new ArrayList<>();
        int size = 0;
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = 1;
        try
        {
            while(read > 0)
            {
                read = sc.read(buffer);
                buffer.flip();
                byte[] data = buffer.array();
                size += data.length;
                bytes.add(data);
            }

            buffer = ByteBuffer.allocate(size);
            for (byte[] b : bytes)
                buffer.put(b);
            buffer.flip();
            byte[] packetBytes = buffer.array();
            
            return deserialiseToEventStoreService(packetBytes);
            
        } catch(IOException ex)
        {
            throw new ServerReadException("Server recieved bytes in an incorrect"
                    + " format. The server is set to recieve packets", ex);
        }
    }

    private EventStoreService deserialiseToEventStoreService(byte[] packetBytes) throws ServerReadException
    {
        try
        {
            return (EventStoreService) EventStoreUtils
                    .deserialise(packetBytes);
        } catch(SerializationException | ClassCastException ex)
        {
            throw new ServerReadException("Server recieved bytes that cannot "
                    + "be deserialised into an accepted packet", ex);
        }
    }

    
    private void write(SelectionKey key, ServiceResult result)
    {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            oos.writeObject(result);
            oos.flush();
            byte[] bytes = baos.toByteArray();
            write(key, bytes);
        } catch(IOException ex)
        {
            //error logging
        }
    }
    

    
    
}
