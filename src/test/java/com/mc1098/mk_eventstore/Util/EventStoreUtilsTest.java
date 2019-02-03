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
import java.io.Serializable;
import java.time.LocalDateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class EventStoreUtilsTest
{
    
    public EventStoreUtilsTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    @Test
    public void testSerialiseToDeserialise() throws Exception
    {
        System.out.println("serialiseTodeserialise");
        
        Serializable expResult = LocalDateTime.now();
        byte[] bytes = EventStoreUtils.serialise(expResult);
        Serializable result = EventStoreUtils.deserialise(bytes);
        
        assertEquals(expResult, result);
    }
    
    @Test (expected = SerializationException.class)
    public void testSerialisation_Exception() throws SerializationException
    {
        System.out.println("serialisation_Exception");
        
        EventStoreUtils.serialise(new NonSerializableObject());
    }
    
    @Test (expected = SerializationException.class)
    public void testDeserialisation_Exception() throws SerializationException
    {
        System.out.println("deserialisation_Exception");
        
        EventStoreUtils.deserialise(new byte[]{-12, 32,23});
        
    }
    
}


class NonSerializableObject implements Serializable
{
    private final Object obj;
    
    public NonSerializableObject()
    {
        this.obj = new Object();
    }
}
