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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public abstract class SerializableWrapper
{
    
    enum type 
    {
        STRING(EventConstants.STRING, String.class),
        ARY_STRING((byte)-0x10, String[].class),
        NUMBER((byte)0x20, Number.class), 
        ARY_NUMBER((byte)-0x20, Number[].class),
        BOOL((byte)0x40, boolean.class), 
        ARY_BOOL((byte)-0x40, Boolean[].class);
        
        
        private final byte value;
        private final Class c;
        
        private type(byte value, Class c)
        {
            this.value = value;
            this.c =c;
        }
        
        public byte value(){return value;};
        public Class type() {return c;}
        
        public static byte of(Serializable serializable)
        {
            if(serializable == null)
                return EventConstants.NULL;
            
            Class c = serializable.getClass();
            
            for (type v : values())
                if(v.c.isAssignableFrom(c))
                    return v.value;
            return EventConstants.OBJ;
        }
        
        
    }
    
    
    public static SerializableWrapper of(Serializable serializable)
    {
        if(serializable.getClass().isArray())
            return ofArray(serializable);
        return new ObjectWrapper(type.of(serializable), serializable);
    }
    
    private static SerializableWrapper ofArray(Serializable serializable)
    {
        Class c = serializable.getClass();
        
        if(c.equals(boolean[].class))
            return new PrimativeBooleanArrayWrapper((boolean[]) serializable);
        if(c.equals(char[].class))
            return new PrimativeCharArrayWrapper((char[]) serializable);
        if(c.equals(byte[].class))
            return new PrimativeByteArrayWrapper((byte[]) serializable);
        if(c.equals(short[].class))
            return new PrimativeShortArrayWrapper((short[])serializable);
        if(c.equals(int[].class))
            return new PrimativeIntArrayWrapper((int[])serializable);
        if(c.equals(long[].class))
            return new PrimativeLongArrayWrapper((long[])serializable);
        if(c.equals(float[].class))
            return new PrimativeFloatArrayWrapper((float[])serializable);
        if(c.equals(double[].class))
            return new PrimativeDoubleArrayWrapper((double[])serializable);
        else 
            return new ObjectWrapper(type.of(serializable), serializable);
    }
    
    public abstract byte type();
    public abstract Serializable get();
}


class ObjectWrapper extends SerializableWrapper
{
    private final byte type;
    private final Serializable serializable;
    
    public ObjectWrapper(byte type, Serializable serializable)
    {
        this.type = serializable == null ? EventConstants.NULL : type;
        this.serializable = serializable;
    }

    @Override
    public Serializable get() {return serializable;}
    
    @Override
    public byte type() {return type;}
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof ObjectWrapper))
            return false;
        
        ObjectWrapper ow = (ObjectWrapper) o;
        
        if(this.serializable.getClass().isArray() && 
                ow.serializable.getClass().isArray())
            return Arrays.equals((Object[]) this.serializable, 
                    (Object[]) ow.serializable);
        
        return this.serializable.equals(ow.serializable);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.serializable);
        return hash;
    }
}

class PrimativeBooleanArrayWrapper extends SerializableWrapper
{
    private final boolean[] data;
    
    public PrimativeBooleanArrayWrapper(boolean[] data)
    {
        this.data = data;
    }
    
    @Override
    public Serializable get() {return data;}
    
    @Override
    public byte type() {return EventConstants.ARY_BOOL;}
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof PrimativeBooleanArrayWrapper))
            return false;
        
        PrimativeBooleanArrayWrapper pbaw = (PrimativeBooleanArrayWrapper) o;
        
        return Arrays.equals(this.data, pbaw.data);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 73 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
}

class PrimativeCharArrayWrapper extends SerializableWrapper
{
    
    private final char[] data;
    
    public PrimativeCharArrayWrapper(char[] data)
    {
        this.data = data;
    }
    
    @Override
    public Serializable get() {return data;}
    
    @Override
    public byte type() {return EventConstants.ARY_STRING;}
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof PrimativeCharArrayWrapper))
            return false;
        
        PrimativeCharArrayWrapper pbaw = (PrimativeCharArrayWrapper) o;
        
        return Arrays.equals(this.data, pbaw.data);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 73 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
}

class PrimativeByteArrayWrapper extends SerializableWrapper
{
    
    private final byte[] data;
    
    public PrimativeByteArrayWrapper(byte[] data)
    {
        this.data = data;
    }
    
    @Override
    public Serializable get() {return data;}
    
    @Override
    public byte type() {return EventConstants.ARY_NUMBER;}
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof PrimativeByteArrayWrapper))
            return false;
        
        PrimativeByteArrayWrapper pbaw = (PrimativeByteArrayWrapper) o;
        
        return Arrays.equals(this.data, pbaw.data);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 73 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
}


class PrimativeShortArrayWrapper extends SerializableWrapper
{
    
    private final short[] data;
    
    public PrimativeShortArrayWrapper(short[] data)
    {
        this.data = data;
    }
    
    @Override
    public Serializable get() {return data;}
    
    @Override
    public byte type() {return EventConstants.ARY_NUMBER;}
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof PrimativeShortArrayWrapper))
            return false;
        
        PrimativeShortArrayWrapper pbaw = (PrimativeShortArrayWrapper) o;
        
        return Arrays.equals(this.data, pbaw.data);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 73 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
}

class PrimativeIntArrayWrapper extends SerializableWrapper
{
    
    private final int[] data;
    
    public PrimativeIntArrayWrapper(int[] data)
    {
        this.data = data;
    }
    
    @Override
    public Serializable get() {return data;}
    
    @Override
    public byte type() {return EventConstants.ARY_NUMBER;}
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof PrimativeIntArrayWrapper))
            return false;
        
        PrimativeIntArrayWrapper pbaw = (PrimativeIntArrayWrapper) o;
        
        return Arrays.equals(this.data, pbaw.data);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 73 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
}

class PrimativeLongArrayWrapper extends SerializableWrapper
{
    
    private final long[] data;
    
    public PrimativeLongArrayWrapper(long[] data)
    {
        this.data = data;
    }
    
    @Override
    public Serializable get() {return data;}
    
    @Override
    public byte type() {return EventConstants.ARY_NUMBER;}
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof PrimativeLongArrayWrapper))
            return false;
        
        PrimativeLongArrayWrapper pbaw = (PrimativeLongArrayWrapper) o;
        
        return Arrays.equals(this.data, pbaw.data);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 73 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
}

class PrimativeFloatArrayWrapper extends SerializableWrapper
{
    
    private final float[] data;
    
    public PrimativeFloatArrayWrapper(float[] data)
    {
        this.data = data;
    }
    
    @Override
    public Serializable get() {return data;}
    
    @Override
    public byte type() {return EventConstants.ARY_NUMBER;}
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof PrimativeFloatArrayWrapper))
            return false;
        
        PrimativeFloatArrayWrapper pbaw = (PrimativeFloatArrayWrapper) o;
        
        return Arrays.equals(this.data, pbaw.data);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 73 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
}

class PrimativeDoubleArrayWrapper extends SerializableWrapper
{
    
    private final double[] data;
    
    public PrimativeDoubleArrayWrapper(double[] data)
    {
        this.data = data;
    }
    
    @Override
    public Serializable get() {return data;}
    
    @Override
    public byte type() {return EventConstants.ARY_NUMBER;}
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof PrimativeDoubleArrayWrapper))
            return false;
        
        PrimativeDoubleArrayWrapper pbaw = (PrimativeDoubleArrayWrapper) o;
        
        return Arrays.equals(this.data, pbaw.data);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 73 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
}




