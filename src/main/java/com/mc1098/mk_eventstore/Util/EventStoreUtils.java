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
import java.util.Arrays;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class EventStoreUtils
{
    public static boolean isEqual(Serializable s, Serializable s2)
    {
        if(!(s.getClass().isArray() && s.getClass().isArray()))
            return s.equals(s2);
        
        Class c = s.getClass();
        Class c2 = s.getClass();
        
        if(c.equals(boolean[].class) && c2.equals(boolean[].class))
            return Arrays.equals((boolean[]) s, (boolean[])s2);
        if(c.equals(byte[].class) && c2.equals(byte[].class))
            return Arrays.equals((byte[]) s, (byte[])s2);
        if(c.equals(short[].class) && c2.equals(short[].class))
            return Arrays.equals((short[]) s, (short[])s2);
        if(c.equals(int[].class) && c2.equals(int[].class))
            return Arrays.equals((int[]) s, (int[])s2);
        if(c.equals(long[].class) && c2.equals(long[].class))
            return Arrays.equals((long[]) s, (long[])s2);
        if(c.equals(float[].class) && c2.equals(float[].class))
            return Arrays.equals((float[]) s, (float[])s2);
        if(c.equals(double[].class) && c2.equals(double[].class))
            return Arrays.equals((double[]) s, (double[])s2);
        else 
            return Arrays.equals((Object[]) s, (Object[]) s2);
        
        
    }
    
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
}
