package jdoo.tools;

import java.util.HashSet;
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
                return (V)map.get(key);
            }
            return $default;
        }
    }
}
