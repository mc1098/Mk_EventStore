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

import com.mc1098.mk_eventstore.Exception.EventStoreException;
import java.util.List;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public interface TransactionPage
{
    public void writeTransaction(Transaction transaction) throws EventStoreException;
    public void writeTransaction(List<Transaction> transactions)throws EventStoreException;
    public boolean hasTransaction();
    public Transaction peek();
    public void confirmTransactionProcessed(Transaction transaction);
    
    /**
     * This method will indicate the ability to truncate the Transaction Log on 
     * before the next write.
     * 
     * Implementations do not need to guarantee that the truncation will take 
     * place or even when this will take place. It should however guarantee 
     * that the truncation will happen before the next write. 
     * 
     * This method should be non blocking and shouldn't cause any exceptions 
     * even if called many times before the indicated truncation occurs.
     */
    public void truncateLog();
    public void refresh();
}
