package jdoo.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdoo.exceptions.ParseException;

public class TypeUtils {
    private static boolean DEFAULT_BOOLEAN;
    private static byte DEFAULT_BYTE;
    private static short DEFAULT_SHORT;
    private static int DEFAULT_INT;
    private static long DEFAULT_LONG;
    private static float DEFAULT_FLOAT;
    private static double DEFAULT_DOUBLE;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Object getDefaultValue(Class<?> clazz) {
        if (clazz.equals(boolean.class)) {
            return DEFAULT_BOOLEAN;
        } else if (clazz.equals(byte.class)) {
            return DEFAULT_BYTE;
        } else if (clazz.equals(short.class)) {
            return DEFAULT_SHORT;
        } else if (clazz.equals(int.class)) {
            return DEFAULT_INT;
        } else if (clazz.equals(long.class)) {
            return DEFAULT_LONG;
        } else if (clazz.equals(float.class)) {
            return DEFAULT_FLOAT;
        } else if (clazz.equals(double.class)) {
            return DEFAULT_DOUBLE;
        }
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    public static <T> T parse(String str, Class<T> clazz) throws ParseException {
        try {
            if (clazz.equals(String.class)) {
                return (T) str;
            } else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
                return (T) Boolean.valueOf(str);
            } else if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
                return (T) Byte.valueOf(str);
            } else if (clazz.equals(short.class) || clazz.equals(Short.class)) {
                return (T) Short.valueOf(str);
            } else if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
                return (T) Integer.valueOf(str);
            } else if (clazz.equals(long.class) || clazz.equals(Long.class)) {
                return (T) Long.valueOf(str);
            } else if (clazz.equals(float.class) || clazz.equals(Float.class)) {
                return (T) Float.valueOf(str);
            } else if (clazz.equals(double.class) || clazz.equals(Double.class)) {
                return (T) Double.valueOf(str);
            } else if (clazz.equals(Date.class)) {
                return (T) dateFormat.parse(str);
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(str, clazz);
        } catch (Exception exception) {
            throw new ParseException("string value:\"" + str + "\" cannot parse to type:" + clazz, exception);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public static <T> T as(Object o, Class<T> tClass) {
        return tClass.isInstance(o) ? (T) o : null;
    }
}
