package jdoo.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;

import jdoo.models.NewId;

public class Utils {
    public static <T> ArrayList<T> asList(T... e) {
        ArrayList<T> list = new ArrayList<>();
        Collections.addAll(list, e);
        return list;
    }

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

    public static <K, V> V setdefault(Map<K, V> map, K key, Supplier<? extends V> supplier) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        V value = supplier.get();
        map.put(key, value);
        return value;
    }

    public static <K, V> V get(Map<K, V> map, K key, Supplier<? extends V> supplier) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        V value = supplier.get();
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

    public static String repr(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    /**
     * test the obj in the known case:
     * <p>
     * {@code !String.isBlank()}
     * </p>
     * <p>
     * {@code !Collection.isEmpty()}
     * </p>
     * <p>
     * {@code Iterable.iterator().hasNext()}
     * </p>
     * <p>
     * {@code !Integer.equals(0)}
     * </p>
     * <p>
     * {@code !Long.equals(0)}
     * </p>
     * <p>
     * {@code !Float.equals(0.0f)}
     * </p>
     * <p>
     * {@code !Double.equals(0.0d)}
     * </p>
     * <p>
     * {@code !Short.equals((short) 0)}
     * </p>
     * <p>
     * {@code !Byte.equals((byte) 0)}
     * </p>
     * 
     * @param obj the obj to test
     * @return the test result
     */
    public static boolean bool(Object obj) {
        if (obj == null) {
            return false;
        } else if (Boolean.FALSE.equals(obj)) {
            return false;
        } else if (obj instanceof String) {
            return !((String) obj).isBlank();
        } else if (obj instanceof Collection) {
            return !((Collection<?>) obj).isEmpty();
        } else if (obj instanceof Iterable) {
            return ((Iterable<?>) obj).iterator().hasNext();
        } else if (obj instanceof Integer) {
            return !((Integer) obj).equals(0);
        } else if (obj instanceof Long) {
            return !((Long) obj).equals(0l);
        } else if (obj instanceof Float) {
            return !((Float) obj).equals(0.0f);
        } else if (obj instanceof Double) {
            return !((Double) obj).equals(0.0d);
        } else if (obj instanceof Short) {
            return !((Short) obj).equals((short) 0);
        } else if (obj instanceof Byte) {
            return !((Byte) obj).equals((byte) 0);
        } else if (obj instanceof NewId) {
            return false;
        }

        return true;
    }

    public static String l18i(String str) {
        return str;
    }
}
