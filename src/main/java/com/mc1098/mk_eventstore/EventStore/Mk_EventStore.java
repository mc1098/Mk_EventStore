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
package com.mc1098.mk_eventstore.EventStore;

import com.mc1098.mk_eventstore.Entity.EntityToken;
import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Event.SimpleEventConverter;
import com.mc1098.mk_eventstore.Exception.EntityChronologicalException;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Page.EntityPage;
import com.mc1098.mk_eventstore.Page.Mk_PageDirectory;
import com.mc1098.mk_eventstore.Page.PageDirectory;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionPage;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionConverter;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionWorker;
import com.mc1098.mk_eventstore.Transaction.Transaction;
import com.mc1098.mk_eventstore.Transaction.TransactionBuilder;
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import com.mc1098.mk_eventstore.Transaction.TransactionWorker;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import com.mc1098.mk_eventstore.Page.EntityPageConverter;
import com.mc1098.mk_eventstore.Event.EventConverter;
import com.mc1098.mk_eventstore.FileSystem.PageFileSystem;
import com.mc1098.mk_eventstore.FileSystem.RelativeFileSystem;
import java.nio.file.Paths;
import com.mc1098.mk_eventstore.Transaction.TransactionConverter;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_EventStore implements EventStore
{
    
    public static EventStore create() throws EventStoreException
    {
        RelativeFileSystem rfs = PageFileSystem.ofRoot(Paths.get("Entity"));
        
        TransactionConverter tc = new Mk_TransactionConverter();
        rfs.createFile("TL");
        Queue<Transaction> transactions = new ArrayDeque<>(rfs
                .readAndParseRecursively(tc, "TL"));
        TransactionPage transactionPage = new Mk_TransactionPage(rfs, transactions, tc);
        
        rfs.createFile("ENM");
        
        EventConverter ef = new SimpleEventConverter();
        PageDirectory directory = Mk_PageDirectory.setup(rfs, ef, transactionPage);
        EntityPageConverter converter = directory.getEntityPageConverter();
        TransactionWorker tw = new Mk_TransactionWorker(rfs, transactionPage, 
                directory, converter);
        tw.flush();
        tw.start();
        return new Mk_EventStore(directory, transactionPage, tw);
    }
    
    public static EventStore create(RelativeFileSystem rfs, 
            PageDirectory directory, TransactionPage transactionPage) 
            throws EventStoreException
    {
        EntityPageConverter parser = directory.getEntityPageConverter();
        TransactionWorker tw = new Mk_TransactionWorker(rfs, transactionPage, 
                directory, parser);
        tw.flush();
        tw.start();
        return new Mk_EventStore(directory, transactionPage, tw);
        
    }
    
    
    private final PageDirectory directory;
    private final TransactionPage transactionPage;
    private final TransactionWorker transactionWorker;
    
    protected Mk_EventStore(PageDirectory directory, 
            TransactionPage transactionPage, TransactionWorker transactionWorker)
    {
        this.directory = directory;
        this.transactionPage = transactionPage;
        this.transactionWorker = transactionWorker;
    }
    
    @Override
    public int getERP(String entityName)
    {
        return directory.getEPR(directory.getEntity(entityName));
    }
    
    @Override
    public EntityToken getById(String entityName, long id) 
            throws EventStoreException
    {
        long entity = directory.getEntity(entityName);
        EntityPage page = directory.getEntityPage(entity, id);
        
        return new EntityToken(page.getSnapshot(), page.getEvents());
    }

    @Override
    public EntityToken getById(String entityName, long id, long version) 
            throws EventStoreException
    {
        long entity = directory.getEntity(entityName);
        int erp = directory.getEPR(entity);
        long pageId =  version / erp;
        int eventsToRead = (int) (version - (pageId * erp));
        EntityPage page = directory.getEntityPage(entity, id, pageId);
        
        Event[] events = new Event[eventsToRead];
        System.arraycopy(page.getEvents(), 0, events, 0, eventsToRead);
        return new EntityToken(page.getSnapshot(), events);
    }

    @Override
    public Queue<Event> getEventsById(String entityName, long id, long fromVer) 
            throws EventStoreException
    {
        long entity = directory.getEntity(entityName);
        int erp = directory.getEPR(entity);
        long pageId = fromVer/erp;
        final AtomicLong version = new AtomicLong(erp * pageId); //final for lambda 
        
        Queue<Event> events = new ArrayDeque<>();
        directory.consumeEntityPages(entity, id, pageId, (ep)-> 
        {
            for (Event e : ep.getEvents())
                if(version.incrementAndGet() > fromVer)
                    events.add(e);
        });
        
        return events;
    }

    @Override
    public Queue<Event> getEventsById(String entityName, long id, long fromVer, 
            long toVer) throws EventStoreException
    {
        long entity = directory.getEntity(entityName);
        int erp = directory.getEPR(entity);
        long pageFrom = fromVer/erp;
        final AtomicLong version = new AtomicLong(erp * pageFrom);
        long pageTo = toVer/erp;
        
        Queue<Event> events = new ArrayDeque<>();
        directory.consumeEntityPages(entity, id, pageFrom, pageTo, (ep) -> 
        {
            for (Event e : ep.getEvents())
                if(version.incrementAndGet() > fromVer && version.get() <= toVer)
                    events.add(e);
        });
        return events;
    }
    
    @Override
    public Snapshot getSnapshot(String entityName, long id) 
            throws EventStoreException
    {
        long entity = directory.getEntity(entityName);
        EntityPage page = directory.getEntityPage(entity, id);
        return page.getSnapshot();
    }
    
    @Override
    public Snapshot getSnapshot(String entityName, long id, long lteq) 
            throws EventStoreException
    {
        long entity = directory.getEntity(entityName);
        int erp = directory.getEPR(entity);
        long pageId = (lteq / erp);
        
        EntityPage page = directory.getEntityPage(entity, id, pageId);
        return page.getSnapshot();
    }
    
    @Override
    public void saveSnapshot(Snapshot ss) throws EventStoreException
    {
        long entity = directory.getEntity(ss.getEntityName());
        int erp = directory.getEPR(entity);
        long pageId = ss.getVersion() / erp;
        if(directory.hasEntity(ss.getEntityName()))
            validate(entity, erp, ss, 0);
        
        TransactionBuilder tb = new TransactionBuilder();
        Transaction transaction = tb.build(pageId, entity, ss.getEntityId(), 
                ss);
        
        EntityPage page = directory.createPendingEntityPage(entity, 
                ss.getEntityId(), pageId, ss);
        transactionPage.writeTransaction(transaction);
        directory.confirmPendingPage(page);
        transactionPage.refresh();
    }
    
    private void validate(long entity, int erp, Snapshot snapshot, 
            long pendingEvents) 
            throws EventStoreException
    {
        if(snapshot.getVersion() % erp != 0)
            throw new EntityChronologicalException(String.format("Unexpected "
                    + "snapshot. Snapshots are expected every Event Page Ratio "
                    + "(ERP). This snapshot was for version %d where the ERP is"
                    + " currently set at %d", snapshot.getVersion(), erp));
        
        if(snapshot.getVersion() == 0)
            if(directory.doesPageExist(entity, snapshot.getEntityId(), 0))
                throw new EntityChronologicalException(String.format("Save state "
                        + "for this entity already exists a new snapshot for "
                        + "this entity with a version of 0 is not expected."));
            else
                return;
        
        EntityPage page = directory.getEntityPage(entity, snapshot.getEntityId());
        //pending events yet to reach the page can be included for when an EntityToken is
        //updated in an atomic way. Therefore the saveSnapshot would take place after 
        //the pending events have occurred changing the version.
        long pageVersion = page.getVersion() + pendingEvents;
        
        if(pageVersion != snapshot.getVersion())
            throw new EntityChronologicalException(String.format("The state of "
                    + "save events does not match the version for the snapshot, "
                    + "some events may be missing between this store and the "
                    + "snapshot provided. Current version of entity #%d with ID"
                    + " of %d is %d (%d pending events inc) whereas the snapshot "
                    + "version is %d", entity, page.getEntityId(), pageVersion, 
                    pendingEvents, snapshot.getVersion()));
    }

    @Override
    public void save(String entityName, long id, long loadedVersion, 
            Event[] events) throws EventStoreException
    {
        long entity = directory.getEntity(entityName);
        EntityPage page = directory.getEntityPage(entity, id);
        
        validate(entity, loadedVersion, page);
        TransactionBuilder tb = new TransactionBuilder();
        List<Transaction> transactions = tb.build(page.getPageId(), entity, id, 
                events);
        
        page.addToPending(events);
        transactionPage.writeTransaction(transactions);
        page.confirmEvents(events);
    }
    
    private void validate(long entity, long loadedVersion, EntityPage page) 
            throws EntityChronologicalException
    {
        if(page.getVersion() != loadedVersion)
            throw new EntityChronologicalException(directory
                    .getEntityName(entity), page.getVersion(), loadedVersion);
        
    }

    @Override
    public void save(EntityToken token) throws EventStoreException
    {
        if(!hasSnapshotsAndEvents(token))
            throw new EventStoreException("Entity token does not contain both "
                    + "snapshot and events.");
        
        SortedSet<Snapshot> snapshots = new TreeSet<>((s, s1) ->
            Long.compare(s.getVersion(), s1.getVersion()));
        snapshots.addAll(Arrays.asList(token.getSnapshots()));
        
        SortedSet<Event> events = new TreeSet<>((e, e1) -> 
            Long.compare(e.getVersion(), e1.getVersion()));
        events.addAll(Arrays.asList(token.getEvents()));
        
        saveFromSortedSets(snapshots, events);
        
    }

    private void saveFromSortedSets(SortedSet<Snapshot> snapshots, 
            SortedSet<Event> events) throws EventStoreException
    {
        Snapshot s1 = snapshots.first();
        Event e1 = events.first();
        long loadedVersion = e1.getVersion() < s1.getVersion() ? e1.getVersion() : s1.getVersion();
        long entity = directory.getEntity(s1.getEntityName());
        long entityId = s1.getEntityId();
        int erp = directory.getEPR(entity);
        EntityPage page = null;
        
        Map<EntityPage, Event[]> confirmationMap = new HashMap<>();
        List<Event> forCurrentPage = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
        TransactionBuilder tb = new TransactionBuilder();
        
        for (Snapshot snapshot : snapshots)
        {
            if(page == null && loadedVersion < snapshot.getVersion())
                page = directory.getEntityPage(entity, entityId);
            else
            {
                long pageId = snapshot.getVersion() / erp;
                page = directory.createPendingEntityPage(entity, entityId, pageId, snapshot);
                transactions.add(tb.build(page.getPageId(), entity, entityId, snapshot));
                confirmationMap.put(page, null);
            }
            
            for (Event event : events)
            {
                if(event.getVersion() < snapshot.getVersion() + erp)
                    forCurrentPage.add(event);
                else
                    addEventsToCurrentPage(forCurrentPage, transactions, tb, 
                            page, entity, entityId, confirmationMap);
            }
        }
        
        addEventsToCurrentPage(forCurrentPage, transactions, tb, page, entity,
                entityId, confirmationMap);
        
        transactionPage.writeTransaction(transactions);
        confirmChanges(confirmationMap);
        transactionPage.refresh();
    }

    private void confirmChanges(Map<EntityPage, Event[]> confirmationMap) 
            throws EventStoreException
    {
        for (Map.Entry<EntityPage, Event[]> entry : confirmationMap.entrySet())
        {
            EntityPage ep = entry.getKey();
            if(ep.getCleanVersion() == -1)
                directory.confirmPendingPage(ep);
            ep.confirmEvents(entry.getValue());
        }
    }

    private void addEventsToCurrentPage(List<Event> forCurrentPage,
            List<Transaction> transactions, TransactionBuilder tb, 
            EntityPage page, long entity, long entityId, 
            Map<EntityPage, Event[]> confirmationMap) 
            throws EventStoreException
    {
        if(!forCurrentPage.isEmpty())
        {
            Event[] current = forCurrentPage
                    .toArray(new Event[forCurrentPage.size()]);
            transactions.addAll(tb.build(page.getPageId(), entity,
                    entityId, current));
            page.addToPending(current);
            confirmationMap.put(page, current);
        }
    }
    
    
    private boolean hasSnapshotsAndEvents(EntityToken token)
    {
        return (token.getSnapshots().length != 0 && token.getEvents().length != 0);
    }

    @Override
    public void close() throws Exception
    {
        transactionWorker.stopAfterTransaction();
        transactionWorker.join();
    }
    
    
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Mk_EventStore))
            return false;
        
        Mk_EventStore es = (Mk_EventStore) o;
        
        return (this.directory.equals(es.directory) && 
                this.transactionPage.equals(es.transactionPage));
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.directory);
        hash = 41 * hash + Objects.hashCode(this.transactionPage);
        return hash;
    }

    
    
}
