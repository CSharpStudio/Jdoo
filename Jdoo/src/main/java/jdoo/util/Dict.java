package jdoo.util;

import java.util.HashMap;

public class Dict extends HashMap<String, Object> {
    private static final long serialVersionUID = -3285629588651230533L;

    public Dict set(String key, Object value) {
        put(key, value);
        return this;
    }

    public Entry<String, Object> popitem() {
        return Utils.popitem(this);
    }

    public Object setdefault(String key, Object value) {
        return Utils.setdefault(this, key, value);
    }

    @SuppressWarnings("unchecked")
    public <V> V get(Object key, V $default){
        if (containsKey(key)) {
            return (V) get(key);
        }
        return $default;
    }
}
