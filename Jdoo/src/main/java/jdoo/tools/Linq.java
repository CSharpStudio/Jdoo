package jdoo.tools;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collection;

public class Linq {

    public static <T> Enumeration<T> where(Collection<T> e, Func<Boolean, T> func) {
        return new Enumeration<T>() {
            Iterator<T> i = e.iterator();

            @Override
            public boolean hasMoreElements() {
                while (i.hasNext()) {
                    if (func.call(i.next())) {
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
