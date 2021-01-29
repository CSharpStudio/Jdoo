package jdoo.util;

import org.springframework.util.ObjectUtils;

public class Pair<P0, P1> {
    private P0 first;
    private P1 second;

    public Pair(P0 first, P1 second) {
        this.first = first;
        this.second = second;
    }

    public P0 first() {
        return first;
    }

    public P1 second() {
        return second;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair<?, ?> other = (Pair<?, ?>) obj;
            return ObjectUtils.nullSafeEquals(first, other.first) && ObjectUtils.nullSafeEquals(second, other.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (first == null ? 0 : first.hashCode());
        hash = 31 * hash + (second == null ? 0 : second.hashCode());
        return hash;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)", getString(first), getString(second));
    }

    String getString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + obj + "\"";
        }
        return obj.toString();
    }
}
