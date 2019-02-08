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
import com.mc1098.mk_eventstore.Exception.NoPageFoundException;
import com.mc1098.mk_eventstore.FileSystem.RelativeFileSystem;
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import com.mc1098.mk_eventstore.Util.TouchMap;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.mc1098.mk_eventstore.Event.EventConverter;
import com.mc1098.mk_eventstore.Exception.FileSystem.FileSystemException;
import com.mc1098.mk_eventstore.FileSystem.WriteOption;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_PageDirectory implements PageDirectory
{
    
    public static Mk_PageDirectory setup(RelativeFileSystem rfs,
            EventConverter ef, TransactionPage transactionPage) 
            throws EventStoreException
    {
        
        List<EntityMetaData> emds = rfs
                .readAndParseRecursively(EntityMetaData.PARSER, "ENM");
        
        Map<String, Long> entityNames = new HashMap<>();
        Map<Long, Integer> entityErp = new HashMap<>();
        
        emds.forEach(emd -> 
        {
            entityNames.put(emd.getName(), emd.getEntity());
            entityErp.put(emd.getEntity(), emd.getErp());
        });
        
        return new Mk_PageDirectory(rfs, ef, transactionPage, entityNames,
                entityErp);
        
    }
    
    private final RelativeFileSystem fileSystem;
    private final TouchMap<String, EntityPage> entityPages;
    private EntityPageConverter entityPageConverter;
    private final Map<String, Long> entityNames;
    private final Map<Long, Integer> entityERP;
    private final Map<String, EntityPage> pending;
    private final TransactionPage transactionPage;
    
    protected Mk_PageDirectory(RelativeFileSystem rfs, EventConverter ef, 
            TransactionPage transactionPage, 
            Map<String, Long> entityNames, Map<Long, Integer> entityERP)
    {
        this.fileSystem = rfs;
        this.entityPages = new TouchMap<>();
        this.entityPageConverter = new Mk_EntityPageConverter(this, ef);
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
        
        long entity = entityNames.size() + 1L;
        entityNames.put(entityName, entity);
        return entity;
    }

    @Override
    public int getEPR(long entity) 
    {
        if(entityERP.containsKey(entity))
            return entityERP.get(entity);
        return 20;
    }
    
    @Override
    public EntityPage getEntityPage(long entity, long id) 
            throws EventStoreException
    {
        long pageNo = getMostRecentPageNo(id, entity);
        String path = Paths.get(fileSystem.getRootPath(), Long.toHexString(entity), 
                Long.toHexString(id), Long.toHexString(pageNo)).toString();
        
        if(entityPages.containsKey(path))
            return entityPages.get(path);
        
        return fileSystem.readAndParse(entityPageConverter, Long.toHexString(entity), 
                Long.toHexString(id), Long.toHexString(pageNo));
    }

    private long getMostRecentPageNo(long id, long entity) 
            throws NoPageFoundException
    {
        try
        {
            File file = fileSystem.getOrCreateFile(Long.toHexString(entity), 
                    Long.toHexString(id));
            return file.list().length - 1L;
        } catch(FileSystemException ex)
        {
            throw new NoPageFoundException(ex);
        }
    }
    
    @Override
    public boolean doesPageExist(long entity, long id, long pageNo)
    {
        String path = Paths.get(Long.toHexString(entity), Long.toHexString(id), 
                Long.toHexString(pageNo)).toString();
        
        if(entityPages.containsKey(path))
            return true;
        return fileSystem.doesFileExist(path);
    }
    
    @Override
    public EntityPage getEntityPage(long entity, long id, long pageNo) throws 
            EventStoreException
    {
        String path = Paths.get(Long.toHexString(entity), 
                Long.toHexString(id), Long.toHexString(pageNo)).toString();
        
        if(entityPages.containsKey(path))
            return entityPages.get(path);
        
        return fileSystem.readAndParse(entityPageConverter, path);
    }

    @Override
    public List<EntityPage> getEntityPages(long entity, long id, long pageFrom) throws 
            EventStoreException
    {
        File file = fileSystem.getOrCreateDirectory(Long.toHexString(entity), 
                Long.toHexString(id));
        long files = file.list().length;
        return getEntityPages(entity, id, pageFrom, files);
    }

    @Override
    public List<EntityPage> getEntityPages(long entity, long id, long pageNo, long pageNo1) 
            throws EventStoreException
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
        String path = Paths.get(Long.toHexString(entity), 
                Long.toHexString(id), Long.toHexString(pageNo)).toString();
        EntityPage page = new Mk_EntityPage(pageNo, entity, id, 
                getEPR(entity), snapshot);
        pending.put(path, page);
        return page;
    }

    @Override
    public synchronized EntityPage confirmPendingPage(EntityPage page)
            throws EventStoreException
    {
        String path = Paths.get(Long.toHexString(page.getEntity()), 
                Long.toHexString(page.getEntityId()), 
                Long.toHexString(page.getPageId())).toString();
        
        if(!pending.containsKey(path))
            throw new EventStoreException("An attempt to confirm a pending page "
                    + "when no such page is pending confirmation.");
        
        if(!entityNames.containsKey(page.getSnapshot().getEntityName()))
            newEntity(page);
        
        if(fileSystem.doesFileExist(path))
                throw new EventStoreException(String.format("Cannot confirm "
                        + "pending page as the Entity Page already exists for "
                        + "the path %s.", path));
            
        fileSystem.createFile(path);
        byte[] bytes = entityPageConverter.toBytes(page);
        fileSystem.write(WriteOption.WRITE, bytes, path);
        pending.remove(path);
        entityPages.put(path, page);
        return page;
        
    }

    @Override
    public TransactionPage getTransactionPage() {return transactionPage;}

    private void newEntity(EntityPage page) 
            throws EventStoreException
    {
        String entityName = page.getSnapshot().getEntityName();
        long entity = page.getEntity();
        
        EntityMetaData emd = new EntityMetaData(entityName, entity, 
                getEPR(entity));
        
        byte[] bytes = EntityMetaData.CONVERTER.toBytes(emd);
        fileSystem.write(WriteOption.APPEND, bytes, "ENM");
    }

    @Override
    public EntityPageConverter getEntityPageConverter() {return entityPageConverter;}

    @Override
    public void setEntityPageConverter(EntityPageConverter parser)
    {
        if(parser != null)
            this.entityPageConverter = parser;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Mk_PageDirectory))
            return false;
        
        Mk_PageDirectory pd = (Mk_PageDirectory) o;
        
        return (this.entityPages.equals(pd.entityPages) && 
                this.entityNames.equals(pd.entityNames) && 
                this.entityERP.equals(pd.entityERP) && 
                this.pending.equals(pd.pending) &&
                this.transactionPage.equals(pd.transactionPage));
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.entityPages);
        hash = 97 * hash + Objects.hashCode(this.entityPageConverter);
        hash = 97 * hash + Objects.hashCode(this.entityNames);
        hash = 97 * hash + Objects.hashCode(this.entityERP);
        hash = 97 * hash + Objects.hashCode(this.pending);
        hash = 97 * hash + Objects.hashCode(this.transactionPage);
        return hash;
    }
    
}