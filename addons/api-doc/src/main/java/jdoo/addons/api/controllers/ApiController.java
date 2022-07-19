package jdoo.addons.api.controllers;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdoo.core.BaseService;
import org.jdoo.core.MetaField;
import org.jdoo.core.MetaModel;
import org.jdoo.core.Registry;
import org.jdoo.core.BaseService.ApiDoc;
import org.jdoo.fields.RelationalField;
import org.jdoo.https.jsonrpc.JsonRpcParameter;
import org.jdoo.https.jsonrpc.JsonRpcRequest;
import org.jdoo.https.jsonrpc.JsonRpcResponse;
import org.jdoo.https.jsonrpc.RpcId;
import org.jdoo.tenants.Tenant;
import org.jdoo.tenants.TenantService;
import org.jdoo.util.KvMap;
import org.jdoo.utils.HttpUtils;
import org.jdoo.utils.ThrowableUtils;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * API控制器
 * 
 * @author lrz
 */
@org.springframework.stereotype.Controller
public class ApiController {

    @RequestMapping(value = "/{tenant}/api", method = RequestMethod.GET)
    public void handler(@PathVariable("tenant") String tenant, HttpServletRequest request,
            HttpServletResponse response) {
        StringBuilder sb = new StringBuilder(1000);
        try {
            Registry registry = getRegistry(tenant);
            addHtml(sb, tenant, registry);
        } catch (Exception e) {
            sb.append(ThrowableUtils.getDebug(e));
        }
        HttpUtils.WriteHtml(response, sb.toString());
    }

    private Registry getRegistry(String key) {
        Tenant tenant = TenantService.get(key);
        return tenant.getRegistry();
    }

    void addModel(StringBuilder sb, String tenant, MetaModel model) {
        appendLine(sb, "<div class='opblock-model-section'>");
        appendLine(sb, "    <h3 class='opblock-model is-open'>");
        appendLine(sb, "        <a><span>");
        appendLine(sb, model.getName());
        appendLine(sb, "        </span></a>");
        appendLine(sb, "        <a><span>");
        appendLine(sb, model.getDescription());
        appendLine(sb, "        </span></a>");
        appendLine(sb, "        <small></small>");
        appendLine(sb, "        <button class='expand-operation'>");
        appendLine(sb, "            <span class='arrow'>\u25e5</span>");
        appendLine(sb, "        </button>");
        appendLine(sb, "    </h3>");
        appendLine(sb, "    <div class='operation-tag-content'>");
        addFields(sb, model);
        model.getService().values().stream().sorted((x, y) -> x.getName().compareTo(y.getName()))
                .forEach(svc -> addService(sb, tenant, svc, model));
        appendLine(sb, "    </div>");
        appendLine(sb, "</div>");
    }

