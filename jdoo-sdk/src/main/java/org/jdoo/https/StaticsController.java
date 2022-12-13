package org.jdoo.https;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdoo.utils.PathUtils;
import org.jdoo.utils.PropertiesUtils;
import org.jdoo.utils.Utils;
import org.jdoo.utils.StringUtils;
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
    @RequestMapping(value = "/res/**", method = RequestMethod.GET)
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getServletPath();
        path = path.startsWith("/") ? path.substring(5) : path.substring(4);
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
        String rootPath = (String) PropertiesUtils.getProperty("rootPath");
        if (StringUtils.isNotEmpty(rootPath)) {
            String addons = (String) PropertiesUtils.getProperty("addons");
            if (StringUtils.isNotEmpty(addons)) {
                for (String root : rootPath.split(",")) {
                    for (String addon : addons.split(",")) {
                        String fileName = PathUtils.combine(root, addon, "src/main/java", path);
                        File file = new File(fileName);
                        if (file.exists()) {
                            try {
                                return new FileInputStream(file);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        }
        ClassLoader loader = ClassUtils.getDefaultClassLoader();
        if (loader != null) {
            return loader.getResourceAsStream(path);
        }
        return null;
    }
}
