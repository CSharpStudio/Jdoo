package jdoo.tools;

public class Tuple2<T1, T2> {
    public T1 T1;
    public T2 T2;

    public Tuple2(T1 t1, T2 t2) {
        T1 = t1;
        T2 = t2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple2) {
            Tuple2<?, ?> other = (Tuple2<?, ?>) obj;
            return (T1 == null && other.T1 == null || T1.equals(other.T1))
                    && (T2 == null && other.T2 == null || T2.equals(other.T2));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (T1 == null ? 0 : T1.hashCode());
        hash = 31 * hash + (T2 == null ? 0 : T2.hashCode());
        return hash;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)", getString(T1), getString(T2));
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
