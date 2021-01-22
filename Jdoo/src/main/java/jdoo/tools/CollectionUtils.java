package jdoo.tools;

import java.util.HashSet;
import java.util.Set;

public class CollectionUtils {
    @SafeVarargs
    public static <T> Set<T> asSet(T ... args){
        Set<T> set = new HashSet<T>();
        for (T arg : args) {
            set.add(arg);
        }
        return set;
    }
}
