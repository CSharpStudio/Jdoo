package jdoo.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utils {
    public class Collections {

    }

    public static class Sets {
        @SuppressWarnings("unchecked")
        public static <T> HashSet<T> asHashSet(T... e) {
            HashSet<T> set = new HashSet<>();
            for (T a : e) {
                set.add(a);
            }
            return set;
        }
    }

    public static class Maps {
        @SuppressWarnings("unchecked")
        public static <K, V> V get(Map<K, ?> map, K key, V $default) {
            if (map.containsKey(key)) {
                return (V) map.get(key);
            }
            return $default;
        }
    }

    public static <K, V> MapBuilder<K, V> map(K key, V value) {
        return new MapBuilder<K, V>().map(key, value);
    }

    public static <K, V> void update(Map<K, V> old, Map<K, V> $new) {
        for (K key : $new.keySet()) {
            old.put(key, $new.get(key));
        }
    }

    public static List<Tuple<Object>> zip(Collection<?>... collections) {
        List<Tuple<Object>> result = new ArrayList<>();
        List<Iterator<?>> all = new ArrayList<>(collections.length);
        for (Collection<?> c : collections) {
            all.add(c.iterator());
        }
        for (int i = 0; i < collections.length; i++) {
            Object[] objs = new Object[collections.length];
            int index = 0;
            boolean exist = false;
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