    void addFields(StringBuilder sb, MetaModel model) {
        appendLine(sb, "<div class='opblock-fields'>");
        appendLine(sb, "    <div class='opblock-summary'>");
        appendLine(sb, "        <button class='opblock-summary-control'>");
        appendLine(sb, "            <div class='opblock-summary-description'><span>字段</span></div>");
        // ◢
        appendLine(sb, "            <span class='arrow'>\u25e2</span>");
        appendLine(sb, "        </button>");
        appendLine(sb, "    </div>");
        appendLine(sb, "    <div style='display:none;padding:5px'>");
        appendLine(sb, "        <table class='opblock-fields-body' cellspacing='0' cellpadding='0'>");
        appendLine(sb, "            <thead>");
        appendLine(sb, "                <tr>");
        appendLine(sb, "                    <th>名称</th>");
        appendLine(sb, "                    <th>标题</th>");
        appendLine(sb, "                    <th>类型</th>");
        appendLine(sb, "                    <th>必须</th>");
        appendLine(sb, "                    <th>关联</th>");
        appendLine(sb, "                    <th>说明</th>");
        appendLine(sb, "                </tr>");
        appendLine(sb, "            </thead>");
        appendLine(sb, "            <tbody>");
        appendLine(sb, "                <tr>");
        appendLine(sb, "                    <td>id</td>");
        appendLine(sb, "                    <td>主键</td>");
        appendLine(sb, "                    <td>char</td>");
        appendLine(sb, "                    <td>是</td>");
        appendLine(sb, "                    <td></td>");
        appendLine(sb, "                    <td>唯一键,13位字符</td>");
        appendLine(sb, "                </tr>");
        model.getFields().values().stream().filter(f -> !f.isAuto())
                .sorted((x, y) -> x.getName().compareTo(y.getName())).forEach(f -> addField(sb, f));
        if (model.isLogAccess()) {
            appendLine(sb, "            <tr>");
            appendLine(sb, "                <td>create_uid</td>");
            appendLine(sb, "                <td>创建人</td>");
            appendLine(sb, "                <td>many2one</td>");
            appendLine(sb, "                <td>否</td>");
            appendLine(sb, "                <td>rbac.user</td>");
            appendLine(sb, "                <td></td>");
            appendLine(sb, "            </tr>");
            appendLine(sb, "            <tr>");
            appendLine(sb, "                <td>create_date</td>");
            appendLine(sb, "                <td>创建时间</td>");
            appendLine(sb, "                <td>datetime</td>");
            appendLine(sb, "                <td>否</td>");
            appendLine(sb, "                <td></td>");
            appendLine(sb, "                <td></td>");
            appendLine(sb, "            </tr>");
            appendLine(sb, "            <tr>");
            appendLine(sb, "                <td>update_uid</td>");
            appendLine(sb, "                <td>修改人</td>");
            appendLine(sb, "                <td>many2one</td>");
            appendLine(sb, "                <td>否</td>");
            appendLine(sb, "                <td>rbac.user</td>");
            appendLine(sb, "                <td></td>");
            appendLine(sb, "            </tr>");
            appendLine(sb, "            <tr>");
            appendLine(sb, "                <td>update_date</td>");
            appendLine(sb, "                <td>修改时间</td>");
            appendLine(sb, "                <td>datetime</td>");
            appendLine(sb, "                <td>否</td>");
            appendLine(sb, "                <td></td>");
            appendLine(sb, "                <td></td>");
            appendLine(sb, "            </tr>");
        }
        appendLine(sb, "            </tbody>");
        appendLine(sb, "        </table>");
        appendLine(sb, "    </div>");
        appendLine(sb, "</div>");
    }

    void addField(StringBuilder sb, MetaField f) {
        appendLine(sb, "            <tr>");
        appendLine(sb, "                <td>");
        appendLine(sb, f.getName());
        appendLine(sb, "                </td>");
        appendLine(sb, "                <td>");
        appendLine(sb, f.getLabel());
        appendLine(sb, "                </td>");
        appendLine(sb, "                <td>");
        appendLine(sb, f.getType());
        appendLine(sb, "                </td>");
        appendLine(sb, "                <td>");
        appendLine(sb, f.isRequired() ? "是" : "否");
        appendLine(sb, "                </td>");
        appendLine(sb, "                <td>");
        if (f instanceof RelationalField) {
            appendLine(sb, ((RelationalField<?>) f).getComodel());
        }
        appendLine(sb, "                </td>");
        appendLine(sb, "                <td>");
        appendLine(sb, f.getHelp());
        appendLine(sb, "                </td>");
        appendLine(sb, "            </tr>");
    }

