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
package com.mc1098.mk_eventstore.Page;

import java.nio.ByteBuffer;
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
public class EntityMetaDataTest
{
    
    public EntityMetaDataTest()
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
    public void testGetName()
    {
        System.out.println("getName");
        
        String expResult = "name";
        EntityMetaData instance = new EntityMetaData(expResult, 0, 0);
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetEntity()
    {
        System.out.println("getEntity");
        
        long expResult = 23L;
        EntityMetaData instance = new EntityMetaData(null, expResult, 0);
        long result = instance.getEntity();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetErp()
    {
        System.out.println("getErp");
        
        int expResult = 20;
        EntityMetaData instance = new EntityMetaData(null, 0, expResult);
        int result = instance.getErp();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testEquals()
    {
        System.out.println("equals");
        
        EntityMetaData emd = new EntityMetaData("name", 12, 20);
        EntityMetaData emd2 = new EntityMetaData("test", 12, 20);
        EntityMetaData emd3 = new EntityMetaData("name", 4, 20);
        EntityMetaData emd4 = new EntityMetaData("name", 12, 10);
        
        assertEquals(emd, emd); //santity check
        assertNotEquals(emd, emd2);
        assertNotEquals(emd, emd3);
        assertNotEquals(emd, emd4);
        assertNotEquals(emd, new Object());
    }
    
    @Test 
    public void test_EntityMetaDataConverter() throws Exception
    {
        System.out.println("EntityMetaDataConverter");
        
        EntityMetaData expResult = new EntityMetaData("name", 13, 20);
        byte[] bytes = EntityMetaData.CONVERTER.toBytes(expResult);
        EntityMetaData result = EntityMetaData.CONVERTER.parse(ByteBuffer.wrap(bytes));
        
        assertEquals(expResult, result);
    }
    
}
