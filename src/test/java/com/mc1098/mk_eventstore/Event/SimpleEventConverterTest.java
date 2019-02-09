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
import com.mc1098.mk_eventstore.Exception.ParseException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
public class SimpleEventConverterTest
{
    
    public SimpleEventConverterTest()
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
    public void testToBytesAndParse() throws Exception
    {
        System.out.println("toBytesAndParse");
        Event event = new Mk_Event("name", "tName", 1, 0, LocalDateTime.now(), 
                new HashMap<String, Serializable>(){{put("value", 20);}});
        SimpleEventConverter instance = new SimpleEventConverter();
        byte[] bytes = instance.toBytes(event);
        
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + bytes.length);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.rewind();
        Event result = instance.parse(buffer);
        
        assertEquals(event.getName(), result.getName());
        assertEquals(event.getTargetEntityName(), result.getTargetEntityName());
        assertEquals(event.getTargetEntityId(), result.getTargetEntityId());
        assertEquals((int)event.getValue("value"), (int)result.getValue("value"));
    }
    
    @Test (expected = SerializationException.class)
    public void testToBytes_Exception() throws SerializationException
    {
        System.out.println("toBytes_Exception");
        
        Map map = new HashMap();
        map.put("keyToUnserializableObject", new Object());
        Event e = new Mk_Event("TestEvent", "TestEntity", 1, 1, 
                LocalDateTime.now(), map); //java erasure allows this illegal generic
        SimpleEventConverter sef = new SimpleEventConverter();
        sef.toBytes(e);
        
    }
    
    @Test (expected = ParseException.class)
    public void testParse_Exception() throws ParseException
    {
        System.out.println("parse_Exception");
        
        SimpleEventConverter sef = new SimpleEventConverter();
        
        byte[] bytes = new byte[]{23, 61, 122};
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + bytes.length);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.rewind();
        sef.parse(buffer);
    }

    
}
