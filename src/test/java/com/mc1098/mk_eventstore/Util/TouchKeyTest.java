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
public class TouchKeyTest
{
    
    private static final TouchKey<String> TOUCH_KEY = new TouchKey("first");
    
    public TouchKeyTest()
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
    public void testGet()
    {
        System.out.println("get");
        
        String expResult = "Hello World";
        TouchKey<String> key = new TouchKey(expResult);
        String result = key.get();
        assertEquals(expResult, result);
    }
    
    @Test 
    public void testUpdate_CompareTo() throws InterruptedException
    {
        System.out.println("update");
        
        TouchKey key = new TouchKey("key");
        TouchKey key2 = new TouchKey("key2");
        
        int expResult = 0;
        int result = key.compareTo(key2);
        
        assertEquals("Expect compare to return the two keys equal as initialised"
                + " at very similiar times", expResult, result);
        
        synchronized(this)
        {
            wait(10); //allow some delay for update to time difference.
        }
        
        key.touch();
        result = key2.compareTo(key);
        
        assertTrue("Expect result to be less than 0 after first key in test had the"
                + "touch method called on it after a 10 ms wait.", result < 0);
        
    }
    
    @Test
    public void testCompareTo()
    {
        System.out.println("compareTo");
        
        TouchKey key1 = new TouchKey("key1");
        TouchKey key1Copy = new TouchKey("key1");
        TouchKey key2 = new TouchKey("key2");
        
        int expResult = 0;
        int result = key1.compareTo(key1Copy);
        
        assertEquals("Expect compareTo to always return 0 zero when comparing keys "
                + "of the same value regardless of initialisation time and update "
                + "method usage.", expResult, result);
        
        
        result = TOUCH_KEY.compareTo(key2);
        
        assertTrue("Expecting result to be less than 0 when comparing test key "
                + "against key intialised before all tests.", result < 0);
        
        
        
    }
    
    @Test
    public void testToString()
    {
        System.out.println("toString");
        
        TouchKey key = new TouchKey(20);
        assertEquals("20", key.toString());
    }
    
    @Test
    public void testEquals()
    {
        System.out.println("equals");
        
        TouchKey key = new TouchKey("key");
        TouchKey keyCopy = new TouchKey("key");
        
        assertEquals(key, keyCopy);
        assertNotEquals(TOUCH_KEY, key);
    }
    
    
}
