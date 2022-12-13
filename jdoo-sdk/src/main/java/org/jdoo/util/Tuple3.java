package org.jdoo.util;

public class Tuple3<A, B, C> {
    A item1;
    B item2;
    C item3;

    public Tuple3(A item1, B item2, C item3) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
    }

    public A getItem1() {
        return item1;
    }

    public B getItem2() {
        return item2;
    }

    public C getItem3() {
        return item3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple3) {
            Tuple3<?, ?, ?> other = (Tuple3<?, ?, ?>) obj;
            return Tuple.eq(item1, other.item1) && Tuple.eq(item2, other.item2) && Tuple.eq(item3, other.item3);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Tuple.hash(item1) + Tuple.hash(item2) + Tuple.hash(item3);
    }
}
