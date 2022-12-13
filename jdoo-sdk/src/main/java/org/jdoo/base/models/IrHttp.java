package org.jdoo.base.models;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdoo.AbstractModel;
import org.jdoo.Manifest;
import org.jdoo.Model;
import org.jdoo.Records;
import org.jdoo.core.Environment;
import org.jdoo.utils.HttpUtils;
import org.jdoo.utils.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model.Meta(name = "ir.http")
public class IrHttp extends AbstractModel {
    private Logger logger = LoggerFactory.getLogger(IrModelData.class);

    public void home(Records rec, HttpServletRequest request, HttpServletResponse response) {
        Environment env = rec.getEnv();
        String key = "web.home" + (env.isDebug() ? ":debug" : "");
        String view = env.getTenantData(key,
                () -> (String) env.get("ir.ui.view").call("loadWeb", "base.web_home"));
        view = view.replaceAll("\\{\\{TITLE\\}\\}", "JDOO");
        view = view.replace("{{COPYRIGHT}}", "Copyright &copy; 2022-2023 <a href=\"http://jdoo.org\">JDOO</a>.");
        view = view.replace("{{VERSION}}", "<b>Version</b> 1.0");
        view = view.replace("{{USERNAME}}", (String) env.getUser().get("name"));
        HttpUtils.WriteHtml(response, view);
    }

    public void login(Records rec, HttpServletRequest request, HttpServletResponse response) {
        Environment env = rec.getEnv();
        String key = "web.login" + (env.isDebug() ? ":debug" : "");
        String view = env.getTenantData(key,
                () -> (String) env.get("ir.ui.view").call("loadWeb", "base.web_login"));
        view = view.replaceAll("\\{\\{TITLE\\}\\}", "JDOO");
        view = view.replace("{{COPYRIGHT}}", "Copyright &copy; 2022-2023 <a href=\"http://jdoo.org\">JDOO</a>.");
        view = view.replace("{{VERSION}}", "<b>Version</b> 1.0");
        HttpUtils.WriteHtml(response, view);
    }

    public void view(Records rec, HttpServletRequest request, HttpServletResponse response) {
        Environment env = rec.getEnv();
        String key = "web.view" + (env.isDebug() ? ":debug" : "");
        String view = env.getTenantData(key,
                () -> (String) env.get("ir.ui.view").call("loadWeb", "base.web_view"));
        HttpUtils.WriteHtml(response, view);
    }

    public void registerControllers(Records rec, Manifest manifest) {
        for (Class<?> clazz : manifest.controllers()) {
            try {
                SpringUtils.registerController(clazz);
            } catch (Exception e) {
                logger.error(String.format("注册控制器[%s]失败", clazz.getName()), e);
            }
        }
    }

    public void unregisterControllers(Records rec, Manifest manifest) {
        for (Class<?> clazz : manifest.controllers()) {
            SpringUtils.unregisterController(clazz);
        }
    }
}
