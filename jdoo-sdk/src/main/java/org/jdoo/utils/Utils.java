package org.jdoo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdoo.core.MetaField;
import org.jdoo.fields.*;
import com.mysql.cj.x.protobuf.MysqlxCrud.Collection;

/**
 * 工具杂项
 * 
 * @author lrz
 */
public class Utils {
    public static Map<String, Object> clearContext(Map<String, Object> ctx) {
        Map<String, Object> result = new HashMap<>(ctx.size());
        for (String key : ctx.keySet()) {
            if (!key.startsWith("default_")) {
                result.put(key, ctx.get(key));
            }
        }
        return result;
    }

    public static Object getExampleValue(MetaField field, boolean usePresent) {
        if (field instanceof BooleanField) {
            return true;
        } else if (field instanceof DateField) {
            return "2022-01-01";
        } else if (field instanceof DateTimeField) {
            return "2022-01-01 12:00:00";
        } else if (field instanceof FloatField) {
            return 0.1;
        } else if (field instanceof IdField) {
            return "01no6a1ocg9hc";
        } else if (field instanceof BinaryBaseField) {
            return "binary".getBytes();
        } else if (field instanceof IntegerField) {
            return 1;
        } else if (field instanceof Many2manyField || field instanceof One2manyField) {
            return Arrays.asList("01o5ryusb9lhc", "01ng9if6fve2o");
        } else if (field instanceof Many2oneField) {
            if (usePresent) {
                return Arrays.asList("01o5ryusb9lhc", "name");
            }
            return "01o5ryusb9lhc";
        } else if (field instanceof SelectionField) {
            return "selection";
        } else if (field instanceof StringField) {
            return "str";
        }
        return "";
    }

    public static Object getExampleValue(Class<?> clazz) {
        if (clazz == String.class) {
            return "str";
        } else if (clazz == Boolean.class) {
            return true;
        } else if (clazz == Integer.class) {
            return 1;
        } else if (clazz == Float.class || clazz == Double.class) {
            return 0.1;
        } else if (clazz == Timestamp.class) {
            return "2022-01-01 12:00:00";
        } else if (clazz == Date.class) {
            return "2022-01-01";
        } else if (Collection.class.isAssignableFrom(clazz) || List.class.isAssignableFrom(clazz)
                || Set.class.isAssignableFrom(clazz) || clazz.isArray()) {
            return Collections.emptyList();
        } else if (Map.class.isAssignableFrom(clazz)) {
            return Collections.emptyMap();
        }
        return "";
    }

    public static String getJsonType(Class<?> clazz) {
        if (clazz == String.class) {
            return "字符串";
        } else if (clazz == Boolean.class) {
            return "布尔";
        } else if (clazz == Integer.class || clazz == Long.class || clazz == Float.class || clazz == Double.class) {
            return "数字";
        } else if (clazz == Timestamp.class) {
            return "日期时间";
        } else if (clazz == Date.class) {
            return "日期";
        } else if (Collection.class.isAssignableFrom(clazz) || List.class.isAssignableFrom(clazz)
                || Set.class.isAssignableFrom(clazz) || clazz.isArray()) {
            return "数组";
        }
        return "对象";
    }

    public static long stream(InputStream input, OutputStream output) throws IOException {
        try (
            ReadableByteChannel inputChannel = Channels.newChannel(input);
            WritableByteChannel outputChannel = Channels.newChannel(output);
        ) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(10240);
            long size = 0;
    
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                size += outputChannel.write(buffer);
                buffer.clear();
            }
    
            return size;
        }
    }
}
