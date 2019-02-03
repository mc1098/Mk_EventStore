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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
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
public class TouchMapTest
{
    
    public TouchMapTest()
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
    public void testLastValue_Put()
    {
        System.out.println("lastValue_put");
        TouchMap<String, Integer> instance = new TouchMap<>();
        Integer expResult = 2342;
        instance.put("value1", 2342);
        instance.put("value2", 25);
        Integer result = instance.lastValue();
        assertEquals("Expect first value to be added to the instance to be the "
                + "value from lastValue method.", expResult, result);
        
        instance.get("value1"); //touch value1
        expResult = 25;
        result = instance.lastValue();
        
        assertEquals("", expResult, result);
        
        
    }
    
    @Test(expected = NullPointerException.class)
    public void testLastValue_OnEmptyMap()
    {
        System.out.println("lastValue_OnEmptyMap");
        TouchMap map = new TouchMap();
        map.lastValue();
    }

    @Test
    public void testSize_Put_PutAll_Remove_Clear()
    {
        System.out.println("size_put_putAll_remove");
        TouchMap<Long, String> instance = new TouchMap<>();
        int result = instance.size();
        assertEquals("Expect size of 0 on a new instance of FloatingOrderedMap.",0, result);
        
        instance.put(1L, "value1");
        result = instance.size();
        assertEquals("Expect size 1 after one put value.", 1, result);
        
        Map<Long, String> map = new HashMap<>();
        map.put(2L, "value2");
        map.put(3L, "value3");
        instance.putAll(map);
        result = instance.size();
        assertEquals("Expect size of 3 after one single put and put all with a"
                + " map (size 2).", 3, result);
        
        instance.remove(2L);
        result = instance.size();
        assertEquals("Expect size of 2 after one single put and put all with a "
                + "map (size 2), then a removal of one value.", 2, result);
        
        instance.clear();
        result = instance.size();
        assertEquals("Expect size of 0 after one single put and put all with a "
                + "map (size 2), then remove one value and finally a clear",
                0, result);
        
        
        
    }

    @Test
    public void testIsEmpty_Put()
    {
        System.out.println("isEmpty_put");
        TouchMap instance = new TouchMap();
        boolean result = instance.isEmpty();
        assertEquals("Expect this map to be empty on new instance.", true, result);
        
        instance.put(1L, "V1");
        result = instance.isEmpty();
        assertEquals("Expect map not to be empty after putting in a value.", false, result);
        
    }

    @Test
    public void testContainsKey_Put()
    {
        System.out.println("containsKey_put");
        Integer key = 333;
        TouchMap instance = new TouchMap();
        boolean result = instance.containsKey(key);
        assertEquals("Expect any call to containsKey to return false on a new "
                + "instance.", false, result);
        
        instance.put(key, "v1");
        result = instance.containsKey(key);
        assertEquals(String.format("Expect this map to contain key %s after "
                + "putting this key into the map.", key), true, result);
        
    }

    @Test
    public void testContainsValue_Put()
    {
        System.out.println("containsValue_put");
        Long value = 53482L;
        TouchMap instance = new TouchMap();
        boolean result = instance.containsValue(value);
        assertEquals("Expect any call to containValue on a new instance to "
                + "return false", false, result);
        
        instance.put("key", value);
        result = instance.containsValue(value);
        assertEquals(String.format("Expect this map to contain value %s after"
                + " putting this value into the map", value), true, result);
    }

    @Test
    public void testGet_Put()
    {
        System.out.println("get_put");
        LocalDateTime key = LocalDateTime.now();
        Object expResult = "Testing Time";
        TouchMap instance = new TouchMap();
        instance.put(key, expResult);
        Object result = instance.get(key);
        assertEquals(expResult, result);
    }

    @Test
    public void testKeySet_Put()
    {
        System.out.println("keySet_put");
        
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        Set expResult = new HashSet(Arrays.asList(key1, key2, key3));
        
        TouchMap instance = new TouchMap();
        instance.put(key1, "v1");
        instance.put(key2, "v2");
        instance.put(key3, "v3");
        
        Set result = instance.keySet();
        assertEquals(expResult, result);
    }

    @Test
    public void testValues_Put()
    {
        System.out.println("values_put");
        
        long value1 = 8;
        long value2 = 16;
        long value3 = 32;
        Collection expResult = Arrays.asList(value1, value2, value3);
        
        TouchMap instance = new TouchMap();
        instance.put("key1", value1);
        instance.put("key2", value2);
        instance.put("key3", value3);
       
        Collection result = instance.values();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testEntrySet_Put()
    {
        System.out.println("entrySet_put");
        
        TouchMap instance = new TouchMap();
        instance.put("key1", "value1");
        instance.put("key2", "value2");
        
        Map map = new HashMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        
        
        Set<Map.Entry> expResult = map.entrySet();
        Set<Map.Entry> result = instance.entrySet();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetOrDefault_Put()
    {
        System.out.println("getOrDefault_put");
        Object key = 1L;
        Object v = "default";
        Object expResult = "result";
        
        TouchMap instance = new TouchMap();
        Object result = instance.getOrDefault(key, v);
        assertEquals("Expect default value to be returned on new instance.", 
                v, result);
        
        instance.put(key, expResult);
        result = instance.getOrDefault(key, v);
        assertEquals(String.format("Expect result of %s after putting this "
                + "value.", expResult), expResult, result);
        
    }

    @Test
    public void testForEach_Put()
    {
        System.out.println("forEach_put");
        TouchMap instance = new TouchMap();
        instance.put("key1", "value1");
        instance.put("key2", "value2");
        instance.put("key3", "value3");
        
        Map map = new HashMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        
        instance.forEach((t, u) ->
        {
            assertTrue("Expect key to be found in other map.", map.containsKey(t));
            assertTrue("Expect value to be found in other map.", map.containsValue(u));
        });
    }
    
}
