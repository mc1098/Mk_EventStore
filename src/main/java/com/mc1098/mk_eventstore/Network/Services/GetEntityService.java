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
package com.mc1098.mk_eventstore.Network.Services;

import com.mc1098.mk_eventstore.Entity.EntityToken;
import com.mc1098.mk_eventstore.EventStore.EventStore;
import com.mc1098.mk_eventstore.Exception.EventStoreException;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class GetEntityService implements EventStoreService
{
    private final String entityName;
    private final long entityId;
    private final long version;
    
    public GetEntityService(String entityName, long entityId, long version)
    {
        this.entityName = entityName;
        this.entityId = entityId;
        this.version = version;
    }
    
    public GetEntityService(String entityName, long entityId)
    {
        this.entityName = entityName;
        this.entityId = entityId;
        this.version = -1;
    }
    
    @Override
    public ServiceResult execute(EventStore store)
    {
        EntityToken token;
        
        try
        {
            if(version == -1)
                token = store.getById(entityName, entityId);
            else 
                token = store.getById(entityName, entityId, version);

            return new ServiceResult(this, "success", token);
        } catch(EventStoreException ex)
        {
            return ServiceResult.errorResult(this, ex.getLocalizedMessage());
        }
        
    }
    
}
