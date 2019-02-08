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
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public interface RelativeFileSystem
{
    /**
     * Returns the relative path of the root directory.
     * The root directory for a RelativeFileSystem is the main directory 
     * when using an implementation of this class. 
     * Any paths given to this FileSystem will be relative to the root directory.
     * 
     * @return Retrieves the relative path of the root directory.
     */
    public String getRootPath();
    
    /**
     * Returns a {@link Path} object with the given path relative to the root 
     * directory.
     * 
     * This method does not check or guarantee the existence of this path on the 
     * current system.
     * @param strings to be joined after the root directory to create a path.
     * @return Retrieves a {@link Path} object with the given path relative to the root 
     * directory.
     * 
     * @see Path
     * @see #getRootPath() 
     */
    public Path getRelativePath(String...strings);
    
    /**
     * Returns a directory File object which is guaranteed to exist at the path 
     * relative to the root directory.
     * This function will either retrieve a directory that already exists or will 
     * attempt to create a new directory. If the directories don't already 
     * exists in the path given then this function will create the required 
     * directories before the target directory.
     * 
     * Directory file objects are file objects which will return true to 
     * the {@link File#isDirectory()} method: 
     * @code File file ...; file.isDirectory() == true
     * 
     * @param strings to be joined after the root directory to create a path.
     * @return Retrieves a directory File object which is guaranteed to exist at
     * the path relative to the root directory
     * @throws FileSystemException If an error occurs when creating the required 
     * directory or dependent parent directories.
     * 
     * @see #getRootPath() 
     * @see #getOrCreateFile(java.lang.String...) 
     * @see #doesFileExist(java.lang.String...) 
     */
    public File getOrCreateDirectory(String...strings) throws FileSystemException;
    
    /**
     * Creates a File object at the path given relative to the root directory.
     * This method will create all the required parent directories required to
     * create this file.
     * If the file already exists and is a file and not a directory then the 
     * method will do nothing.
     * @param strings to be joined after the root directory to create a path.
     * @throws FileSystemException If an error occurs when creating the required
     * file or parent directories. This error is also thrown if a directory exists
     * instead of a file at this path.
     */
    public void createFile(String...strings) throws FileSystemException;
    
    /**
     * Returns a File object which is guaranteed to exist at the path relative 
     * to the root directory.
     * This function will either retrieve a file that already exists or will 
     * attempt to create a new file. If the directories don't already exists in
     * the path given then this function will create the required directories 
     * before the file.
     * 
     * @param strings to be joined after the root directory to create a path.
     * @return Retrieves a File object which is guaranteed to exist at the path 
     * relative to the root directory
     * @throws FileSystemException If an error occurs when creating the required 
     * file or dependent parent directories.
     * 
     * @see #getRootPath() 
     * @see #doesFileExist(java.lang.String...) 
     */
    public File getOrCreateFile(String...strings) throws FileSystemException;
    
    /**
     * Returns true a file exists at the path given relative to the root directory.
     * 
     * @param strings to be joined after the root directory to create a path.
     * @return Retrieves a boolean which is true if a file exists at the path 
     * given relative to the root directory.
     * 
     * @see #getRootPath() 
     * @see #doesFileExist(java.lang.String...) 
     * @see File
     */
    public boolean doesFileExist(String...strings);
    
    /**
     * Returns the bytes read from a file that is at the path given relative to 
     * the root directory.
     * 
     * If the file exists and is empty then the byte array returned will be empty.
     * 
     * @param strings to be joined after the root directory to create a path.
     * @return the bytes read from a file that is at the path given relative to 
     * the root directory.
     * @throws FileSystemException If the file does not exist or there is an 
     * error when accessing or reading the file. 
     * 
     * @see #getRootPath()
     * @see #readAndParse(FileSystem.ByteParser, java.lang.String...) 
     * @see #readAndParseRecursively(FileSystem.ByteParser, java.lang.String...) 
     */
    public byte[] read(String...strings) throws FileSystemException;
    
    /**
     * Return a parsed object from a file that is at the path given relative 
     * to the root directory.
     * @param <T> Generic type of object which is parsed.
     * @param parser Parser used to parse bytes from the given ByteBuffer.
     * @param strings to be joined after the root directory to create a path.
     * @return Retrieves a parsed object from a file that is at the path given 
     * relative to the root directory.
     * @throws FileSystemException If the file does not exist or there is an 
     * error when accessing or reading the file. 
     * @throws ParseException If an error occurs during the parsing of bytes to
     * the object.
     * 
     * @see #getRootPath() 
     * @see #read(java.lang.String...) 
     * @see #readAndParse(FileSystem.ByteParser, java.lang.String...) 
     * @see ByteParser
     */
    public <T> T readAndParse(ByteParser<T> parser, String...strings) throws 
            FileSystemException, ParseException;
    
    /**
     * Returns a List object which contains the parsed objects from a file 
     * that is at the path given relative to the root directory.
     * @param <T> Generic type of object which is parsed to the returning List.
     * @param parser Parser used to parse bytes from the given ByteBuffer.
     * @param strings to be joined after the root directory to create a path.
     * @return a List object which contains the parsed objects from a file 
     * that is at the path given relative to the root directory.
     * @throws FileSystemException If the file does not exist or there is an 
     * error when accessing or reading the file. 
     * @throws ParseException If an error occurs during the parsing of bytes to
     * the object.
     * 
     * @see #getRootPath() 
     * @see #read(java.lang.String...) 
     * @see #readAndParse(FileSystem.ByteParser, java.lang.String...) 
     * @see ByteParser
     */
    public <T> List<T> readAndParseRecursively(ByteParser<T> parser, 
            String...strings) throws FileSystemException, ParseException;
    
    /**
     * Writes the bytes with the given {@link WriteOption} to a file that is at 
     * the path given relative to the root directory.
     * 
     * @param wo WriteOption to be used when writing to the file.
     * @param bytes The bytes to be written to the file.
     * @param strings to be joined after the root directory to create a path.
     * @throws FileSystemException If the file does not exist or there is an 
     * error when accessing or reading the file. 
     * 
     * @see #getRootPath() 
     * @see #serializeAndWrite(FileSystem.WriteOption, FileSystem.ByteSerializer, java.util.List, java.lang.String...)
     * @see WriteOption
     */
    public void write(WriteOption wo, byte[] bytes, String...strings) 
            throws FileSystemException;
    
    /**
     * Serialises the given List of objects using the {@link ByteSerializer} 
     * provided, and compacts the results to a byte array which is written with 
     * the given {@link WriteOption} to a file that is at 
     * the path given relative to the root directory.
     * 
     * Implementations of this function should guarantee an atomic write of 
     * these serialised objects to the file. 
     * 
     * @param <T> Generic type of object which is serialised and written to file.
     * @param wo WriteOption to be used when writing to the file.
     * @param serializer Used to serialise the objects in the List given.
     * @param list List of objects to be serialised before written to file.
     * @param strings to be joined after the root directory to create a path.
     * @throws FileSystemException FileSystemException If the file does not 
     * exist or there is an error when accessing or reading the file. 
     * @throws SerializationException If an error occurs while serialising an 
     * object to bytes. To conform with the above requirements, when this exception 
     * is thrown no writes have taken place and all objects have failed to be written.
     * 
     * @see #getRootPath() 
     * @see #write(FileSystem.WriteOption, byte..., java.lang.String...) 
     * @see ByteSerializer
     */
    public <T> void serializeAndWrite(WriteOption wo, 
            ByteSerializer<T> serializer, List<T> list, 
            String...strings) throws FileSystemException, SerializationException;
}
