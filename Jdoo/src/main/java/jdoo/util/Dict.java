package jdoo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Dict<K, V> extends HashMap<K, V> {
    private static final long serialVersionUID = 1L;

    public Dict() {
    }

    public Dict(Map<K, V> args){
        putAll(args);
    }

    public Dict(Consumer<Dict<K,V>> consumer) {
        consumer.accept(this);
    }

    public Dict<K,V> set(K key, V value) {
        put(key, value);
        return this;
    }

    public Entry<K, V> popitem() {
        return Utils.popitem(this);
    }

    public Object setdefault(K key, Supplier<V> supplier) {
        return Utils.setdefault(this, key, supplier);
    }
}
