package jdoo.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import jdoo.exceptions.KeyErrorException;

public class StackMap<K,V> {
    Stack<Map<K, V>> _maps = new Stack<Map<K, V>>();

    public V get(Object key) {
        for (int i = _maps.size(); i > 0; i--) {
            Map<K, V> m = _maps.get(i);
            if (m.containsKey(key)) {
                return m.get(key);
            }
        }
        throw new KeyErrorException(key);
    }
    public V get(Object key, V $default) {
        for (int i = _maps.size(); i > 0; i--) {
            Map<K, V> m = _maps.get(i);
            if (m.containsKey(key)) {
                return m.get(key);
            }
        }
        return $default;
    }

    @Override
    public String toString() {
        return String.format("<StackMap %s>", _maps);
    }

    public Map<K, V> pushmap() {
        Map<K, V> map = new HashMap<K, V>();
        _maps.add(map);
        return map;
    }

    public Map<K, V> pushmap(Map<K, V> map) {
        _maps.add(map);
        return map;
    }

    public Map<K, V> popmap(){
        return _maps.pop();
    }
}
