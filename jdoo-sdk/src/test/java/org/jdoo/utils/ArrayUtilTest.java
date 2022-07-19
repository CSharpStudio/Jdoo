package org.jdoo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ArrayUtilTest {
    @Test
    public void testZip() {
        List<Integer> l1 = ArrayUtils.asList(1, 2, 3, 4, 5, 6, 7);
        List<String> l2 = ArrayUtils.asList("a", "b", "c", "d", "e", "f");
        List<List<Object>> result = ArrayUtils.zip(l1, l2);
        assertEquals(6, result.size());
        assertEquals(6, result.get(5).get(0));
    }

    @Test
    public void testArrayZip() {
        Object[] l1 = new Object[] { 1, 2, 3, 4, 5, 6, 7 };
        Object[] l2 = new Object[] { "a", "b", "c", "d", "e", "f" };
        List<List<Object>> result = ArrayUtils.zip(l1, l2);
        assertEquals(6, result.size());
        assertEquals(6, result.get(5).get(0));
    }

    @Test
    public void test() {
        int a = 1;
        int b = 2;
        int c = 4;
        System.out.println(a | b);
        System.out.println(b | c);
        System.out.println(a | b | c);
    }
}
