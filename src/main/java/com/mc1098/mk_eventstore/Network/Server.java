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
package com.mc1098.mk_eventstore.Network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public abstract class Server implements Runnable, AutoCloseable
{
    public static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    
    private final AtomicBoolean run;
    private Exception exception;
    
    public Server()
    {
        this.run = new AtomicBoolean(true);
        this.exception = null;
    }
    
    public Exception getException() {return exception;}

    @Override
    public void run()
    {
        InetSocketAddress isa = new InetSocketAddress(6543);
        try(Selector selector = Selector.open();
                ServerSocketChannel ssc = ServerSocketChannel.open())
        {
            ssc.configureBlocking(false);
            ssc.socket().bind(isa);
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            while(run.get())
            {
                if(selector.select() <= 0)
                    continue;

                Set<SelectionKey> readySet = selector.keys();
                Iterator<SelectionKey> itr = readySet.iterator();
                while(itr.hasNext())
                {
                    SelectionKey key = itr.next();
                    itr.remove();
                    if(key.isAcceptable())
                    {
                        ServerSocketChannel innerSsc = (ServerSocketChannel) key.channel();
                        SocketChannel innerSc = innerSsc.accept();
                        innerSc.configureBlocking(false);
                        innerSc.register(key.selector(), SelectionKey.OP_READ);
                    }
                    if(key.isReadable())
                        processRead(key);
                }
            }
            
        }catch(IOException ex)
        {
            exception = ex;
            return;
        }
        
    }
    
    protected abstract void processRead(SelectionKey key);
    
    protected void write(SelectionKey key, byte[] bytes)
    {
        try
        {
            SocketChannel sc = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            sc.write(buffer);
        } catch(IOException ex)
        {
            LOGGER.log(Level.SEVERE, "Failure to write bytes to client", ex);
        }
        
    }
    
    @Override
    public void close()
    {
        run.set(false);
    }
    
}
