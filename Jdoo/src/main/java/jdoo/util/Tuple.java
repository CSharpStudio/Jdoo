package jdoo.util;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = TupleJsonDeserializer.class)
public class Tuple<E> extends AbstractList<E> {
    @SuppressWarnings("rawtypes")
    public static final Tuple EMPTY_LIST = new Tuple<>();

    @SuppressWarnings("unchecked")
    public static final <E> Tuple<E> emptyTuple() {
        return (Tuple<E>) EMPTY_LIST;
    }

    @java.io.Serial
    private static final long serialVersionUID = -2764017481108945198L;
    private final E[] a;

    @SuppressWarnings("unchecked")
    public Tuple() {
        a = (E[]) new Object[0];
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public Tuple(E... array) {
        a = array;
    }

    @SuppressWarnings("unchecked")
    public static <E> Tuple<E> fromCollection(Collection<E> e) {
        return new Tuple<>((E[]) e.toArray());
    }

    @Override
    public int size() {
        return a.length;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(a, a.length, Object[].class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size)
            return Arrays.copyOf(this.a, size, (Class<? extends T[]>) a.getClass());
        System.arraycopy(this.a, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    public Object[] toArray(int from, int to) {
        return Arrays.copyOfRange(a, from, to);
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a, int from, int to) {
        if (a.length < to - from)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOfRange(this.a, from, to, a.getClass());
        System.arraycopy(this.a, 0, a, from, to - from);
        if (a.length > to - from)
            a[to - from] = null;
        return a;
    }

    @Override
    public E get(int index) {
        return a[index];
    }

    @Override
    public E set(int index, E element) {
        E oldValue = a[index];
        a[index] = element;
        return oldValue;
    }

    @Override
    public int indexOf(Object o) {
        E[] a = this.a;
        if (o == null) {
            for (int i = 0; i < a.length; i++)
                if (a[i] == null)
                    return i;
        } else {
            for (int i = 0; i < a.length; i++)
                if (o.equals(a[i]))
                    return i;
        }
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(a, Spliterator.ORDERED);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        for (E e : a) {
            action.accept(e);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < a.length;
            }

            @Override
            public E next() {
                return (E) a[cursor++];
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple) {
            Tuple<?> other = (Tuple<?>) obj;
            if (a.length != other.a.length)
                return false;
            for (int i = 0; i < a.length; i++) {
                if (!a[i].equals(other.get(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (a.length > 0) {
            int hash = 7;
            for (Object o : a) {
                hash = 31 * hash + (o == null ? 0 : o.hashCode());
            }
            return hash;
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Object obj : a) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append(getString(obj));
        }
        sb.append(')');
        return sb.toString();
    }

    private String getString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + obj + "\"";
        }
        return obj.toString();
    }
}

class TupleJsonDeserializer extends JsonDeserializer<Tuple<?>> {
    @Override
    public Tuple<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        Object[] array = parser.readValueAs(Object[].class);
        return new Tuple<>(array);
    }
}