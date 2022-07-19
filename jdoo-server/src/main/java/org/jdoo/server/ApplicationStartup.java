package org.jdoo.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.jdoo.exceptions.ModelException;
import org.jdoo.tenants.Tenant;
import org.jdoo.tenants.TenantService;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

/**
 * web启动事件
 * 
 * @author lrz
 */
@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Properties properties;
        String path = System.getProperty("java.class.path");
        int firstIndex = path.lastIndexOf(System.getProperty("path.separator")) + 1;
        int lastIndex = path.lastIndexOf(File.separator) + 1;
        String absolutePath = path.substring(firstIndex, lastIndex);
        String filePath = absolutePath + "config/dbcp.properties";
        File file = new File(filePath);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                properties = new Properties();
                properties.load(in);
            } catch (Exception e) {
                throw new ModelException("load properties error", e);
            }
        } else {
            try {
                properties = PropertiesLoaderUtils.loadAllProperties("dbcp.properties");
            } catch (Exception e) {
                throw new ModelException("load properties error", e);
            }
        }
        TenantService.register(new Tenant("root", "Root for super user only", properties));
    }
}
