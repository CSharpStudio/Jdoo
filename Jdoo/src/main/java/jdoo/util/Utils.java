package jdoo.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class Utils {

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> asList(T... e) {
        ArrayList<T> list = new ArrayList<>();
        for (T a : e) {
            list.add(a);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> HashSet<T> asHashSet(T... e) {
        HashSet<T> set = new HashSet<>();
        for (T a : e) {
            set.add(a);
        }
        return set;
    }

    public static <T> List<T> mutli(List<T> list, int times) {
        ArrayList<T> result = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            result.addAll(list);
        }
        return result;
    }

    public static <K, V> void update(Map<K, V> old, Map<K, V> $new) {
        old.putAll($new);
    }

    public static <K, V> Entry<K, V> popitem(Map<K, V> map) {
        Iterator<Entry<K, V>> iterator = map.entrySet().iterator();
        if (iterator.hasNext()) {
            Entry<K, V> e = iterator.next();
            map.remove(e.getKey());
            return e;
        }
        throw new NoSuchElementException("map is empty");
    }

    // public static <K, V> V setdefault(Map<K, V> map, K key, V value) {
    //     if (map.containsKey(key)) {
    //         return map.get(key);
    //     }
    //     map.put(key, value);
    //     return value;
    // }

    public static <K, V> V setdefault(Map<K, V> map, K key, Supplier<? extends V> supplier) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        V value = supplier.get();
        map.put(key, value);
        return value;
    }

    public static <T, V> Map<T, V> toMap(Collection<Pair<T, V>> pairs) {
        Map<T, V> map = new HashMap<>();
        for (Pair<T, V> p : pairs) {
            map.put(p.first(), p.second());
        }
        return map;
    }

    public static List<Tuple<Object>> zip(Collection<?>... collections) {
        List<Tuple<Object>> result = new ArrayList<>();
        List<Iterator<?>> all = new ArrayList<>(collections.length);
        for (Collection<?> c : collections) {
            all.add(c.iterator());
        }
        boolean exist = false;
        while (!exist) {
            Object[] objs = new Object[collections.length];
            int index = 0;
            for (Iterator<?> iterator : all) {
                if (iterator.hasNext()) {
                    objs[index++] = iterator.next();
                } else {
                    exist = true;
                    break;
                }
            }
            if (exist)
                break;
            result.add(new Tuple<>(objs));
        }
        return result;
    }
}
