package org.jdoo.https;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdoo.utils.PathUtils;
import org.jdoo.utils.Utils;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 静态资源
 * 
 * @author lrz
 */
@org.springframework.stereotype.Controller
public class StaticsController {
    @RequestMapping(value = "**/statics/**", method = RequestMethod.GET)
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getServletPath();
        String seperator = "/";
        if (path.startsWith(seperator)) {
            path = path.substring(1);
        }
        // ClassLoader loader = ClassUtils.getDefaultClassLoader();
        // InputStream input = loader.getResourceAsStream(path);
        InputStream input = getResource(path);
        Optional<MediaType> o = MediaTypeFactory.getMediaType(path);
        if (o.isPresent()) {
            response.setContentType(o.get().toString());
        }
        response.setCharacterEncoding("UTF-8");
        if (input != null) {
            try (OutputStream output = response.getOutputStream()) {
                Utils.stream(input, output);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("响应失败", e);
            }
        } else {
            response.setStatus(404);
        }
    }

    InputStream getResource(String path) {
        Properties properties;
        try {
            properties = PropertiesLoaderUtils.loadAllProperties("application.properties");
            if ("dev".equals(properties.get("spring.profiles.active"))) {
                properties = PropertiesLoaderUtils.loadAllProperties("application-dev.properties");
                String root = (String) properties.get("staticPath");
                String fileName = PathUtils.combine(root, path);
                File file = new File(fileName);
                if (file.exists()) {
                    return new FileInputStream(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ClassLoader loader = ClassUtils.getDefaultClassLoader();
        InputStream input = loader.getResourceAsStream(path);
        return input;
    }
}
