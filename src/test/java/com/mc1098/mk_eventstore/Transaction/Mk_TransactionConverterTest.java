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

import com.mc1098.mk_eventstore.Exception.ParseException;
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
public class Mk_TransactionConverterTest
{
    
    public Mk_TransactionConverterTest()
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
        
        Transaction expResult = new Transaction(TransactionType.PUT_EVENT, 0, 1,
                1, 1, new byte[]{10,20,30});
        
        TransactionConverter parser = new Mk_TransactionConverter();
        
        byte[] bytes = parser.toBytes(expResult);
        Transaction result = parser.parse(ByteBuffer.wrap(bytes));
        
        assertEquals(expResult, result);
        
    }
    
    @Test (expected = ParseException.class)
    public void testParse_NotEnoughBytes() throws ParseException
    {
        System.out.println("parse_MalformedBytes");
        
        TransactionConverter parser = new Mk_TransactionConverter();
        parser.parse(ByteBuffer.wrap(new byte[]{1, 49}));
    }
    
    @Test (expected = ParseException.class)
    public void testParse_InvalidTypeByte() throws ParseException
    {
        System.out.println("parse_MalformedBytes");
        
        TransactionConverter parser = new Mk_TransactionConverter();
        parser.parse(ByteBuffer.wrap(new byte[]{20, 49}));
    }

    
}
