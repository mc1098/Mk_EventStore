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
public class TouchValueTest
{
    
    public TouchValueTest()
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
        
        TouchKeyDummy key = new TouchKeyDummy();
        String expResult = "value";
        TouchValue value = new TouchValue(key, expResult);
        Object result = value.get();
        
        assertEquals(expResult, result);
        assertTrue("Expect get method to also call touch method on key.", 
                key.wasTouchCalled);
    }
    
    @Test 
    public void testPeek()
    {
        System.out.println("peek");
        
        TouchKeyDummy key = new TouchKeyDummy();
        String expResult = "value";
        TouchValue value = new TouchValue(key, expResult);
        Object result = value.peek();
        
        assertEquals(expResult, result);
        assertFalse("Expect peek method not to call touch method on key.", 
                key.wasTouchCalled);
    }
    
    @Test 
    public void testToString()
    {
        System.out.println("toString");
        
        TouchValue value = new TouchValue(new TouchKeyDummy(), "value");
        assertEquals("value", value.toString());
        
    }
    
    @Test
    public void testEquals()
    {
        System.out.println("equals");
        
        TouchValue value = new TouchValue(new TouchKeyDummy(), "value");
        TouchValue valueCopy = new TouchValue(new TouchKeyDummy(), "value");
        TouchValue value2 = new TouchValue(new TouchKeyDummy(), "value2");
        
        //expect equal irrespective of what key.
        assertEquals(value, valueCopy);
        assertNotEquals(value2, value);
    }
    
    
}

class TouchKeyDummy extends TouchKey<Object>
{
    public boolean wasTouchCalled;
    
    public TouchKeyDummy()
    {
        super(null);
    }
    
    @Override
    public void touch() {wasTouchCalled = true;}
    
}