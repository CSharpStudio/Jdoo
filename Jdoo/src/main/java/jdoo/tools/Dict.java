package jdoo.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class Dict implements Map<String, Object> {
    HashMap<String, Object> values = new HashMap<String, Object>();

    @Override
    public Object put(String key, Object value) {
        return values.put(key, value);
    }

    public Dict set(String key, Object value) {
        values.put(key, value);
        return this;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return values.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return values.get(key);
    }

    @Override
    public Object remove(Object key) {
        return values.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        values.putAll(m);
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public Set<String> keySet() {
        return values.keySet();
    }

    @Override
    public Collection<Object> values() {
        return values.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return values.entrySet();
    }

    public void update(Map<String, Object> map) {
        values.putAll(map);
    }

    public Entry<String, Object> popitem() {
        Iterator<Entry<String, Object>> iterator = values.entrySet().iterator();
        if (iterator.hasNext()) {
            Entry<String, Object> e = iterator.next();
            values.remove(e.getKey());
            return e;
        }
        throw new NoSuchElementException("dict is empty");
    }
}
