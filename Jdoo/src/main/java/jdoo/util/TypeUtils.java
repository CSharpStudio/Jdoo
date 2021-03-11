package jdoo.util;

import java.math.BigDecimal;
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
            } else if (clazz.equals(char.class) || clazz.equals(Character.class)) {
                if (str.length() > 0) {
                    return (T) Character.valueOf(str.charAt(0));
                }
                return (T) Character.valueOf('\0');
            } else if (clazz.equals(Date.class)) {
                return (T) dateFormat.parse(str);
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(str, clazz);
        } catch (Exception exception) {
            throw new ParseException("string value:\"" + str + "\" cannot parse to type:" + clazz, exception);
        }
    }

    public static <T> T as(Object o, Class<T> tClass) {
        return tClass.isInstance(o) ? (T) o : null;
    }

    public static boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return "true".equalsIgnoreCase((String) value) || "y".equalsIgnoreCase((String) value);
        } else if (value instanceof Integer) {
            return !value.equals(0);
        }
        return false;
    }

    public static int toInteger(Object value) {
        if (value == null) {
            return 0;
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof Float) {
            return ((Float) value).intValue();
        } else if (value instanceof String) {
            return parse((String) value, Integer.class);
        }
        return parse(value.toString(), Integer.class);
    }

    public static double toDouble(Object value) {
        if (value == null) {
            return 0;
        } else if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Double) {
            return ((Double) value).doubleValue();
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof String) {
            return parse((String) value, Double.class);
        }
        return parse(value.toString(), Double.class);
    }

    public static boolean isInstance(Class<?> clazz, Object obj) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(boolean.class)) {
                return obj instanceof Boolean;
            } else if (clazz.equals(byte.class)) {
                return obj instanceof Byte;
            } else if (clazz.equals(short.class)) {
                return obj instanceof Short;
            } else if (clazz.equals(int.class)) {
                return obj instanceof Integer;
            } else if (clazz.equals(long.class)) {
                return obj instanceof Long;
            } else if (clazz.equals(float.class)) {
                return obj instanceof Float;
            } else if (clazz.equals(double.class)) {
                return obj instanceof Double;
            } else if (clazz.equals(char.class)) {
                return obj instanceof Character;
            }
        }
        return clazz.isInstance(obj);
    }

    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        return b.setScale(scale).doubleValue();
    }
}
