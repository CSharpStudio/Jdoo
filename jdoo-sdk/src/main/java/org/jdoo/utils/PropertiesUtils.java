package org.jdoo.utils;

import java.util.Properties;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import org.jdoo.exceptions.PlatformException;

public class PropertiesUtils {
    public static Object getProperty(String key) {
        try {
            Properties properties = PropertiesLoaderUtils.loadAllProperties("application.properties");
            if ("dev".equals(properties.get("spring.profiles.active"))) {
                Properties devProperties = PropertiesLoaderUtils.loadAllProperties("application-dev.properties");
                Object value = devProperties.get(key);
                if (value != null) {
                    return value;
                }
            }
            return properties.get(key);
        } catch (Exception e) {
            throw new PlatformException("读取", e);
        }
    }
}