    void addService(StringBuilder sb, String tenant, BaseService svc, MetaModel model) {
        Map<String, ApiDoc> args = svc.getArgsDoc(model);
        ApiDoc result = svc.getResultDoc(model);
        String requestExample = getRequestExample(tenant, svc.getName(), model.getName(), args);
        appendLine(sb, "<div class='opblock'>");
        appendLine(sb, "    <div class='opblock-summary'>");
        appendLine(sb, "        <button class='opblock-summary-control'>");
        appendLine(sb, "            <span class='opblock-summary-method'>");
        appendLine(sb, svc.getName());
        appendLine(sb, "            </span>");
        appendLine(sb, "            <div class='opblock-summary-description'><span>");
        appendLine(sb, svc.getDescription());
        appendLine(sb, "            </span></div>");
        appendLine(sb, "            <span class='arrow'>\u25e2</span>");
        appendLine(sb, "        </button>");
        appendLine(sb, "    </div>");
        appendLine(sb, "    <div class='opblock-body' style='display:none'>");
        appendLine(sb, "        <div class='request-wrapper'>");
        appendLine(sb, "            <div class='opblock-section-header'>");
        appendLine(sb, "                <h4>Args</h4>");
        appendLine(sb, "            </div>");
        addArgs(sb, args);
        appendLine(sb, "            <div class='opblock-section-header'>");
        appendLine(sb, "                <h4>Result</h4>");
        appendLine(sb, "            </div>");
        addResult(sb, result);
        appendLine(sb, "            <div class='opblock-section-header'>");
        appendLine(sb, "                <h4>Request</h4>");
        appendLine(sb, "                <div class='try-out'>");
        appendLine(sb, "                    <button id='" + tenant + "-" + model.getName().replace(".", "_") + "-"
                + svc.getName() + "' class='try-btn try-out-btn'>试一试</button>");
        appendLine(sb, "                </div>");
        appendLine(sb, "            </div>");
        appendLine(sb, "            <div class='try-code' style='display:none'>");
        sb.append("<textarea class='req-box' rows='18' spellcheck='false' style='resize: none; width: 100%;'>");
        sb.append(requestExample);
        appendLine(sb, "</textarea>");
        appendLine(sb, "                <div class='execute-wrapper'>");
        appendLine(sb, "                    <button class='try-btn execute'>发送</button>");
        appendLine(sb, "                </div>");
        appendLine(sb, "            </div>");
        appendLine(sb, "            <div class='highlight-code'>");
        sb.append("<pre class='microlight'>");
        addFormatJson(sb, requestExample);
        appendLine(sb, "</pre>");
        appendLine(sb, "            </div>");
        appendLine(sb, "        </div>");
        appendLine(sb, "        <div class='responses-wrapper'>");
        appendLine(sb, "            <div class='opblock-section-header'>");
        appendLine(sb, "                <h4>Response</h4>");
        appendLine(sb, "            </div>");
        appendLine(sb, "            <div class='try-code' style='display:none'>");
        appendLine(sb,
                "<textarea class='res-box' rows='18' spellcheck='false' style='resize: none; width: 100%;'></textarea>");
        appendLine(sb, "            </div>");
        appendLine(sb, "            <div class='highlight-code'>");
        sb.append("<pre class='microlight'>");
        addResponseExample(sb, result);
        sb.append("</pre>");
        appendLine(sb, "            </div>");
        appendLine(sb, "        </div>");
        appendLine(sb, "    </div>");
        appendLine(sb, "</div>");
    }

    Pattern p = Pattern.compile("\"(.*?)\"");

