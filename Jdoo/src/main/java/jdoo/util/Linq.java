package jdoo.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collection;
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
}
