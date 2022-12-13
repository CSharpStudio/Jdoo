package org.jdoo.base.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jdoo.*;
import org.jdoo.core.Constants;
import org.jdoo.core.Environment;
import org.jdoo.core.MetaField;
import org.jdoo.core.MetaModel;
import org.jdoo.exceptions.UserException;
import org.jdoo.fields.SelectionField;
import org.jdoo.util.KvMap;
import org.jdoo.utils.ArrayUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * UI视图
 * 
 * @author lrz
 */
@Model.Meta(name = "ir.ui.view", label = "视图", order = "priority, name, id")
public class IrUiView extends Model {
    static Field name = Field.Char().label("视图名称").required();
    static Field model = Field.Char().label("模型").help("没指定模型的视图直接通过识别码加载").index();
    static Field key = Field.Char().label("识别码").help("用于区分视图组");
    static Field priority = Field.Integer().label("顺序").defaultValue(16).required();
    static Field type = Field.Selection().label("视图类型").selection(new HashMap<String, String>() {
        {
            put("grid", "表格");
            put("form", "表单");
            put("card", "卡片");
            put("search", "查询");
            put("custom", "自定义");
            put("resource", "资源");
            put("web", "页面");
        }
    });
    static Field arch = Field.Text().label("视图结构");
    static Field inherit_id = Field.Many2one("ir.ui.view").label("继承视图");
    static Field mode = Field.Selection().label("视图继承模式").selection(new HashMap<String, String>() {
        {
            put("primary", "基础视图");
            put("extension", "扩展视图");
        }
    }).defaultValue("primary").required();
    static Field active = Field.Boolean().label("是否有效").defaultValue(true);

    public String loadWeb(Records rec, String key) {
        Records views = rec.find(Criteria.equal("key", key), 0, 0, "");
        Records primary = null;
        List<Records> extension = new ArrayList<>();
        for (Records view : views) {
            if (primary == null && "primary".equals(view.get("mode"))) {
                primary = view;
            }
            if ("extension".equals(view.get("mode"))) {
                extension.add(view);
            }
        }
        if (primary == null) {
            throw new UserException(rec.l10n("找不到视图:%s", key));
        }
        Document doc = Jsoup.parse((String) primary.get("arch"), Parser.xmlParser());
        Elements base = doc.children();
        for (Records ext : extension) {
            Document arch = Jsoup.parse((String) ext.get("arch"), Parser.xmlParser());
            Elements data = getData(arch);
            combined(base, data);
        }
        if (rec.getEnv().isDebug()) {
            doc.select("[debug=false]").remove();
        } else {
            doc.select("[debug=true]").remove();
        }
        // dom4j无法解析<!DOCTYPE>
        return "<!DOCTYPE html>\r\n" + doc.toString();
    }

    @Model.ServiceMethod(records = false, doc = "加载属性", auth = Constants.ANONYMOUS)
    public Object loadFields(Records rec, @Doc(doc = "模型名称") String model) {
        KvMap result = new KvMap();
        result.put("fields", getFields(rec.getEnv(), model));
        return result;
    }

