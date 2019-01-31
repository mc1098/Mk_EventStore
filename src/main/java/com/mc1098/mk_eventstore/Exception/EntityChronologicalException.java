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
package com.mc1098.mk_eventstore.Exception;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class EntityChronologicalException extends EventStoreException
{

    public EntityChronologicalException(String string)
    {
        super(string);
    }

    public EntityChronologicalException(String string, Throwable thrwbl)
    {
        super(string, thrwbl);
    }

    public EntityChronologicalException(Throwable thrwbl)
    {
        super(thrwbl);
    }
    
    
    public EntityChronologicalException(String entityName, long version, 
            long loadedVersion)
    {
        super(String.format("Events cannot "
                    + "be saved as they deviate from the saved history of the "
                    + "entity. The version of the entity provided has since "
                    + "been updated and therefore the events supplied cannot "
                    + "be saved. Entity: %s has a saved version of %d and the "
                    + "loaded version of the events is %d", entityName, 
                    version, loadedVersion));
    }
    
}
