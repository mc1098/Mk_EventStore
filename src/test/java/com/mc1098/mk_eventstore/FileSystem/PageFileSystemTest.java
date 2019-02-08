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
package com.mc1098.mk_eventstore.FileSystem;

import com.mc1098.mk_eventstore.Exception.FileSystem.FileSystemException;
import com.mc1098.mk_eventstore.Exception.ParseException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class PageFileSystemTest
{
    
    private String root;
    
    public PageFileSystemTest()
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
    public void setUp() throws Exception
    {
        root = "Entity";
    }
    
    @After
    public void tearDown()
    {
        File file = new File(root);
        if(file.exists())
        {
            for (File f : file.listFiles())
            {
                if(f.isDirectory())
                    for (File f2 : f.listFiles())
                        f2.delete();
                f.delete();
            }
            file.delete();
        }
    }

    @Test
    public void testOfRoot() throws Exception
    {
        System.out.println("ofRoot");
        Path path = Paths.get(root);
        PageFileSystem result = PageFileSystem.ofRoot(path);
        PageFileSystem expResult = new PageFileSystem(new File(root));
        assertEquals(expResult, result);
    }
    
    @Test(expected = FileSystemException.class)
    public void testOfRoot_AbsolutePath() throws Exception
    {
        System.out.println("ofRoot_AbsolutePath");
        Path path = Paths.get(root).toAbsolutePath();
        PageFileSystem result = PageFileSystem.ofRoot(path);
    }
    
    @Test
    public void testGetRootPath() throws Exception
    {
        System.out.println("getRootPath");
        Path path = Paths.get(root);
        String expResult = path.toString();
        PageFileSystem instance = PageFileSystem.ofRoot(path);
        String result = instance.getRootPath();
        assertEquals(expResult, result);
    }
    
    @Test 
    public void testGetRelativePath() throws Exception
    {
        System.out.println("getRelativePath");
        Path path = Paths.get(root);
        Path expResult = Paths.get(root, "test");
        PageFileSystem instance = PageFileSystem.ofRoot(path);
        Path result = instance.getRelativePath("test");
        
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetOrCreateDirectory() throws Exception 
    {
        System.out.println("getOrCreateDirectory");
        
        Path path = Paths.get(root);
        File expResult = Paths.get(root, "test", "child").toFile();
        PageFileSystem instance = PageFileSystem.ofRoot(path);
        File result = instance.getOrCreateDirectory("test", "child");
        
        assertEquals(expResult, result);
    }
    
    @Test (expected = FileSystemException.class)
    public void testGetOrCreateDirectory_WhenFileNotADirectoryExists() 
            throws Exception
    {
        System.out.println("getOrCreateDirectory_WhenFileNotADirectoryExists");
        
        Path path = Paths.get(root);
        File expResult = Paths.get(root, "test", "child").toFile();
        expResult.getParentFile().mkdirs();
        expResult.createNewFile();
        PageFileSystem instance = PageFileSystem.ofRoot(path);
        instance.getOrCreateDirectory("test", "child");
    }
    
    @Test
    public void testCreateFile() throws Exception
    {
        System.out.println("createFile");
        
        Path path = Paths.get(root);
        File file = Paths.get(root, "test").toFile();
        
        PageFileSystem instance = PageFileSystem.ofRoot(path);
        instance.createFile("test");
        assertTrue(file.exists());
        assertTrue(file.isFile());
        
    }

    @Test
    public void testGetOrCreateFile() throws Exception
    {
        System.out.println("getOrCreateFile");
        Path path = Paths.get(root);
        RelativeFileSystem instance = PageFileSystem.ofRoot(path);
        String filePath = "test";
        File result = instance.getOrCreateFile(filePath);
        File expResult = Paths.get(root, "test").toFile();
        
        assertEquals(expResult, result);
        assertTrue(result.exists());
    }

    @Test
    public void testDoesFileExist() throws Exception
    {
        System.out.println("doesFileExist");
        String path = "test";
        PageFileSystem instance = PageFileSystem.ofRoot(Paths.get(root));
        boolean expResult = false;
        boolean result = instance.doesFileExist(path);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testDoesFileExist_FileCreatedBefore() throws Exception
    {
        System.out.println("doesFileExist");
        String path = "test";
        File file = new File(root + "/test");
        file.getParentFile().mkdir();
        file.createNewFile();
        PageFileSystem instance = PageFileSystem.ofRoot(Paths.get(root));
        boolean expResult = true;
        boolean result = instance.doesFileExist(path);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testWriteAndRead() throws Exception
    {
        System.out.println("writeAndRead");
        
        PageFileSystem instance = PageFileSystem.ofRoot(Paths.get(root));
        String path = "test";
        File file = Paths.get(root, path).toFile();
        file.getParentFile().mkdir();
        file.createNewFile();
        
        byte[] expResult = new byte[]{20, 30};
        instance.write(WriteOption.WRITE, expResult, "test");
        byte[] result = instance.read(path);
        
        assertArrayEquals(expResult, result);
    }
    
    @Test
    public void testRead_EmptyFile() throws Exception 
    {
        System.out.println("read_EmptyFile");
        
        PageFileSystem instance = PageFileSystem.ofRoot(Paths.get(root));
        String path = "test";
        File file = Paths.get(root, path).toFile();
        file.getParentFile().mkdir();
        file.createNewFile();
        
        byte[] result = instance.read(path);
        
        assertArrayEquals(new byte[0], result);
    }
    
    @Test(expected = FileSystemException.class)
    public void testWriteAndRead_NoExistingFile() throws Exception
    {
        System.out.println("writeAndRead_NoExistingFile");
        
        PageFileSystem instance = PageFileSystem.ofRoot(Paths.get(root));
        byte[] expResult = new byte[]{20, 30};
        instance.write(WriteOption.WRITE, expResult, "test");
    }
    
    @Test(expected = FileSystemException.class)
    public void testWriteAndRead_DeleteExistingFileAfterWrite() throws Exception
    {
        System.out.println("writeAndRead_DeleteExistingFileAfterWrite");
        
        PageFileSystem instance = PageFileSystem.ofRoot(Paths.get(root));
        String path = "test";
        File file = Paths.get(root, path).toFile();
        file.getParentFile().mkdir();
        file.createNewFile();
        
        byte[] expResult = new byte[]{20, 30};
        instance.write(WriteOption.WRITE, expResult, "test");
        file.delete();
        byte[] result = instance.read(path);
    }
    
    @Test 
    public void testSerializeAndWriteAndReadAndParse() throws Exception
    {
        System.out.println("serializeAndWriteAndReadAndParse");
        
        PageFileSystem instance = PageFileSystem.ofRoot(Paths.get(root));
        String path = "test";
        File file = Paths.get(root, path).toFile();
        file.getParentFile().mkdir();
        file.createNewFile();
       
        List<Byte> expResult = Arrays.asList(new Byte[]{10, 20});
        instance.serializeAndWrite(WriteOption.WRITE, (Byte t) -> new byte[]{t},
                expResult, "test");
        List<Byte> result = instance.readAndParseRecursively((ByteBuffer b) -> b.get(), path);
        
        assertEquals(expResult, result);
    }
    
    @Test(expected = SerializationException.class) 
    public void testSerializeAndWriteAndReadAndParse_SerializationExceptionHandling()
            throws Exception
    {
        System.out.println("serializeAndWriteAndReadAndParse_SerializationExceptionHandling");
        
        PageFileSystem instance = PageFileSystem.ofRoot(Paths.get(root));
        String path = "test";
        File file = Paths.get(root, path).toFile();
        file.getParentFile().mkdir();
        file.createNewFile();
       
        List<Byte> expResult = Arrays.asList(new Byte[]{10, 20});
        instance.serializeAndWrite(WriteOption.WRITE, 
                new ExceptionThrowingSerializer(), expResult, "test");
    }
    
    @Test(expected = ParseException.class)
    public void testSerializeAndWriteAndReadAndParse_ParseExceptionHandling() throws Exception
    {
        System.out.println("serializeAndWriteAndReadAndParse_ParseExceptionHandling");
        
        PageFileSystem instance = PageFileSystem.ofRoot(Paths.get(root));
        String path = "test";
        File file = Paths.get(root, path).toFile();
        file.getParentFile().mkdir();
        file.createNewFile();
       
        List<Byte> expResult = Arrays.asList(new Byte[]{10, 20});
        instance.serializeAndWrite(WriteOption.WRITE, (Byte t) -> new byte[]{t},
                expResult, "test");
        List<Byte> result = instance
                .readAndParseRecursively(new ExceptionThrowingParser(), path);
        
        assertEquals(expResult, result);
    }
    
    @Test
    public void testReadAndParse_EmptyFile() throws Exception
    {
        System.out.println("readAndParse_EmptyFile");
        
        PageFileSystem instance = PageFileSystem.ofRoot(Paths.get(root));
        String path = "test";
        File file = Paths.get(root, path).toFile();
        file.getParentFile().mkdir();
        file.createNewFile();
        List<Byte> result = instance.readAndParseRecursively((ByteBuffer b) -> b.get(), path);
        
        assertEquals(new ArrayList<>(), result);
    }
    
    @Test
    public void testEquals() throws Exception
    {
        System.out.println("equals");
        
        PageFileSystem pfs = new PageFileSystem(new File(root));
        PageFileSystem pfs2 = new PageFileSystem(new File("test"));
        
        assertEquals(pfs, pfs); //sanity check
        assertNotEquals(pfs, pfs2);
        assertNotEquals(pfs, new Object());
    }
    
    class ExceptionThrowingSerializer implements ByteSerializer<Byte>
    {

        @Override
        public byte[] toBytes(Byte t) throws SerializationException
        {
            throw new SerializationException("Intentional exception for testing");
        }
        
    }
    
    class ExceptionThrowingParser implements ByteParser<Byte>
    {

        @Override
        public Byte parse(ByteBuffer buffer) throws ParseException
        {
            throw new ParseException("Intentional exception for testing");
        }
        
    }
    
}
