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

import com.mc1098.mk_eventstore.Exception.FileSystem.DirectoryCreationException;
import com.mc1098.mk_eventstore.Exception.FileSystem.FileCreationException;
import com.mc1098.mk_eventstore.Exception.FileSystem.FileSystemException;
import com.mc1098.mk_eventstore.Exception.ParseException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class PageFileSystem implements RelativeFileSystem
{
    public static PageFileSystem ofRoot(Path path) throws FileSystemException
    {
        if(path.isAbsolute())
            throw new FileSystemException("Root paths should only be relative.");
        File file = path.toFile();
        try
        {
            boolean created = file.mkdir();
            if(created)
                LOGGER.log(Level.FINEST, "Directory {0} created at path {1} as the root directory for PageFileSystem.", 
                        new Object[]{file.getName(), file.getPath()});
            
            return new PageFileSystem(file);
            
        } catch(SecurityException ex)
        {
            throw new DirectoryCreationException(file, ex);
        }
        
    }
    
    private final static Logger LOGGER = Logger.getLogger(PageFileSystem.class.getName());
    
    private final String root;
    
    protected PageFileSystem(File root)
    {
        this.root = root.getPath();
    }
    
    @Override
    public String getRootPath() {return root;}

    @Override
    public File getOrCreateFile(String...strings) throws FileSystemException
    {
        Path path = Paths.get(root, strings);
        File file = path.toFile();
        
        if(file.getParentFile().mkdirs())
            LOGGER.log(Level.FINEST, "Created directories to form path {0}.", 
                    file.getPath());
        try
        {
            if(!file.createNewFile())
                LOGGER.log(Level.FINEST, "Created file {0} at path {1}.",
                        new Object[]{file.getName(), file.getPath()});
            
            return file;
        } catch(IOException ex)
        {
            throw new FileCreationException(file, ex);
        }
            
    }
    
    @Override
    public boolean doesFileExist(String... strings)
    {
        return Paths.get(root, strings).toFile().exists();
    }

    @Override
    public byte[] read(String... strings) throws FileSystemException
    {
        Path path = Paths.get(root, strings);
        return readToBuffer(path).array();
    }

    @Override
    public <T> List<T> readAndParseRecursively(ByteParser<T> parser, 
            String... strings) throws FileSystemException, ParseException
    {
        Path path = Paths.get(root, strings);
        ByteBuffer buffer = readToBuffer(path);
        List<T> list = new ArrayList<>();
        while(buffer.hasRemaining())
            list.add(parser.fromBytes(buffer));
            
        return list;
    }
    
    private ByteBuffer readToBuffer(Path path) throws FileSystemException
    {
        try(FileChannel fc = FileChannel.open(path))
        {
            int size = (int) fc.size();
            ByteBuffer buffer = ByteBuffer.allocate(size);
            fc.read(buffer);
            buffer.rewind();
            return buffer;
        } catch(IOException ex) {throw new FileSystemException(ex);}
    }

    @Override
    public void write(WriteOption wo, byte[] bytes, String... strings) 
            throws FileSystemException
    {
        Path path = Paths.get(root, strings);
        try(FileChannel fc = FileChannel.open(path, wo.option()))
        {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            fc.write(buffer);
        }catch(IOException ex)
        {
            throw new FileSystemException(ex);
        }
    }

    @Override
    public <T> void serializeAndWrite(WriteOption wo, 
            ByteSerializer<T> serializer, 
            List<T> cols, String... strings) 
            throws FileSystemException, SerializationException
    {
        Path path = Paths.get(root, strings);
        
        byte[][] colBytes = new byte[cols.size()][0];
        int size = 0;
        for (int i = 0; i < cols.size(); i++)
        {
            byte[] bytes = serializer.toBytes(cols.get(i));
            size+= bytes.length;
            colBytes[i] = bytes;
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(size);
        for (byte[] bytes : colBytes)
            buffer.put(bytes);
        
        buffer.rewind();
        
        writeFromBuffer(path, buffer, wo);
    }

    private void writeFromBuffer(Path path, ByteBuffer buffer, WriteOption wo) 
            throws FileSystemException
    {
        try(FileChannel fc = FileChannel.open(path, wo.option()))
        {
            fc.write(buffer);
        } catch(IOException ex)
        {
            throw new FileSystemException(ex);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof PageFileSystem))
            return false;
        
        PageFileSystem pfs = (PageFileSystem) o;
        
        return this.root.equals(pfs.root);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.root);
        return hash;
    }

    
    
}