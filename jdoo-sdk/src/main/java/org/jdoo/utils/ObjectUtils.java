package org.jdoo.utils;

/**
 * 对象工具
 * 
 * @author lrz
 */
public class ObjectUtils extends org.apache.commons.lang3.ObjectUtils {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Class<T> clazz) {
        return (T) obj;
    }
}
