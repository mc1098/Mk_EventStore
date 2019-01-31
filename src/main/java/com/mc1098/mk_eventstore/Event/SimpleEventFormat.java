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
package com.mc1098.mk_eventstore.Event;

import com.mc1098.mk_eventstore.Exception.ParseException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class SimpleEventFormat implements EventFormat
{

    @Override
    public byte[] toBytes(Event event) throws ParseException
    {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
                ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            oos.writeObject(event);
            oos.flush();
            return baos.toByteArray();
        } catch(IOException ex)
        {
            throw new ParseException(ex);
        }
    }

    @Override
    public Event parse(byte[] bytes) throws ParseException
    {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes); 
                ObjectInputStream ois = new ObjectInputStream(bais))
        {
            return (Event) ois.readObject();
        } catch(IOException | ClassNotFoundException ex)
        {
            throw new ParseException(ex);
        }
    }
    
}
