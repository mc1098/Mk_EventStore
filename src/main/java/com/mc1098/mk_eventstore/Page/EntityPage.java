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
import com.mc1098.mk_eventstore.Event.Event;
import com.mc1098.mk_eventstore.Exception.AlreadyPendingChange;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public interface EntityPage
{
    public long getPageId();
    public long getEntity();
    public long getEntityId();
    public long getVersion();
    public long getCleanVersion();
    public void setCleanVersion(long version);
    public Snapshot getSnapshot();
    public Event[] getEvents();
    public int getEventPageRatio();
    public int events();
    public void addToPending(Event...events) throws AlreadyPendingChange;
    public void confirmEvents(Event...events);
}

