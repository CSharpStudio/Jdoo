package org.jdoo.util;

public class Tuple5<A, B, C, D, E> {
    A item1;
    B item2;
    C item3;
    D item4;
    E item5;

    public Tuple5(A item1, B item2, C item3, D item4, E item5) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.item4 = item4;
        this.item5 = item5;
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

    public D getItem4() {
        return item4;
    }

    public E getItem5() {
        return item5;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple3) {
            Tuple5<?, ?, ?, ?, ?> other = (Tuple5<?, ?, ?, ?, ?>) obj;
            return Tuple.eq(item1, other.item1) && Tuple.eq(item2, other.item2) && Tuple.eq(item3, other.item3)
                    && Tuple.eq(item4, other.item4) && Tuple.eq(item5, other.item5);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Tuple.hash(item1) + Tuple.hash(item2) + Tuple.hash(item3) + Tuple.hash(item4) + Tuple.hash(item5);
    }
}
