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
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import java.util.function.Consumer;

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
    public EntityPage getEntityPage(long entity, long id) throws EventStoreException;
    public EntityPage getEntityPage(long entity, long id, long pageNo) 
            throws EventStoreException;
    
    /**
     * This method reads and consumes the pages, with the provided 
     * {@link Consumer}, from the fromPage parameter provided (inclusive) to
     * the most recent page for the Entity Id. 
     * 
     * Implementations of this method may not guarantee atomic consuming of 
     * the EntityPages, therefore consuming of some pages maybe performed before
     * this method is interrupted with an Exception. Consumers that causes change
     * in state directly is not recommended unless an implementation of this 
     * method guarantees atomic consuming.
     * 
     * Implementations must guarantee that consuming, regardless of it's atomicity, 
     * is done in ascending page order from the first page parameter given.
     * 
     * @param entity Representative number for the Entity.
     * @param id Id of the Entity.
     * @param fromPage PageId from which to start reading and consuming (inclusive).
     * @param cnsmr Consumer to use for each page read.
     * @throws EventStoreException 
     */
    public void consumeEntityPages(long entity, long id, long fromPage, 
            Consumer<EntityPage> cnsmr) throws EventStoreException;
    
    /**
     * This method reads and consumes the pages, with the provided 
     * {@link Consumer}, from the fromPage parameter provided (inclusive) to
     * the toPage parameter provided (exclusive).
     * 
     * Implementations of this method may not guarantee atomic consuming of 
     * the EntityPages, therefore consuming of some pages maybe performed before
     * this method is interrupted with an Exception. Consumers that causes change
     * in state directly is not recommended unless an implementation of this 
     * method guarantees atomic consuming.
     * 
     * Implementations must guarantee that consuming, regardless of it's atomicity, 
     * is done in ascending page order from the first page parameter given.
     * 
     * @param entity Representative number for the Entity.
     * @param id Id of the Entity.
     * @param fromPage PageId from which to start reading and consuming (inclusive).
     * @param toPage PageId to end reading and consuming (exclusive).
     * @param cnsmr Consumer to use for each page read.
     * @throws EventStoreException 
     */
    public void consumeEntityPages(long entity, long id, long fromPage, 
            long toPage, Consumer<EntityPage> cnsmr) throws EventStoreException;
    public EntityPage createPendingEntityPage(long entity, long id, long pageNo, Snapshot snapshot);
    public EntityPage confirmPendingPage(EntityPage page) throws EventStoreException;
    public TransactionPage getTransactionPage();
    public EntityPageConverter getEntityPageConverter();
}
