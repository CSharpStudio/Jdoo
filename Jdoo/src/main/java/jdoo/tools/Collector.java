package jdoo.tools;

import java.util.ArrayList;
import java.util.HashMap;

import jdoo.util.Utils;

public class Collector extends HashMap<Object, ArrayList<Object>> {
    private static final long serialVersionUID = 1L;

    public void add(Object key, Object val) {
        ArrayList<Object> vals = Utils.setdefault(this, key, () -> new ArrayList<Object>());
        if (!vals.contains(val)) {
            vals.add(val);
        }
    }
}
