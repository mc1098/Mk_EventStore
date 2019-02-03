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
package com.mc1098.mk_eventstore.Util;

import com.mc1098.mk_eventstore.Exception.SerializationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class EventStoreUtils
{
    public static Serializable deserialise(byte[] bytes) 
            throws SerializationException
    {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais))
        {
            return (Serializable) ois.readObject();
        } catch(IOException | ClassNotFoundException ex)
        {
            throw new SerializationException(ex);
        }
    }
    
    public static byte[] serialise(Serializable serializable) 
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
            throw new SerializationException(ex);
        }
    }
    
    private EventStoreUtils()
    {
        throw new IllegalStateException("Utility class");
    }
}
