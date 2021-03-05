package jdoo.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FrozenDict<K, V> implements Map<K, V> {
    Map<K, V> map;

    public FrozenDict(Map<K, V> map) {
        this.map = new HashMap<>(map);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FrozenDict)) {
            return false;
        }
        FrozenDict<?, ?> other = (FrozenDict<?, ?>) obj;
        if (map.size() != other.size()) {
            return false;
        }
        for (Entry<K, V> entry : entrySet()) {
            if (!other.containsKey(entry.getKey())) {
                return false;
            } else if (!Objects.equals(entry.getValue(), other.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int h = 0;
        for (Entry<K, V> entry : entrySet())
            h += entry.hashCode();
        return h;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}