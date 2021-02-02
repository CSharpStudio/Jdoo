package jdoo.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Predicate;

public class Linq {

    public static <T> Enumeration<T> where(Collection<T> e, Predicate<T> func) {
        return new Enumeration<T>() {
            Iterator<T> i = e.iterator();

            @Override
            public boolean hasMoreElements() {
                while (i.hasNext()) {
                    if (func.test(i.next())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public T nextElement() {
                return i.next();
            }

        };
    }

    public static <T, V extends Comparable<V>> List<T> orderBy(List<T> list, java.util.function.Function<T, V> func) {
        Collections.sort(list, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return func.apply(o1).compareTo(func.apply(o2));
            }
        });
        return list;
    }
}
