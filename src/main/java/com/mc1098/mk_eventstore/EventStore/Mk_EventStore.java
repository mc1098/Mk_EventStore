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
import com.mc1098.mk_eventstore.Event.EventFormat;
import com.mc1098.mk_eventstore.Event.SimpleEventFormat;
import com.mc1098.mk_eventstore.Exception.EntityChronologicalException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Exception.AlreadyPendingChange;
import com.mc1098.mk_eventstore.Exception.TransactionException;
import com.mc1098.mk_eventstore.Page.EntityPage;
import com.mc1098.mk_eventstore.Page.EntityPageParser;
import com.mc1098.mk_eventstore.Page.Mk_PageDirectory;
import com.mc1098.mk_eventstore.Page.PageDirectory;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionPage;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionParser;
import com.mc1098.mk_eventstore.Transaction.Mk_TransactionWorker;
import com.mc1098.mk_eventstore.Transaction.Transaction;
import com.mc1098.mk_eventstore.Transaction.TransactionBuilder;
import com.mc1098.mk_eventstore.Transaction.TransactionPage;
import com.mc1098.mk_eventstore.Transaction.TransactionParser;
import com.mc1098.mk_eventstore.Transaction.TransactionWorker;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_EventStore implements EventStore
{
    
    public static EventStore create() throws IOException, EventStoreException
    {
        TransactionPage transactionPage;
        TransactionParser tp = new Mk_TransactionParser();
        File file = new File("Entity/TL");
        
        if(!file.exists())
        {
            if(!(file.getParentFile().mkdirs() && file.createNewFile()))
                throw new EventStoreException("Unable to create the required"
                        + " directories or files in order for setup.");
            transactionPage = new Mk_TransactionPage(file, tp);
        }
        else
        {
            ByteBuffer buffer;
            try(FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.READ))
            {
                buffer = ByteBuffer.allocate((int)fc.size());
                while (fc.read(buffer) > 0) {};
            }
            transactionPage = Mk_TransactionPage.parse(file, buffer, tp);
        }
        
        file = new File("Entity/ENM");
        
        if(!file.exists())
            if(!file.createNewFile())
                throw new EventStoreException("Unable to create the required"
                        + " ENM file in order for setup.");
        
        EventFormat ef = new SimpleEventFormat();
        PageDirectory directory = Mk_PageDirectory.setup(ef, transactionPage);
        EntityPageParser parser = directory.getEntityPageParser();
        TransactionWorker tw = new Mk_TransactionWorker(transactionPage, directory, parser);
        tw.flush();
        new Thread(tw, "Transaction Worker Thread").start();
        return new Mk_EventStore(directory, transactionPage, tw);
    }
    
    public static EventStore create(PageDirectory directory, 
            TransactionPage transactionPage) throws IOException
    {
        EntityPageParser parser = directory.getEntityPageParser();
        TransactionWorker tw = new Mk_TransactionWorker(transactionPage, directory, parser);
        tw.flush();
        new Thread(tw, "Transaction Worker Thread").start();
        return new Mk_EventStore(directory, transactionPage, tw);
        
    }
    
    
    private final PageDirectory directory;
    private final TransactionPage transactionPage;
    private final TransactionWorker transactionWorker;
    
    private Mk_EventStore(PageDirectory directory, 
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
        long version = erp * pageId;
        
        List<EntityPage> pages = directory.getEntityPages(entity, id, pageId);
        Queue<Event> events = new ArrayDeque<>();
        for (EntityPage page : pages)
            for (Event event : page.getEvents())
            {
                version+=1;
                if(version > fromVer)
                    events.add(event);
            }
        
        return events;
    }

    @Override
    public Queue<Event> getEventsById(String entityName, long id, long fromVer, 
            long toVer) throws EventStoreException
    {
        long entity = directory.getEntity(entityName);
        int erp = directory.getEPR(entity);
        long pageId = fromVer/erp;
        long version = erp * pageId;
        long pageId1 = toVer/erp;
        
        List<EntityPage> pages = directory.getEntityPages(entity, id, pageId, pageId1);
        Queue<Event> events = new ArrayDeque<>();
        for (EntityPage page : pages)
            for (Event event : page.getEvents())
            {
                version+=1;
                if(version > fromVer && version < toVer)
                    events.add(event);
            }
        
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
    public void saveSnapshot(Snapshot ss) throws EntityChronologicalException, 
            TransactionException, EventStoreException
    {
        long entity = directory.getEntity(ss.getEntityName());
        int erp = directory.getEPR(entity);
        long pageId = ss.getVersion() / erp;
        if(directory.hasEntity(ss.getEntityName()))
            validate(entity, erp, ss, 0);
        
        TransactionBuilder tb = new TransactionBuilder();
        Transaction transaction = tb.build(pageId, entity, ss.getEntityId(), 
                ss);
        
        EntityPage page = directory.createPendingEntityPage(entity, ss.getEntityId(), pageId, ss);
        transactionPage.writeTransaction(transaction);
        directory.confirmPendingPage(page);
        transactionPage.refresh();
    }
    
    private void validate(long entity, int erp, Snapshot snapshot, 
            long pendingEvents) 
            throws EntityChronologicalException, EventStoreException
    {
        if(snapshot.getVersion() % erp != 0)
            throw new EntityChronologicalException(String.format("Unexpected "
                    + "snapshot. Snapshots are expected every Event Page Ratio "
                    + "(ERP). This snapshot was for version %d where the ERP is"
                    + " currently set at %d", snapshot.getVersion(), erp));
        
        if(snapshot.getVersion() == 0)
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
            Event[] events) throws EntityChronologicalException, 
            SerializationException, AlreadyPendingChange, 
            EventStoreException
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
    public void save(EntityToken token) throws EntityChronologicalException, 
            SerializationException, EventStoreException
    {
        if(!hasSnapshotsAndEvents(token))
            throw new EventStoreException("Entity token does not contain both "
                    + "snapshot and events.");
        
        SortedSet<Snapshot> snapshots = new TreeSet<>((s, s1) ->{
            return Long.compare(s.getVersion(), s1.getVersion());
        });
        snapshots.addAll(Arrays.asList(token.getSnapshots()));
        
        SortedSet<Event> events = new TreeSet<>((e, e1) -> {
            return Long.compare(e.getVersion(), e1.getVersion());
        });
        events.addAll(Arrays.asList(token.getEvents()));
        
        saveFromSortedSets(snapshots, events);
        
    }

    private void saveFromSortedSets(SortedSet<Snapshot> snapshots, 
            SortedSet<Event> events) throws SerializationException, 
            EventStoreException, TransactionException, AlreadyPendingChange
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
            throws AlreadyPendingChange, SerializationException
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

    
    
}