    void addResponseExample(StringBuilder sb, ApiDoc result) {
        KvMap map = new KvMap();
        map.put("data", result.getExample());
        map.put("context", Collections.emptyMap());
        JsonRpcResponse response = new JsonRpcResponse(new RpcId("guid"), map);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            String[] lines = json.split("\r\n");
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String key = parts[0];
                    sb.append(key);
                    sb.append(":");
                    String value = parts[1];
                    boolean boolval = value != null && (value.trim().equals("true") || value.trim().equals("false"));
                    if (boolval) {
                        value = "<span class='boolval'>" + value + "</span>";
                    } else {
                        Matcher m = p.matcher(value);
                        while (m.find()) {
                            value = value.replace(m.group(), "<span class='strval'>" + m.group() + "</span>");
                        }
                    }
                    sb.append(value);
                    sb.append("\r\n");
                } else {
                    appendLine(sb, line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addFormatJson(StringBuilder sb, String json) {
        try {
            String[] lines = json.split("\r\n");
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String key = parts[0];
                    sb.append(key);
                    sb.append(":");
                    String value = parts[1];
                    boolean boolval = value != null && (value.trim().equals("true") || value.trim().equals("false"));
                    if (boolval) {
                        value = "<span class='boolval'>" + value + "</span>";
                    } else {
                        Matcher m = p.matcher(value);
                        while (m.find()) {
                            value = value.replace(m.group(), "<span class='strval'>" + m.group() + "</span>");
                        }
                    }
                    sb.append(value);
                    sb.append("\r\n");
                } else {
                    appendLine(sb, line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addResult(StringBuilder sb, ApiDoc result) {
        appendLine(sb, "<div style='padding:5px'>");
        appendLine(sb, "<table class='opblock-fields-body' cellspacing='0' cellpadding='0'>");
        appendLine(sb, "    <thead>");
        appendLine(sb, "        <tr>");
        appendLine(sb, "            <th>类型</th>");
        appendLine(sb, "            <th>说明</th>");
        appendLine(sb, "        </tr>");
        appendLine(sb, "    </thead>");
        appendLine(sb, "    <tbody>");
        appendLine(sb, "        <tr>");
        sb.append("            <td>");
        sb.append(result.getType());
        appendLine(sb, "</td>");
        sb.append("            <td>");
        sb.append(result.getDescription());
        appendLine(sb, "</td>");
        appendLine(sb, "        </tr>");
        appendLine(sb, "    </tbody>");
        appendLine(sb, "</table>");
        appendLine(sb, "</div>");
    }

    void addArgs(StringBuilder sb, Map<String, ApiDoc> args) {
        appendLine(sb, "<div style='padding:5px'>");
        appendLine(sb, "<table class='opblock-fields-body' cellspacing='0' cellpadding='0'>");
        appendLine(sb, "    <thead>");
        appendLine(sb, "        <tr>");
        appendLine(sb, "            <th>名称</th>");
        appendLine(sb, "            <th>类型</th>");
        appendLine(sb, "            <th>说明</th>");
        appendLine(sb, "        </tr>");
        appendLine(sb, "    </thead>");
        appendLine(sb, "    <tbody>");

        for (Entry<String, ApiDoc> e : args.entrySet()) {
            String arg = e.getKey();
            if ("@args".equals(arg)) {
                arg = "";
            }
            ApiDoc api = e.getValue();

            appendLine(sb, "        <tr>");
            sb.append("            <td>");
            sb.append(arg);
            appendLine(sb, "</td>");
            sb.append("            <td>");
            sb.append(api.getType());
            appendLine(sb, "</td>");
            sb.append("            <td>");
            sb.append(api.getDescription());
            appendLine(sb, "</td>");
            appendLine(sb, "        </tr>");
        }

        appendLine(sb, "    </tbody>");
        appendLine(sb, "</table>");
        appendLine(sb, "</div>");
    }

    String getRequestExample(String tenant, String method, String model, Map<String, ApiDoc> args) {
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcRequest request = new JsonRpcRequest();
        request.setId(new RpcId("guid"));
        request.setMethod(method);
        KvMap map = new KvMap(4);
        map.put("args",
                args.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getExample())));
        map.put("model", model);
        map.put("context", new KvMap().set("uid", "").set("lang", "zh_CN").set("tenant", tenant));
        request.setParams(new JsonRpcParameter(map));
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }

    void appendLine(StringBuilder sb, String str, Object... formatArgs) {
        if (str != null) {
            if (formatArgs.length > 0) {
                sb.append(String.format(str, formatArgs));
            } else {
                sb.append(str);
            }
        }
        sb.append("\r\n");
    }

    void addHtml(StringBuilder sb, String tenant, Registry registry) {
        appendLine(sb, "<html>");
        appendLine(sb, "    <head>");
        appendLine(sb, "        <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
        appendLine(sb, "        <title>API Document</title>");
        appendLine(sb, "        <script src='/jdoo/addons/api/statics/js/jquery-1.4.4.min.js'></script>");
        appendLine(sb, "        <script src='/jdoo/addons/api/statics/js/api-doc.js'></script>");
        appendLine(sb,
                "        <link rel='stylesheet' type='text/css' href='/jdoo/addons/api/statics/style/api-doc.css'>");
        appendLine(sb, "    </head>");
        appendLine(sb, "    <body>");
        appendLine(sb, "        <script>var tenant='%s'</script>", tenant);
        appendLine(sb, "        <div class='topbar'>");
        appendLine(sb, "            <div class='wrapper'>");
        appendLine(sb, "                <a>API 文档</a>");
        appendLine(sb, "            </div>");
        appendLine(sb, "        </div>");
        appendLine(sb, "        <div class='wrapper'>");
        appendLine(sb,
                "        <div class='wrapper' style='padding:20px'>API遵循jsonrpc2.0规范, POST地址 {tenant}/rpc/service</div>");
        appendLine(sb, "        <div class='opblock-fields'>");
        appendLine(sb, "            <table style='padding:5px'>");
        appendLine(sb, "                <tr>");
        appendLine(sb, "                    <td>账号<td>");
        appendLine(sb, "                    <td><input type='text' name='login_account' /><td>");
        appendLine(sb, "                    <td>密码<td>");
        appendLine(sb, "                    <td><input type='password' name='login_password' /><td>");
        appendLine(sb, "                    <td><button type='button' name='login_submit'>登录</button><td>");
        appendLine(sb, "                    <td><span name='login_token'></span><td>");
        appendLine(sb, "                </tr>");
        appendLine(sb, "            </table>");
        appendLine(sb, "        </div>");
        appendLine(sb, "        <div style='display: flex;flex-direction: column;align-items: flex-end;'><button name='collapse_all' class='try-btn'>折叠所有</button></div>");

        registry.getModels().values().stream().filter(m -> !m.isAbstract())
                .sorted((x, y) -> x.getName().compareTo(y.getName())).forEach(model -> addModel(sb, tenant, model));

        appendLine(sb, "        </div>");
        appendLine(sb, "    </head>");
        appendLine(sb, "</html>");
    }
}
