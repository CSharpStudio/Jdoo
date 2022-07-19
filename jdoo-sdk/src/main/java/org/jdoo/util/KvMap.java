package org.jdoo.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link String} : {@link Object} 键值对
 * 
 * @author lrz
 */
public class KvMap implements Map<String, Object> {
    Map<String, Object> data;

    static KvMap empty;
    static {
        empty = new KvMap(0);
        empty.data = Collections.emptyMap();
    }

    public static KvMap empty() {
        return empty;
    }

    public KvMap() {
        data = new HashMap<>();
    }

    public KvMap(int initialCapacity) {
        data = new HashMap<>(initialCapacity);
    }

    public KvMap(Map<? extends String, ? extends Object> m) {
        data = new HashMap<>(m);
    }

    public KvMap set(String key, Object value) {
        put(key, value);
        return this;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return data.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return data.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return data.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        data.putAll(m);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public Set<String> keySet() {
        return data.keySet();
    }

    @Override
    public Collection<Object> values() {
        return data.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return data.entrySet();
    }
}
