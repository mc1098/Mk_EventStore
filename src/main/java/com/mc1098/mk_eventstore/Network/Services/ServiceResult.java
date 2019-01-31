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

import java.io.Serializable;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class ServiceResult<T extends Serializable> implements Serializable
{
    
    public static ServiceResult errorResult(EventStoreService service,
            String message)
    {
        return new ServiceResult(service, message);
    }
    
    
    private final EventStoreService service;
    private final String message;
    private final T result;
    
    public ServiceResult(EventStoreService service,
            String message, T result)
    {
        this.service = service;
        this.message = message;
        this.result = result;
    }
    
    public ServiceResult(EventStoreService service,
            String message)
    {
        this(service, message, null);
    }
    
    public EventStoreService service() {return service;}
    public boolean isSuccessful() {return result != null;}
    public String message() {return message;}
    public T result() {return result;}
}
