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

import com.mc1098.mk_eventstore.Exception.ParseException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import com.mc1098.mk_eventstore.Util.EventStoreUtils;
import java.nio.ByteBuffer;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class SimpleEventConverter implements EventConverter
{

    @Override
    public byte[] toBytes(Event event) throws SerializationException
    {
        try
        {
            return EventStoreUtils.serialise(event);
        } catch(SerializationException ex)
        {
            throw new SerializationException(ex);
        }
    }

    @Override
    public Event parse(ByteBuffer buffer) throws ParseException
    {
        try
        {
            int size = buffer.getInt();
            byte[] bytes = new byte[size];
            buffer.get(bytes);
            return (Event) EventStoreUtils.deserialise(bytes);
        } catch(SerializationException | ClassCastException ex)
        {
            throw new ParseException(ex);
        }
    }
    
}
