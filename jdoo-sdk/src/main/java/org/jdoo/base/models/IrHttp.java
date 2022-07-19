package org.jdoo.base.models;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdoo.AbstractModel;
import org.jdoo.Model;
import org.jdoo.Records;
import org.jdoo.core.Environment;
import org.jdoo.utils.HttpUtils;

@Model.Meta(name = "ir.http")
public class IrHttp extends AbstractModel {
    public void home(Records rec, HttpServletRequest request, HttpServletResponse response) {
        Environment env = rec.getEnv();
        String view = env.getTenantData("web.home",
                () -> (String) rec.getEnv().get("ir.ui.view").call("loadWeb", "base.web_home"));
        view = view.replaceAll("\\{\\{TITLE\\}\\}", "JDOO");
        view = view.replace("{{COPYRIGHT}}", "Copyright &copy; 2022-2023 <a href=\"http://jdoo.io\">JDOO</a>.");
        view = view.replace("{{VERSION}}", "<b>Version</b> 1.0");
        view = view.replace("{{USERNAME}}", (String) rec.getEnv().getUser().get("name"));
        HttpUtils.WriteHtml(response, view);
    }

    public void login(Records rec, HttpServletRequest request, HttpServletResponse response) {
        Environment env = rec.getEnv();
        String view = env.getTenantData("web.login",
                () -> (String) rec.getEnv().get("ir.ui.view").call("loadWeb", "base.web_login"));
        view = view.replaceAll("\\{\\{TITLE\\}\\}", "JDOO");
        view = view.replace("{{COPYRIGHT}}", "Copyright &copy; 2022-2023 <a href=\"http://jdoo.io\">JDOO</a>.");
        view = view.replace("{{VERSION}}", "<b>Version</b> 1.0");
        HttpUtils.WriteHtml(response, view);
    }

    public void view(Records rec, HttpServletRequest request, HttpServletResponse response) {
        Environment env = rec.getEnv();
        String view = env.getTenantData("web.view",
                () -> (String) rec.getEnv().get("ir.ui.view").call("loadWeb", "base.web_view"));
        HttpUtils.WriteHtml(response, view);
    }
}
