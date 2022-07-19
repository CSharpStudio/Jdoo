package org.jdoo.utils;

/**
 * 字符串工具
 * 
 * @author lrz
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static void join(java.lang.String[] array, char separator, java.lang.StringBuilder sb) {
        org.apache.tomcat.util.buf.StringUtils.join(array, separator, sb);
    }

    public static java.lang.String join(java.util.Collection<java.lang.String> collection) {
        return org.apache.tomcat.util.buf.StringUtils.join(collection);
    }

    public static java.lang.String join(java.util.Collection<java.lang.String> collection, char separator) {
        return org.apache.tomcat.util.buf.StringUtils.join(collection, separator);
    }

    public static void join(java.lang.Iterable<java.lang.String> iterable, char separator, java.lang.StringBuilder sb) {
        org.apache.tomcat.util.buf.StringUtils.join(iterable, separator, sb);
    }

    public static <T> void join(T[] array, char separator, java.util.function.Function<T, java.lang.String> function,
            java.lang.StringBuilder sb) {
        org.apache.tomcat.util.buf.StringUtils.join(array, separator, function, sb);
    }

    public static <T> void join(java.lang.Iterable<T> iterable, char separator,
            java.util.function.Function<T, java.lang.String> function, java.lang.StringBuilder sb) {
        org.apache.tomcat.util.buf.StringUtils.join(iterable, separator, function, sb);
    }
}
