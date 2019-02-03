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
import com.mc1098.mk_eventstore.Exception.NoPageFoundException;
import com.mc1098.mk_eventstore.Exception.SerializationException;
import com.mc1098.mk_eventstore.Page.EntityPage;
import com.mc1098.mk_eventstore.Page.EntityPageParser;
import com.mc1098.mk_eventstore.Page.PageDirectory;
import com.mc1098.mk_eventstore.Util.EventStoreUtils;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class Mk_TransactionWorker extends TransactionWorker
{
    
    public static final Logger LOGGER = Logger
        .getLogger(TransactionWorker.class.getName());
    
    private final PageDirectory directory;
    private final TransactionPage transactionPage;
    private final EntityPageParser entityPageParser;
    private final AtomicBoolean run;
    
    public Mk_TransactionWorker(TransactionPage transactionPage, 
            PageDirectory directory, EntityPageParser parser)
    {
        this.transactionPage = transactionPage;
        this.directory = directory;
        this.entityPageParser = parser;
        this.run = new AtomicBoolean(true);
    }

    @Override
    public void run()
    {
        while (run.get())
        {            
            processTransactions();
        }
            
    }
    
    private void processTransactions()
    {
        Transaction transaction = null;
        try
        {
            transaction = transactionPage.poll(5, TimeUnit.SECONDS);
            if(transaction == null)
            {
                transactionPage.truncateLog();
                return;
            }
            
            long entity = transaction.getEntity();
            long entityId = transaction.getEntityId();
            long expPageId = transaction.getVersion() / directory.getEPR(transaction.getEntity());
            
            if(transaction.getType() == TransactionType.PUT_EVENT)
                processEventTransaction(entity, entityId, expPageId, transaction);
            else if(transaction.getType() == TransactionType.PUT_SNAPSHOT)
                processSnapshotTransaction(entity, entityId, expPageId, transaction);
                    
            
        } catch (IOException ex)
        {
            LOGGER.log(Level.INFO, null, ex);
        } catch(NoPageFoundException ex)
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
        catch(InterruptedException ex)
        {
            LOGGER.log(Level.SEVERE, "Interruption exception thrown while "
                    + "waiting for the next transaction.", ex);
            Thread.currentThread().interrupt();
        }
        catch (EventStoreException ex)
        {
            Logger.getLogger(TransactionWorker.class.getName()).log(Level.SEVERE, null, ex);
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
            throws EventStoreException, IOException
    {
        EntityPage page = directory.getEntityPage(entity, entityId,
                expPageId);
        if (page.getCleanVersion() > transaction.getVersion())
        {
            //already been saved
            transactionPage.confirmTransactionProcessed(transaction);
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
                writePage(page, bytes);
                transactionPage.confirmTransactionProcessed(transaction);
            }
        }
        else
            transactionPage.confirmTransactionProcessed(transaction);
    }

    private void writePage(EntityPage page, byte[] bytes) 
    {
        File file = new File(String.format("./Entity/%s/%s/%s",  
                Long.toHexString(page.getEntity()), 
                Long.toHexString(page.getEntityId()), 
                Long.toHexString(page.getPageId())));
                
        
        try(FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.WRITE);)
        {
            fc.write(ByteBuffer.wrap(bytes));
        } catch(IOException ex)
        {
            System.out.println(ex);
        }
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
    public void flush() throws IOException
    {
        while(transactionPage.hasTransaction())
            processTransactions();
        transactionPage.truncateLog();
    }
    
    @Override
    public void stopAfterTransaction() throws InterruptedException
    {
        run.set(false);
    }
    
}
