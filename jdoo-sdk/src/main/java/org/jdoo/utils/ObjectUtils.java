package org.jdoo.utils;

import java.math.BigDecimal;

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

    @SuppressWarnings("unchecked")
    public static <T> T as(Object o, Class<T> tClass) {
        return tClass.isInstance(o) ? (T) o : null;
    }

    public static Boolean toBoolean(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        if (o instanceof Integer) {
            return 0 != (Integer) o;
        }
        if (o instanceof Long) {
            return 0L != (Long) o;
        }
        if (o instanceof BigDecimal) {
            return !BigDecimal.ZERO.equals(o);
        }
        if (o instanceof String) {
            return Boolean.valueOf((String) o);
        }
        return false;
    }

    public static Long toLong(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Long) {
            return (Long) o;
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).longValue();
        }
        if (o instanceof String) {
            return Long.valueOf((String) o);
        }
        return Long.valueOf(o.toString());
    }

    public static Integer toInt(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).intValue();
        }
        if (o instanceof String) {
            return Integer.valueOf((String) o);
        }
        return Integer.valueOf(o.toString());
    }

    public static Double toDouble(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Double) {
            return (Double) o;
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).doubleValue();
        }
        if (o instanceof String) {
            return Double.valueOf((String) o);
        }
        return Double.valueOf(o.toString());
    }
}
