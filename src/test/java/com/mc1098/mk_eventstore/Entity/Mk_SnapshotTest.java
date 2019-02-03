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
package com.mc1098.mk_eventstore.Entity;

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
public class Mk_SnapshotTest
{
    
    public Mk_SnapshotTest()
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
    public void testGetEntityName()
    {
        System.out.println("getEntityName");
        String expResult = "TestEntity";
        Mk_Snapshot instance = new Mk_Snapshot(expResult, 1, 0, new byte[]{});
        String result = instance.getEntityName();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetEntityId()
    {
        System.out.println("getEntityId");
        
        long expResult = 1L;
        Mk_Snapshot instance = new Mk_Snapshot("TestEntity", expResult, 0, new byte[]{});
        long result = instance.getEntityId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getVersion method, of class Mk_Snapshot.
     */
    @Test
    public void testGetVersion()
    {
        System.out.println("getVersion");
        
        long expResult = 0L;
        Mk_Snapshot instance = new Mk_Snapshot("TestEntity", 1L, expResult, new byte[]{});
        long result = instance.getVersion();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetBytes()
    {
        System.out.println("getBytes");
        
        byte[] expResult = new byte[]{36, 65, 111, 23, 91};
        Mk_Snapshot instance = new Mk_Snapshot("TestEntity", 1L, 0, expResult);
        byte[] result = instance.getBytes();
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testEquals()
    {
        System.out.println("equals");
        Mk_Snapshot snapshot = new Mk_Snapshot("TestEntity", 1L, 0, new byte[]{20, 30, 10});
        Mk_Snapshot snapshot2 = new Mk_Snapshot("TestEntity", 1L, 1, new byte[]{30, 20, 10});
        
        assertEquals(snapshot, snapshot);//sanity check
        assertNotEquals(snapshot, snapshot2);
        assertNotEquals(snapshot, new Object());
    }
    
}
