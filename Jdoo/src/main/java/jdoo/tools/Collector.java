package jdoo.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import jdoo.util.Utils;

public class Collector<K, V> extends HashMap<K, List<V>> {
    private static final long serialVersionUID = 1L;

    public void add(K key, V val) {
        List<V> vals = Utils.setdefault(this, key, ArrayList::new);
        if (!vals.contains(val)) {
            vals.add(val);
        }
    }

    @Override
    public List<V> get(Object key) {
        if (containsKey(key)) {
            return super.get(key);
        }
        return Collections.emptyList();
    }
}
