package org.jdoo.util;

public class Tuple<A, B> {
    A item1;
    B item2;

    static boolean eq(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    static int hash(Object a) {
        if (a != null) {
            return a.hashCode();
        }
        return 0;
    }

    public Tuple(A item1, B item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public A getItem1() {
        return item1;
    }

    public B getItem2() {
        return item2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple) {
            Tuple<?, ?> other = (Tuple<?, ?>) obj;
            return eq(item1, other.item1) && eq(item2, other.item2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hash(item1) + hash(item2);
    }
}
