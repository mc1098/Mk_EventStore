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

import java.util.HashMap;
import java.util.Iterator;
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
public class WrapperEntryTest
{
    
    public WrapperEntryTest()
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
    public void testEquals()
    {
        System.out.println("equals");
        
        Map<TouchKey, TouchValue> map = new HashMap<>();
        TouchKey key = new TouchKey("key1");
        map.put(key, new TouchValue(key, "value1"));
        key = new TouchKey("key2");
        map.put(key, new TouchValue(key, "value2"));
        Iterator<Map.Entry<TouchKey, TouchValue>> itr = map.entrySet().iterator();
        WrapperEntry we1 = new WrapperEntry(itr.next());
        WrapperEntry we2 = new WrapperEntry(itr.next());
        
        assertEquals("Expect the same value to be equal.", we1, we1);
        assertNotEquals("Expect these two Entry objects to not be equal.", we1, we2);
        assertNotEquals(we1, new Object());
        assertEquals("Expected toString to mirror Map.Entry#toString", "key1=value1", we1.toString());
        
        we2.setValue("value34");
        TouchValue result = map.get(key);
        TouchValue expResult = new TouchValue(key, "value34");
        assertEquals(expResult, result);
        
    }
    
    @Test 
    public void testToString()
    {
        System.out.println("toString");
        
        Map<TouchKey, TouchValue> map = new HashMap<>();
        TouchKey key = new TouchKey("key1");
        map.put(key, new TouchValue(key, "value1"));
        Iterator<Map.Entry<TouchKey, TouchValue>> itr = map.entrySet().iterator();
        WrapperEntry we1 = new WrapperEntry(itr.next());
        
        assertEquals("Expected toString to mirror Map.Entry#toString", 
                "key1=value1", we1.toString());
    }
    
    @Test 
    public void testSetValue()
    {
        System.out.println("setValue");
        
        Map<TouchKey, TouchValue> map = new HashMap<>();
        TouchKey key = new TouchKey("key1");
        map.put(key, new TouchValue(key, "value1"));
        Iterator<Map.Entry<TouchKey, TouchValue>> itr = map.entrySet().iterator();
        WrapperEntry we1 = new WrapperEntry(itr.next());
        
        we1.setValue("value34");
        TouchValue result = map.get(key);
        TouchValue expResult = new TouchValue(key, "value34");
        assertEquals(expResult, result);
        
    }
    
    
}
