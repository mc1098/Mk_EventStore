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
package com.mc1098.mk_eventstore.Transaction;

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
public class TransactionTest
{
    
    public TransactionTest()
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
    public void testGetType()
    {
        System.out.println("getType");
        Transaction instance = new Transaction(TransactionType.PUT_EVENT, 0, 0, 
                1, 0, new byte[]{10, 20, 30});
        TransactionType expResult = TransactionType.PUT_EVENT;
        TransactionType result = instance.getType();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPageId()
    {
        System.out.println("getPageId");
        Transaction instance = new Transaction(TransactionType.PUT_EVENT, 0, 0, 
                1, 0, new byte[]{10, 20, 30});
        long expResult = 0L;
        long result = instance.getPageId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetEntity()
    {
        System.out.println("getEntity");
        Transaction instance = new Transaction(TransactionType.PUT_EVENT, 0, 0, 
                1, 0, new byte[]{10, 20, 30});
        long expResult = 0L;
        long result = instance.getEntity();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetEntityId()
    {
        System.out.println("getEntityId");
        Transaction instance = new Transaction(TransactionType.PUT_EVENT, 0, 0, 
                1, 0, new byte[]{10, 20, 30});
        long expResult = 1L;
        long result = instance.getEntityId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetVersion()
    {
        System.out.println("getVersion");
        Transaction instance = new Transaction(TransactionType.PUT_EVENT, 0, 0, 
                1, 0, new byte[]{10, 20, 30});
        long expResult = 0L;
        long result = instance.getVersion();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetData()
    {
        System.out.println("getData");
        Transaction instance = new Transaction(TransactionType.PUT_EVENT, 0, 0, 
                1, 0, new byte[]{10, 20, 30});
        byte[] expResult = new byte[]{10, 20, 30};
        byte[] result = instance.getData();
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testEquals()
    {
        System.out.println("equals");
        
        Transaction transaction1 = new Transaction(TransactionType.PUT_EVENT, 0, 0, 
                1, 0, new byte[]{10, 20, 30});
        Transaction transaction2 = new Transaction(TransactionType.PUT_SNAPSHOT, 0, 0, 
                1, 0, new byte[]{10, 20, 30});
        Transaction transaction3 = new Transaction(TransactionType.PUT_EVENT, 1, 0, 
                1, 0, new byte[]{10, 20, 30});
        Transaction transaction4 = new Transaction(TransactionType.PUT_EVENT, 0, 1, 
                1, 0, new byte[]{10, 20, 30});
        Transaction transaction5 = new Transaction(TransactionType.PUT_EVENT, 0, 0, 
                23, 0, new byte[]{10, 20, 30});
        Transaction transaction6 = new Transaction(TransactionType.PUT_EVENT, 0, 0, 
                1, 1, new byte[]{10, 20, 30});
        Transaction transaction7 = new Transaction(TransactionType.PUT_EVENT, 0, 0, 
                1, 0, new byte[]{10, 10, 10});
        
        assertEquals(transaction1, transaction1); //sanity check
        assertNotEquals(transaction1, transaction2);
        assertNotEquals(transaction1, transaction3);
        assertNotEquals(transaction1, transaction4);
        assertNotEquals(transaction1, transaction5);
        assertNotEquals(transaction1, transaction6);
        assertNotEquals(transaction1, transaction7);
        assertNotEquals(transaction1, new Object());
    }
    
}
