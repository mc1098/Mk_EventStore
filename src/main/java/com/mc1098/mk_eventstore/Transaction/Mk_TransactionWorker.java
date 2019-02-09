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

import com.mc1098.mk_eventstore.Entity.Mk_Snapshot;
import com.mc1098.mk_eventstore.Entity.Snapshot;
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Exception.EventStoreException;
import com.mc1098.mk_eventstore.Exception.FileSystem.FileSystemException;
import com.mc1098.mk_eventstore.Exception.NoPageFoundException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import com.mc1098.mk_eventstore.FileSystem.RelativeFileSystem;
import com.mc1098.mk_eventstore.FileSystem.WriteOption;
import com.mc1098.mk_eventstore.Page.EntityPage;
import com.mc1098.mk_eventstore.Page.PageDirectory;
import com.mc1098.mk_eventstore.Util.EventStoreUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mc1098.mk_eventstore.Page.EntityPageConverter;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_TransactionWorker extends TransactionWorker
{
    
    public static final Logger LOGGER = Logger
        .getLogger(TransactionWorker.class.getName());
    
    private final RelativeFileSystem fileSystem;
    private final PageDirectory directory;
    private final TransactionPage transactionPage;
    private final EntityPageConverter entityPageParser;
    private final AtomicBoolean run;
    
    public Mk_TransactionWorker(RelativeFileSystem rfs, 
            TransactionPage transactionPage, PageDirectory directory, 
            EntityPageConverter parser)
    {
        this.fileSystem = rfs;
        this.transactionPage = transactionPage;
        this.directory = directory;
        this.entityPageParser = parser;
        this.run = new AtomicBoolean(true);
        this.setName("Transaction Worker Thread");
    }
    
    @Override
    public boolean isShuttingDown() {return !run.get();}

    @Override
    public void run()
    {
        while (run.get())
        {
            try 
            {
                processTransactions();
                synchronized(this)
                {
                    wait(5000);
                }
            } catch(EventStoreException ex)
            {
                LOGGER.log(Level.SEVERE, "Unrecoverable or possibly data "
                        + "corrupting error has occurred.", ex);
                getDefaultUncaughtExceptionHandler().uncaughtException(this, ex);
                break;
            } catch(InterruptedException ex)
            {
                LOGGER.log(Level.SEVERE, "Interruption exception thrown while "
                    + "waiting for the next transaction.", ex);
                Thread.currentThread().interrupt();
            }
        }
            
    }
    
    private void processTransactions() throws EventStoreException
    {
        Transaction transaction = null;
        try
        {
            transaction = transactionPage.peek();
            if(transaction == null)
            {
                transactionPage.truncateLog();
                return;
            }
            
            long entity = transaction.getEntity();
            long entityId = transaction.getEntityId();
            long expPageId = transaction.getVersion() / directory.getEPR(transaction.getEntity());
            
            
            switch(transaction.getType())
            {
                case PUT_EVENT: 
                    processEventTransaction(entity, entityId, expPageId, transaction);
                    break;
                case PUT_SNAPSHOT:
                    processSnapshotTransaction(entity, entityId, expPageId, transaction);
                    break;
                default:
                    throw new AssertionError(String.format("%s TransactionType "
                            + "is not supported by this TransactionWorker",
                            transaction.getType().name()));
            }     
            
        }catch(NoPageFoundException ex)
        {
            if(!run.get())
                LOGGER.log(Level.INFO, "Transaction was unable to be processed "
                        + "while worker is shutting down. The transaction will "
                        + "be processed when started again.", ex);
            else 
            {
                LOGGER.log(Level.SEVERE, String.format("Unable to find "
                        + "associated page for transaction -> %s", transaction),
                        ex);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processSnapshotTransaction(long entity, long entityId, 
            long expPageId, Transaction transaction) 
            throws EventStoreException
    {
        if(!directory.doesPageExist(entity, entityId, expPageId))
        {
            Snapshot snapshot = new Mk_Snapshot(directory
                    .getEntityName(entity), entityId,
                    transaction.getVersion(), transaction.getData());
            
            EntityPage page = directory.createPendingEntityPage(entity,
                    entityId, expPageId, snapshot);
            directory.confirmPendingPage(page);
            
        }
        transactionPage.confirmTransactionProcessed(transaction);
    }

    private void processEventTransaction(long entity, long entityId, 
            long expPageId, Transaction transaction) 
            throws EventStoreException
    {
        EntityPage page = directory.getEntityPage(entity, entityId,
                expPageId);
        if (page.getCleanVersion() > transaction.getVersion())
        {
            transactionPage.confirmTransactionProcessed(transaction); //already been saved
            return;
        }
        if(page.getCleanVersion() < transaction.getVersion() ||
                page.getSnapshot().getVersion() == transaction.getVersion())
        {
            if(page.getVersion() <= transaction.getVersion())
                addEventToPage(page, transaction);
            long version = page.getCleanVersion();
            byte[] bytes = entityPageParser.toBytes(page);
            if(version == page.getCleanVersion())
            {
                fileSystem.write(WriteOption.WRITE, bytes, 
                        Long.toHexString(page.getEntity()),
                        Long.toHexString(page.getEntityId()), 
                        Long.toHexString(page.getPageId()));
                transactionPage.confirmTransactionProcessed(transaction);
            }
        }
        else
            transactionPage.confirmTransactionProcessed(transaction);
    }

    private void addEventToPage(EntityPage page, Transaction transaction) 
            throws EventStoreException
    {
        try 
        {
            Event e = (Event) EventStoreUtils.deserialise(transaction.getData());
            page.addToPending(e);
            page.confirmEvents(e);
        } catch(SerializationException | ClassCastException ex)
        {
            throw new EventStoreException(ex);
        }
    }
    
    @Override
    public void flush() throws EventStoreException
    {
        try 
        {
            while(transactionPage.hasTransaction())
                processTransactions();
            fileSystem.truncateFile("TL");
        } catch(FileSystemException ex)
        {
            LOGGER.log(Level.WARNING, "Unable to truncate the Transaction Log after flushing the pending transactions", ex);
            throw new EventStoreException(ex);
        }
    }
    
    @Override
    public void stopAfterTransaction() throws InterruptedException
    {
        run.set(false);
        synchronized(this)
        {
            notifyAll();
        }
    }
    
}
