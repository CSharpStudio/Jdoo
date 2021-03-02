package jdoo.util;

import java.util.HashMap;
import java.util.function.Supplier;

import jdoo.exceptions.JdooException;

public class DefaultDict<K,V> extends HashMap<K,V> {
    private static final long serialVersionUID = 1838703243142872947L;

    Class<?> defaultClass;
    Supplier<V> defaultSupplier;

    public DefaultDict(Class<?> defaultClass) {
        this.defaultClass = defaultClass;
    }

    public DefaultDict(Supplier<V> defaultSupplier) {
        this.defaultSupplier = defaultSupplier;
    }

    public DefaultDict<K,V> set(K key, V value) {
        put(key, value);
        return this;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or put and return default value if this map contains no mapping for the key.
     */
    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        if (containsKey(key)) {
            return super.get(key);
        }
        V value = getDefault();
        put((K)key, value);
        return value;
    }

    @SuppressWarnings("unchecked")
    private V getDefault() {
        if (defaultClass != null) {
            try {
                return (V)defaultClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new JdooException(String.format("Class %s cannot new instance", defaultClass.getName()), e);
            }
        } else {
            return defaultSupplier.get();
        }
    }
}
