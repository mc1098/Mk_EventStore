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

import java.util.ArrayDeque;
import java.util.Queue;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class ByteArrayBuilder 
{
    private int pos;
    private int gain;
    private int limit;
    private byte[] data;
//    private final Queue<Byte> data;
    
    public ByteArrayBuilder(int gain)
    {
        this.pos = 0;
        this.gain = gain <= 0 ? 1 : gain;
        this.data = new byte[gain];
    }
    
    private void grow()
    {
        byte[] bytes = new byte[limit + gain];
        System.arraycopy(data, 0, bytes, 0, data.length);
        data = bytes;
        limit += gain;
    }
    
    private void grow(int size)
    {
        if(size > gain && size / gain > 2)
            gain = size * (size / gain) -2;
        grow();
    }
    
    public void put(byte b)
    {
        if(pos + 1 > limit)
            grow();
        data[pos] = b;
        pos+= 1;
    }
    
    public void put(byte[] bytes)
    {
        if(pos + bytes.length > limit)
        {
            grow();
            put(bytes);
            return;
        }
        System.arraycopy(bytes, 0, data, pos, bytes.length);
        pos+= bytes.length;
    }
    
    public void putChar(char c)
    {
        putShort((short)c);
    }
    
    public void putDouble(double d)
    {
        putLong(Double.doubleToRawLongBits(d));
    }
    
    public void putFloat(float f)
    {
        putInt(Float.floatToRawIntBits(f));
    }
    
    public void putInt(int i)
    {
        if(pos + 4 > limit)
        {
            grow();
            putInt(i);
            return;
        }
        
        data[pos] = (byte) (i >> 24);
        pos+=1;
        data[pos] = (byte) (i >> 16);
        pos+=1;
        data[pos] = (byte) (i >> 8);
        pos+=1;
        data[pos] = (byte) i;
        pos+=1;
    }
    
    public void putLong(long l)
    {
        if(pos + 8 > limit)
        {
            grow();
            putLong(l);
            return;
        }
        
        data[pos] = (byte) (l >> 56);
        pos+=1;
        data[pos] = (byte) (l >> 48);
        pos+=1;
        data[pos] = (byte) (l >> 40);
        pos+=1;
        data[pos] = (byte) (l >> 32);
        pos+=1;
        data[pos] = (byte) (l >> 24);
        pos+=1;
        data[pos] = (byte) (l >> 16);
        pos+=1;
        data[pos] = (byte) (l >> 8);
        pos+=1;
        data[pos] = (byte) l;
        pos+=1;
    }
    
    public void putShort(short s)
    {
        if(pos + 2 > limit)
        {
            grow();
            putShort(s);
            return;
        }
        
        data[pos] = (byte) (s >> 8);
        pos+=1;
        data[pos] = (byte) s;
        pos+=1;
    }
    
    
    public byte[] array()
    {
        byte[] bytes = new byte[pos];
        System.arraycopy(data, 0, bytes, 0, bytes.length);
        return bytes;
    }
    
}
