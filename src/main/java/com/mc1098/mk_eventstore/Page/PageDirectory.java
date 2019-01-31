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
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import java.util.List;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public interface PageDirectory
{
    public String getEntityName(long entity);
    public long getEntity(String entityName);
    public boolean hasEntity(String entityName);
    public int getEPR(long entity);
    public boolean doesPageExist(long entity, long entityId, long pageNo);
    public EntityPage getEntityPage(long entity, long id) 
            throws EventStoreException, NoPageFoundException;
    public EntityPage getEntityPage(long entity, long id, long pageNo) 
            throws EventStoreException, NoPageFoundException;
    public List<EntityPage> getEntityPages(long entity, long id, long pageFrom) 
            throws EventStoreException, NoPageFoundException;
    public List<EntityPage> getEntityPages(long entity, long id, long pageNo, long pageNo1) 
            throws EventStoreException, NoPageFoundException;
    public EntityPage createPendingEntityPage(long entity, long id, long pageNo, Snapshot snapshot);
    public EntityPage confirmPendingPage(EntityPage page) throws EventStoreException;
    public TransactionPage getTransactionPage();
    public EntityPageParser getEntityPageParser();
    public void setEntityPageParser(EntityPageParser parser);
}
