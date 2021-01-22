package jdoo.tools;

public class Tuple {
    Object[] tuple;

    public Tuple() {
        tuple = new Object[0];
    }

    public Tuple(Object e) {
        tuple = new Object[] { e };
    }

    public Tuple(Object... e) {
        tuple = e;
    }

    public int size() {
        return tuple.length;
    }

    public Object get(int index) {
        return tuple[index];
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz, int index) {
        return (T) tuple[index];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple) {
            Tuple other = (Tuple) obj;
            if (tuple.length != other.tuple.length)
                return false;
            for (int i = 0; i < tuple.length; i++) {
                if (!tuple[i].equals(obj))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (tuple.length > 0) {
            int hash = 7;
            for (Object o : tuple) {
                hash = 31 * hash + (o == null ? 0 : o.hashCode());
            }
            return hash;
        }
        return super.hashCode();
    }
}