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

import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Event.EventFormat;
import com.mc1098.mk_eventstore.Exception.ParseException;
import com.mc1098.mk_eventstore.Exception.NoPageFoundException;
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import com.mc1098.mk_eventstore.Util.TouchMap;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_PageDirectory implements PageDirectory
{
    
    public static Mk_PageDirectory setup(EventFormat ef, TransactionPage transactionPage) 
            throws EventStoreException
    {
        File enm = new File("Entity/ENM");
        
        Map<String, Long> entityNames = new HashMap<>();
        Map<Long, Integer> entityErp = new HashMap<>();
        
        try(FileChannel fc = FileChannel.open(enm.toPath(), StandardOpenOption.READ))
        {
            ByteBuffer buffer = ByteBuffer.allocate((int)fc.size());
            while(fc.read(buffer) > 0){};
            buffer.rewind();
            while (buffer.hasRemaining())
            {                
                long entity = buffer.getLong();
                int erp = buffer.getInt();
                int nameSize = buffer.getInt();
                byte[] nameBytes = new byte[nameSize];
                buffer.get(nameBytes);
                String name = new String(nameBytes, StandardCharsets.UTF_8);
                
                entityErp.put(entity, erp);
                entityNames.put(name, entity);
            }
            
            return new Mk_PageDirectory(ef, transactionPage, entityNames, 
                    entityErp);
            
        } catch(IOException ex)
        {
            throw new EventStoreException(ex);
        }
    }
    
    private final TouchMap<String, EntityPage> entityPages;
    private EntityPageParser entityPageParser;
    private final Map<String, Long> entityNames;
    private final Map<Long, Integer> entityERP;
    private final Map<String, EntityPage> pending;
    private final TransactionPage transactionPage;
    
    private Mk_PageDirectory(EventFormat ef, 
            TransactionPage transactionPage, 
            Map<String, Long> entityNames, Map<Long, Integer> entityERP)
    {
        this.entityPages = new TouchMap<>();
        this.entityPageParser = new Mk_EntityPageParser(this, ef);
        this.entityNames = entityNames;
        this.entityERP = entityERP;
        this.pending = new HashMap<>();
        this.transactionPage = transactionPage;
    }
    
    @Override
    public String getEntityName(long entity)
    {
        for (Map.Entry<String, Long> entry : entityNames.entrySet())
            if(entry.getValue() == entity)
                return entry.getKey();
        return null;
    }
    
    @Override
    public boolean hasEntity(String entityName)
    {
        return entityNames.containsKey(entityName);
    }

    @Override
    public long getEntity(String entityName) 
    {
        if(entityNames.containsKey(entityName))
            return entityNames.get(entityName);
        
        long entity = entityNames.size()+1;
        entityNames.put(entityName, entity);
        return entity;
    }

    @Override
    public int getEPR(long entity) 
    {
        if(entityERP.containsKey(entity))
            entityERP.get(entity);
        return 20;
    }
    
    @Override
    public EntityPage getEntityPage(long entity, long id) 
            throws EventStoreException, NoPageFoundException
    {
        long pageNo = getMostRecentPageNo(id, entity);
        String path = getRelativePath(Long.toHexString(pageNo), "Entity", 
                Long.toHexString(entity), Long.toHexString(id));
        
        if(entityPages.containsKey(path))
            return entityPages.get(path);
        
        File file = getRelativeFile(Long.toHexString(pageNo), "Entity", 
                Long.toHexString(entity), Long.toHexString(id));
        
        return getPageFromFile(file);
    }

    private long getMostRecentPageNo(long id, long entity) 
            throws NoPageFoundException
    {
        File file = getRelativeFile(Long.toHexString(id), "Entity",
                Long.toHexString(entity));
        return file.list().length-1;
    }
    
    private EntityPage getPageFromFile(File file) throws EventStoreException
    {
        try(FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.READ))
        {
            ByteBuffer buffer = ByteBuffer.allocate((int)fc.size());
            fc.read(buffer);
            buffer.rewind();
            EntityPage page = entityPageParser.parse(buffer);
            entityPages.put(file.getPath(), page);
            return page;
        } catch(IOException | ParseException ex)
        {
            throw new EventStoreException(ex);
        }
    }
    
    private File getRelativeFile(String fileName, String...dirs) 
            throws NoPageFoundException
    {
        String path = getRelativePath(fileName, dirs);
        
        File file = new File(path);
        if(!file.exists())
            throw new NoPageFoundException(String.format("No such file found "
                    + "with path %s", path));
        
        return file;
        
    }

    private String getRelativePath(String fileName, String...dirs)
    {
        StringBuilder sb = new StringBuilder("");
        for (String dir : dirs)
            sb.append(String.format("%s/", dir));
        sb.append(fileName);
        return sb.toString();
    }
    
    @Override
    public boolean doesPageExist(long entity, long id, long pageNo)
    {
        String path = getRelativePath(Long.toHexString(pageNo), "Entity",
                Long.toHexString(entity), Long.toHexString(id));
        
        if(entityPages.containsKey(path))
            return true;
        return new File(path).exists();
    }
    
    @Override
    public EntityPage getEntityPage(long entity, long id, long pageNo) throws 
            EventStoreException, NoPageFoundException
    {
        File file = getRelativeFile(Long.toHexString(pageNo), "Entity", 
                Long.toHexString(entity), Long.toHexString(id));
        
        if(entityPages.containsKey(file.getPath()))
            return entityPages.get(file.getPath());
        
        return getPageFromFile(file);
    }

    @Override
    public List<EntityPage> getEntityPages(long entity, long id, long pageFrom) throws 
            EventStoreException, NoPageFoundException
    {
        File file = getRelativeFile(Long.toHexString(id), "Entity", 
                Long.toHexString(entity));
        long files = file.list().length;
        
        return getEntityPages(entity, id, pageFrom, files);
    }

    @Override
    public List<EntityPage> getEntityPages(long entity, long id, long pageNo, long pageNo1) 
            throws EventStoreException, NoPageFoundException
    {
        List<EntityPage> pages = new ArrayList<>();
        
        for (long i = pageNo; i < pageNo1; i++)
            pages.add(getEntityPage(entity, id, i));
        return pages;
    }

    @Override
    public EntityPage createPendingEntityPage(long entity, long id, 
            long pageNo, Snapshot snapshot)
    {
        String path = getRelativePath(Long.toHexString(pageNo), "Entity",
                Long.toHexString(entity), Long.toHexString(id));
        EntityPage page = new Mk_EntityPage(pageNo, entity, id, 
                getEPR(entity), snapshot);
        pending.put(path, page);
        return page;
    }

    @Override
    public synchronized EntityPage confirmPendingPage(EntityPage page)
            throws EventStoreException
    {
        String entityPath = getRelativePath(Long.toHexString(page.getEntity()), 
                "Entity");
        
        String path = getRelativePath(Long.toHexString(page.getPageId()), 
                "Entity", Long.toHexString(page.getEntity()), 
                Long.toHexString(page.getEntityId()));
        
        if(!pending.containsKey(path))
            throw new EventStoreException("An attempt to confirm a pending page "
                    + "when no such page is pending confirmation.");
        
        try
        {
            File entityFile = new File(entityPath);
            if(!entityFile.exists())
                newEntity(entityFile, page.getEntity(), 
                        page.getSnapshot().getEntityName());
            
            File file = new File(path);
            if(file.exists())
                throw new EventStoreException(String.format("Cannot confirm "
                        + "pending page as the Entity Page already exists for "
                        + "the path %s.", path));
            
            file.getParentFile().mkdirs();
            file.createNewFile();
            
            try(FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.WRITE))
            {
                fc.write(ByteBuffer.wrap(entityPageParser.toBytes(page)));
            }
            
        }catch(IOException ex)
        {
            throw new EventStoreException(ex);
        }
        pending.remove(path);
        entityPages.put(path, page);
        return page;
        
    }

    @Override
    public TransactionPage getTransactionPage() {return transactionPage;}

    private void newEntity(File file, long entity, String entityName) 
            throws EventStoreException
    {
        int size = Long.BYTES + (Integer.BYTES * 2);
        byte[] nameBytes = entityName.getBytes(StandardCharsets.UTF_8);
        size+= nameBytes.length;
        
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putLong(entity);
        buffer.putInt(20);
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);
        buffer.rewind();
        
        file.mkdir();
        String enmPath = getRelativePath("ENM", "Entity");
        File enm = new File(enmPath);
        try(FileChannel fc = FileChannel.open(enm.toPath(), StandardOpenOption.APPEND))
        {
            fc.write(buffer);
        } catch(IOException ex)
        {
            throw new EventStoreException(ex);
        }
    }

    @Override
    public EntityPageParser getEntityPageParser() {return entityPageParser;}

    @Override
    public void setEntityPageParser(EntityPageParser parser)
    {
        if(parser != null)
            this.entityPageParser = parser;
    }
    
}