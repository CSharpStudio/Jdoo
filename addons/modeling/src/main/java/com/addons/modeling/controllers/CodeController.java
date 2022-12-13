package com.addons.modeling.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jdoo.https.Controller;
import org.jdoo.https.RequestHandler;
import org.jdoo.https.RequestHandler.AuthType;
import org.jdoo.https.RequestHandler.HandlerType;
import org.jdoo.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@org.springframework.stereotype.Controller
public class CodeController extends Controller {
    private static Logger logger = LoggerFactory.getLogger(CodeController.class);

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{tenant}/diagram/code/**", method = RequestMethod.GET)
    @RequestHandler(auth = AuthType.USER, type = HandlerType.HTTP)
    public void downloadCode() {
        String models = httpServletRequest.getParameter("models");
        if (StringUtils.isNoneEmpty(models)) {
            List<String> modelIds = Arrays.asList(models.split(","));
            List<Map<String, String>> codes = (List<Map<String, String>>) getEnv().get("modeling.model", modelIds)
                    .call("getCode");
            try (ZipOutputStream zos = new ZipOutputStream(httpServletResponse.getOutputStream())) {
                for (Map<String, String> row : codes) {
                    String code = row.get("code");
                    try (InputStream in = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8))) {
                        addEntry(zos, String.format("models/%s.java", row.get("class")), in);
                    } catch (Exception exc) {
                        logger.warn(String.format("压缩java代码[%s]失败", row.get("class")), exc);
                    }
                    String view = row.get("view");
                    try (InputStream in = new ByteArrayInputStream(view.getBytes(StandardCharsets.UTF_8))) {
                        String name = String.format("views/%s_views.xml",
                                row.get("model").toString().replaceAll("\\.", "_"));
                        addEntry(zos, name, in);
                    } catch (Exception exc) {
                        logger.warn(String.format("压缩视图[%s]失败", row.get("model")), exc);
                    }
                }
                httpServletResponse.setHeader("Content-Disposition",
                        "attachment;filename=code-" + System.currentTimeMillis() + ".zip");
            } catch (Exception exc) {
                logger.warn(String.format("生成代码失败：%s", models), exc);
            }
        }
    }

    void addEntry(ZipOutputStream zos, String name, InputStream in) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        int len;
        byte[] buf = new byte[1024];
        while ((len = in.read(buf)) != -1) {
            zos.write(buf, 0, len);
        }
        zos.closeEntry();
    }
}
