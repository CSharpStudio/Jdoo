package jdoo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Kvalues extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public Kvalues() {
    }

    public Kvalues(Map<String, Object> args){
        putAll(args);
    }

    public Kvalues(Consumer<Kvalues> consumer) {
        consumer.accept(this);
    }

    public Kvalues set(String key, Object value) {
        put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<? extends T> c, String key) {
        if (containsKey(key)) {
            return (T) get(key);
        }
        return null;
    }

    public Entry<String, Object> popitem() {
        return Utils.popitem(this);
    }

    public Object setdefault(String key, Supplier<Object> supplier) {
        return Utils.setdefault(this, key, supplier);
    }
}
