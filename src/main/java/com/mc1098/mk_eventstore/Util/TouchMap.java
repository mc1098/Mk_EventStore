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
package com.mc1098.mk_eventstore.Util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class TouchMap<K, V> implements Map<K, V>
{
    private final Map<TouchKey<K>, TouchValue<V>> map;
    
    
    public TouchMap()
    {
        this.map = new HashMap<>();
    }
    
    public V lastValue() 
    {
        return entrySet().stream().sorted().findFirst().get().getValue();
    }
    

    @Override
    public int size() {return map.size();}

    @Override
    public boolean isEmpty() {return map.isEmpty();}

    @Override
    public boolean containsKey(Object o) {return map.containsKey(new TouchKey(o));}

    @Override
    public boolean containsValue(Object o) 
    {
        return map.values().stream()
                .anyMatch((TouchValue tv)->{return tv.peek().equals(o);});
    }

    @Override
    public V get(Object o) 
    {
        return map.get(new TouchKey(o)).get();
    }

    @Override
    public V put(K k, V v)
    {
        TouchKey<K> key = new TouchKey(k);
        return (V) map.put(key, new TouchValue(key, v));
    }

    @Override
    public V remove(Object o) {return map.remove(new TouchKey(o)).get();}

    @Override
    public void putAll(Map<? extends K, ? extends V> map)
    {
        Map<TouchKey<K>, TouchValue<V>> temp = new HashMap<>();
        map.forEach((t, u) ->{
            TouchKey<K> key = new TouchKey(t);
            temp.put(key, new TouchValue(key, u));
        });
        this.map.putAll(temp);
    }

    @Override
    public void clear() {map.clear();}

    @Override
    public Set<K> keySet() 
    {
        Set<K> set = new TreeSet<>();
        map.keySet().forEach((tk)-> {set.add(tk.key);});
        return set;
    }

    @Override
    public Collection<V> values()
    {
        Collection<V> c = new ArrayList<>();
        map.values().forEach((v)->{c.add(v.get());});
        return c;
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        Set<Entry<K,V>> set = new LinkedHashSet<>();
        map.entrySet().forEach((e)->{set.add(new WrapperEntry(e));});
        return set;
    }

    @Override
    public V getOrDefault(Object o, V v)
    {
        TouchKey key = new TouchKey(o);
        if(map.containsKey(key))
            return map.get(key).get();
        else 
            return v;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> bc)
    {
        entrySet().stream().sorted().forEach((t) ->
        {bc.accept(t.getKey(), t.getValue());});
    }
    
    class WrapperEntry<K, V> implements Entry<K, V>, Comparable<WrapperEntry<K,V>>
    {

        private final Entry<TouchKey<K>, TouchValue<V>> entry;
        
        public WrapperEntry(Entry<TouchKey<K>, TouchValue<V>> entry)
        {
            this.entry = entry;
        }

        @Override
        public K getKey() 
        {
            return entry.getKey().get();
        }

        @Override
        public V getValue() {return entry.getValue().get();}

        @Override
        public V setValue(V v) {return (V) entry.setValue(new TouchValue(entry.getKey(), v)).get();}

        @Override
        public String toString() {return entry.toString();}
        
        @Override
        public int compareTo(WrapperEntry<K, V> t)
        {
            return this.entry.getKey().compareTo(t.entry.getKey());
        }
        
        @Override
        public boolean equals(Object o)
        {
            if(!(o instanceof WrapperEntry))
                return false;
            
            WrapperEntry we = (WrapperEntry) o;
            
            return (this.entry.equals(we.entry));
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 17 * hash + Objects.hashCode(this.entry);
            return hash;
        }
        
    }
    
    
    class TouchKey<K> implements Comparable<TouchKey<K>>
    {
        private final K key;
        private Instant lastTouch;
        
        public TouchKey(K key)
        {
            this.key = key;
            this.lastTouch = Instant.now();
        }
        
        public K get() {return key;}

        @Override
        public int compareTo(TouchKey<K> t)
        {
            if(this.key.equals(t.key))
                return 0;
            return this.lastTouch.compareTo(t.lastTouch);
        }
        
        public void touch() {lastTouch = Instant.now();}
        
        @Override
        public String toString() {return key.toString();}
        
        @Override
        public boolean equals(Object o)
        {
            if(!(o instanceof TouchKey))
                return false;
            
            TouchKey tk = (TouchKey) o;
            
            return this.key.equals(tk.key);
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.key);
            return hash;
        }
        
    }
    
    class TouchValue<V>
    {
        private final TouchKey key;
        private final V value;
        
        public TouchValue(TouchKey key, V value)
        {
            this.key = key;
            this.value = value;
        }
        
        public V get() 
        {
            key.touch();
            return value;
        }
        
        public V peek() {return value;}
        
        public String toString() {return value.toString();}
        
        @Override
        public boolean equals(Object o)
        {
            if(!(o instanceof TouchValue))
                return false;
            
            TouchValue tv = (TouchValue)o;
            
            return value.equals(tv.value);
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 89 * hash + Objects.hashCode(this.value);
            return hash;
        }
    }
    
}
