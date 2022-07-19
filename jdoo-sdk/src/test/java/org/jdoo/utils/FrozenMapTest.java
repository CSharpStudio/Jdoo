package org.jdoo.utils;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class FrozenMapTest {
    @Test
    public void testKeySet() {
        Set<Object> x = new HashSet<>();
        x.add(1);
        x.add(2);

        Set<Object> y = new HashSet<>();
        y.add(new Integer(2));
        y.add(new Integer(1));

        boolean c = x.equals(y);

        System.out.println(c);
        System.out.println(x.hashCode());
        System.out.println(y.hashCode());

        Object a = 1.0;
        System.out.println(a.hashCode());

        Map<Object, Object> mapx = new HashMap<>();
        mapx.put("key", 1);
        mapx.put("key2", Timestamp.valueOf("2022-02-17 12:15:30"));
        mapx= Collections.unmodifiableMap(mapx);

        Map<Object, Object> mapy = new HashMap<>();
        mapy.put("key2", Timestamp.valueOf("2022-02-17 12:15:30"));
        mapy.put("key", 1);
        mapy= Collections.unmodifiableMap(mapy);

        System.out.println(mapx.equals(mapy));
        System.out.println(mapx.hashCode());
        System.out.println(mapy.hashCode());

        
        Map<Object, Object> m = new HashMap<>();
        m.put(mapx, 1);
        System.out.println(m.get(mapy));

        // for (Object key : mapx.keySet().toArray()) {
        //     System.out.println(key);
        //     System.out.println(mapx.remove(key));
        //     System.out.println(mapx.size());
        // }

    }
}
