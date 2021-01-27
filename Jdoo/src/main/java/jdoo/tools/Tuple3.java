package jdoo.tools;

public class Tuple3<T1, T2, T3> {
    public T1 T1;
    public T2 T2;
    public T3 T3;

    public Tuple3(T1 t1, T2 t2, T3 t3) {
        T1 = t1;
        T2 = t2;
        T3 = t3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple3) {
            Tuple3<?, ?, ?> other = (Tuple3<?, ?, ?>) obj;
            return (T1 == null && other.T1 == null || T1.equals(other.T1))
                    && (T2 == null && other.T2 == null || T2.equals(other.T2))
                    && (T3 == null && other.T3 == null || T3.equals(other.T3));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (T1 == null ? 0 : T1.hashCode());
        hash = 31 * hash + (T2 == null ? 0 : T2.hashCode());
        hash = 31 * hash + (T3 == null ? 0 : T3.hashCode());
        return hash;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s,%s)", getString(T1), getString(T2), getString(T3));
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