    @SuppressWarnings("unchecked")
    Map<String, Map<String, Object>> getFields(Environment env, String model) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        Records rec = env.get(model);
        ObjectMapper m = new ObjectMapper();
        Set<String> deny = (Set<String>) env.get("rbac.permission").call("loadModelDenyFields", model);
        for (Entry<String, MetaField> e : rec.getMeta().getFields().entrySet()) {
            try {
                MetaField field = e.getValue();
                Map<String, Object> data = (Map<String, Object>) m.treeToValue(m.valueToTree(field), Map.class);
                data.put("defaultValue", field.getDefault(rec));
                if (field instanceof SelectionField) {
                    data.put("options", ((SelectionField) field).getOptions(rec));
                }
                if (deny.contains(field.getName())) {
                    data.put("deny", true);
                }
                result.put(field.getName(), data);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    @Model.ServiceMethod(records = false, doc = "加载视图", auth = Constants.ANONYMOUS)
    @Doc(doc = "视图配置", value = "{\"model\":\"\",\"fields\":[],\"views\":[]}")
    public Object loadView(Records rec, @Doc(doc = "模型名称") String model, @Doc(doc = "视图类型") String type,
            @Doc(doc = "视图组") String key) {
        List<String> types = ArrayUtils.asList(type.split(","));
        types.add("resource");
        if (types.contains("grid") || types.contains("card")) {
            types.add("search");
        }
        Records datas = rec.find(Criteria.equal("model", model).and("type", "in", types).and("key", "=", key));
        MetaModel meta = rec.getEnv().getRegistry().get(model);
        KvMap result = new KvMap();
        result.put("model", model);
        result.put("module", meta.getModule());
        result.put("present", meta.getPresent());
        Object auths = rec.getEnv().isAdmin() ? "@all"
                : rec.getEnv().get("rbac.permission").call("loadModelAuths", model);
        result.put("auths", auths);
        result.put("fields", getFields(rec.getEnv(), model));
        KvMap views = new KvMap();
        result.put("views", views);
        for (String t : types) {
            if ("resource".equals(t)) {
                Optional<Records> primary = datas.stream()
                        .filter(v -> t.equals(v.get("type")) && "primary".equals(v.get("mode"))).findFirst();
                if (!primary.isPresent()) {
                    continue;
                }

                List<Records> extension = datas.stream()
                        .filter(v -> t.equals(v.get("type")) && "extension".equals(v.get("mode")))
                        .collect(Collectors.toList());

                Document doc = Jsoup.parse((String) primary.get().get("arch"), Parser.xmlParser());
                Elements base = doc.children();
                for (Records ext : extension) {
                    Document arch = Jsoup.parse((String) ext.get("arch"), Parser.xmlParser());
                    Elements data = getData(arch);
                    combined(base, data);
                }
                if (rec.getEnv().isDebug()) {
                    base.select("[debug=false]").remove();
                } else {
                    base.select("[debug=true]").remove();
                }
                if (base.size() == 1 && "resource".equals(base.get(0).tagName())) {
                    result.put(t, base.html());
                } else {
                    result.put(t, base.toString());
                }
            } else {
                KvMap view = new KvMap();
                Optional<Records> primary = datas.stream()
                        .filter(v -> t.equals(v.get("type")) && "primary".equals(v.get("mode"))).findFirst();
                if (!primary.isPresent()) {
                    if ("search".equals(t)) {
                        view.put("arch", "");
                        view.put("view_id", "");
                    } else {
                        throw new UserException(rec.l10n("模型[%s]的视图[%s]不存在", model, t));
                    }
                } else {
                    List<Records> extension = datas.stream()
                            .filter(v -> t.equals(v.get("type")) && "extension".equals(v.get("mode")))
                            .collect(Collectors.toList());

                    Document doc = Jsoup.parse((String) primary.get().get("arch"), Parser.xmlParser());
                    Elements base = doc.children();
                    for (Records ext : extension) {
                        Document arch = Jsoup.parse((String) ext.get("arch"), Parser.xmlParser());
                        Elements data = getData(arch);
                        combined(base, data);
                    }
                    if (rec.getEnv().isDebug()) {
                        base.select("[debug=false]").remove();
                    } else {
                        base.select("[debug=true]").remove();
                    }
                    view.put("arch", base.toString());
                    view.put("view_id", primary.get().getId());
                }

                views.put(t, view);
            }
        }
        return result;
    }

    public static void combined(Elements base, Elements data) {
        for (Element el : data) {
            String position = el.attr("position");
            if ("xpath".equals(el.tagName())) {
                String has = el.attr("has");
                if (StringUtils.isNotEmpty(has)) {
                    Elements found = base.select(has);
                    if (found.size() == 0) {
                        continue;
                    }
                }
                String hasNot = el.attr("has_not");
                if (StringUtils.isNotEmpty(hasNot)) {
                    Elements found = base.select(hasNot);
                    if (found.size() > 0) {
                        continue;
                    }
                }
                String expr = el.attr("expr");
                Elements selects = base.select(expr);
                for (Element select : selects) {
                    combined(position, select, el.childNodes().toArray(new Node[el.childNodeSize()]));
                }
            } else {
                for (Element select : base) {
                    combined(position, select, el);
                }
            }
        }
    }

    static void combined(String position, Element target, Node... nodes) {
        if ("before".equals(position)) {
            for (Node node : nodes) {
                target.before(node);
            }
        } else if ("after".equals(position)) {
            for (Node node : nodes) {
                target.after(node);
            }
        } else if ("replace".equals(position)) {
            boolean first = true;
            for (Node node : nodes) {
                if (first) {
                    first = false;
                    target.replaceWith(node);
                } else {
                    target.after(node);
                }
            }
        } else if ("inside".equals(position)) {
            target.appendChildren(Arrays.asList(nodes));
        } else if ("attribute".equals(position)) {
            for (Node node : nodes) {
                target.attributes().addAll(node.attributes());
            }
        }
    }

    Elements getData(Document doc) {
        for (Element el : doc.children()) {
            if ("data".equals(el.tagName())) {
                return el.children();
            }
        }
        return doc.children();
    }
}
