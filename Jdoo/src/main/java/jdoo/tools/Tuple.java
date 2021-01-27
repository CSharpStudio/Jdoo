package jdoo.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.springframework.util.ObjectUtils;

public class Tuple<T> implements Collection<T> {
    @SuppressWarnings("rawtypes")
    public static final Tuple EMPTY_LIST = new Tuple<>();

    @SuppressWarnings("unchecked")
    public static final <E> Tuple<E> emptyTuple() {
        return (Tuple<E>) EMPTY_LIST;
    }

    Object[] tuple;

    public Tuple() {
        tuple = new Object[0];
    }

    @SuppressWarnings("unchecked")
    public Tuple(T... e) {
        tuple = e;
    }

    public Tuple(Collection<T> e) {
        tuple = e.toArray();
    }

    @SuppressWarnings("unchecked")
    public static <E> Tuple<E> of(E... e) {
        return new Tuple<E>(e);
    }

    public int size() {
        return tuple.length;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) tuple[index];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple) {
            Tuple<?> other = (Tuple<?>) obj;
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

    @Override
    public boolean isEmpty() {
        return tuple.length == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (Object obj : tuple) {
            if (ObjectUtils.nullSafeEquals(o, obj)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < tuple.length;
            }

            @Override
            public T next() {
                return (T) tuple[cursor++];
            }
        };
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(tuple, tuple.length);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E[] toArray(E[] a) {
        if (a.length < tuple.length)
            // Make a new array of a's runtime type, but my contents:
            return (E[]) Arrays.copyOf(tuple, tuple.length, a.getClass());
        System.arraycopy(tuple, 0, a, 0, tuple.length);
        if (a.length > tuple.length)
            a[tuple.length] = null;
        return a;
    }

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException("cannot add");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("cannot remove");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("cannot add");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("cannot remove");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("cannot retain");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("cannot clear");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Object obj : tuple) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append(getString(obj));
        }
        sb.append(')');
        return sb.toString();
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