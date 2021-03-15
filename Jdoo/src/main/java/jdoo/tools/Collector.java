package jdoo.tools;

import java.util.ArrayList;
import java.util.HashMap;

import jdoo.util.Utils;

public class Collector<K, V> extends HashMap<K, ArrayList<V>> {
    private static final long serialVersionUID = 1L;

    public void add(K key, V val) {
        ArrayList<V> vals = Utils.setdefault(this, key, ArrayList::new);
        if (!vals.contains(val)) {
            vals.add(val);
        }
    }
}
