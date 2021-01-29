package jdoo.tools;

public class Tuple2<T0, T1> {
    public T0 T0;
    public T1 T1;

    public Tuple2(T0 t0, T1 t1) {
        T0 = t0;
        T1 = t1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple2) {
            Tuple2<?, ?> other = (Tuple2<?, ?>) obj;
            return (T0 == null && other.T0 == null || T0.equals(other.T0))
                    && (T1 == null && other.T1 == null || T1.equals(other.T1));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (T0 == null ? 0 : T0.hashCode());
        hash = 31 * hash + (T1 == null ? 0 : T1.hashCode());
        return hash;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)", getString(T0), getString(T1));
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
