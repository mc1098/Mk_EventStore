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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class FloatingOrderedMap<K, V> implements Map<K, V>
{
    private final SortedMap<K, FloatingValue<V>> map;
    
    
    public FloatingOrderedMap()
    {
        this.map = new TreeMap<>();
    }
    
    public V lastValue() {return map.get(map.lastKey()).get();}
    

    @Override
    public int size() {return map.size();}

    @Override
    public boolean isEmpty() {return map.isEmpty();}

    @Override
    public boolean containsKey(Object o) {return map.containsKey(o);}

    @Override
    public boolean containsValue(Object o) {return map.containsValue(o);}

    @Override
    public V get(Object o) {return map.get(o).get();}

    @Override
    public V put(K k, V v)
    {
        return (V) map.put(k, new FloatingValue(v));
    }

    @Override
    public V remove(Object o) {return map.remove(o).get();}

    @Override
    public void putAll(Map<? extends K, ? extends V> map)
    {
        Map<K, FloatingValue<V>> temp = new TreeMap<>();
        map.forEach((t, u) ->{temp.put(t, new FloatingValue(u));});
        this.map.putAll(temp);
    }

    @Override
    public void clear() {map.clear();}

    @Override
    public Set<K> keySet() {return map.keySet();}

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
        Set<Entry<K,V>> set = new TreeSet<>();
        map.entrySet().forEach((e)->{set.add(new WrapperEntry(e));});
        return set;
    }

    @Override
    public V getOrDefault(Object o, V v)
    {
        if(map.containsKey(o))
            return map.get(o).get();
        else 
            return v;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> bc)
    {
        map.forEach((t, u) ->{bc.accept(t, u.get());});
    }
    
    class WrapperEntry<K, V> implements Entry<K, V>
    {

        private final Entry<K, FloatingValue<V>> entry;
        
        public WrapperEntry(Entry<K, FloatingValue<V>> entry)
        {
            this.entry = entry;
        }

        @Override
        public K getKey() {return entry.getKey();}

        @Override
        public V getValue() {return entry.getValue().get();}

        @Override
        public V setValue(V v) {return (V) entry.setValue(new FloatingValue(v)).get();}
        
    }
    
}

class FloatingValue<V> implements Comparable<FloatingValue>
{
    private final V value;
    private Instant lastAccessed;
    
    public FloatingValue(V value)
    {
        this.value = value;
        this.lastAccessed = Instant.now();
    }
    
    public V get()
    {
        lastAccessed = Instant.now();
        return value;
    }
    
    @Override
    public int compareTo(FloatingValue other)
    {
        return other.lastAccessed.compareTo(lastAccessed);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof FloatingValue))
            return false;
        
        FloatingValue fv = (FloatingValue) o;
        
        return (this.value.equals(fv.value));
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.value);
        return hash;
    }
}