package jdoo.tools;

import java.util.HashMap;

public class MapBuilder<K, V> {
    HashMap<K, V> map = new HashMap<>();

    public MapBuilder<K, V> map(K key, V value) {
        map.put(key, value);
        return this;
    }

    public HashMap<K, V> build() {
        return map;
    }
}