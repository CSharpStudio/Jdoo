package com.addons.modeling.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdoo.*;
import org.jdoo.util.StringBuilderPlus;
import org.jdoo.utils.StringUtils;

@Model.Meta(name = "modeling.model", label = "设计模型", authModel = "modeling.diagram")
public class DesignModel extends Model {
    static Field name = Field.Char().label("名称").index().required();
    static Field model = Field.Char().label("模型").index().required();
    static Field description = Field.Char().label("描述").help("模型说明");
    static Field inherit = Field.Char().label("继承").help("模型的继承，多个使用逗号','分隔");
    static Field table = Field.Char().label("表名").help("映射数据库的表名");
    static Field order = Field.Char().label("排序").help("默认排序SQL, 如 name ASC, date DESC");
    static Field present_fields = Field.Char().label("呈现").help("模型展示的字段，使用逗号[,]分隔，例如 name,code，默认尝试使用name字段");
    static Field present_format = Field.Char().label("呈现格式").help("模型展示字段显示的格式，使用{}限定，例如 {name} - {code}");
    static Field is_transient = Field.Boolean().label("是否瞬态");
    static Field is_abstract = Field.Boolean().label("是否抽象");
    static Field model_class = Field.Char().label("类").help("对应代码的类").required();
    static Field package_name = Field.Char().label("包").help("对应代码的包");
    static Field field_ids = Field.One2many("modeling.model.field", "model_id").label("字段");
    static Field module_id = Field.Many2one("ir.module").ondelete(DeleteMode.Restrict);

    public List<Map<String, String>> getCode(Records rec) {
        List<Map<String, String>> codes = new ArrayList<>();
        for (Records r : rec) {
            codes.add(new HashMap<String, String>() {
                {
                    put("name", (String) r.get("name"));
                    put("model", (String) r.get("model"));
                    put("class", (String) r.get("model_class"));
                    put("code", getJavaCode(r));
                    put("view", getXmlView(r));
                }
            });
        }
        return codes;
    }

    @SuppressWarnings("unchecked")
    String getJavaCode(Records rec) {
        StringBuilderPlus code = new StringBuilderPlus();
        String pkg = (String) rec.get("package_name");
        if (StringUtils.isEmpty(pkg)) {
            pkg = (String) rec.getRel("module_id").get("package_info") + ".models";
        }
        code.appendLine("package %s;\r\n", pkg);
        code.appendLine("import org.jdoo.*;\r\n");

        code.append("@Model.Meta(name = \"%s\", label=\"%s\"", rec.get("model"), rec.get("name"));
        String description = (String) rec.get("description");
        if (StringUtils.isNotEmpty(description)) {
            code.append("description = \"%s\"", description);
        }
        String inherit = (String) rec.get("inherit");
        if (StringUtils.isNotEmpty(inherit)) {
            inherit = Arrays.stream(inherit.split(",")).map(p -> "\"" + p + "\"").collect(Collectors.joining(","));
            code.append("inherit = { %s }", inherit);
        }
        code.appendLine(")");
        String base = (Boolean) rec.get("is_abstract") ? "AbstractModel"
                : (Boolean) rec.get("is_transient") ? "TransientModel" : "Model";
        code.appendLine("public class %s exends %s{", rec.get("model_class"), base);
        StringBuilderPlus methods = new StringBuilderPlus();
        for (Records f : rec.getRel("field_ids")) {
            Map<String, String> map = (Map<String, String>) f.call("getCode");
            code.appendLine(map.get("field"));
            methods.appendLine(map.get("method"));
        }
        code.appendLine();
        code.append(methods.toString());
        code.appendLine("}");
        return code.toString();
    }

    String getXmlView(Records r) {
        StringBuilderPlus view = new StringBuilderPlus();
        List<String> search = new ArrayList<>();
        List<String> grid = new ArrayList<>();
        List<String> form = new ArrayList<>();
        List<String> tabs = new ArrayList<>();
        for (Records f : r.getRel("field_ids")) {
            String fview = String.format("<field name='%s'/>", f.get("name"));
            String ftype = (String) f.get("field_type");
            if (ftype.contains("2many")) {
                String label = (String) f.get("label");
                if (StringUtils.isEmpty(label)) {
                    label = (String) f.get("name");
                }
                tabs.add(String.format(
                        "<tab label='%s'>\r\n          <field name='%s' colspan='4' nolabel='1'>\r\n            <grid></grid>\r\n          </field>\r\n        </tab>",
                        label, f.get("name")));
            } else {
                search.add(fview);
                grid.add(fview);
                form.add(fview);
            }
        }
        view.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        view.appendLine("<data>");
        String key = ((String) r.get("model")).replaceAll("\\.", "_");
        // search
        view.appendLine("  <view id='%s_search' name='%s-查询' model='%s'>", key, r.get("name"), r.get("model"));
        view.appendLine("    <search>");
        for (String line : search) {
            view.appendLine("      %s", line);
        }
        view.appendLine("    </search>");
        view.appendLine("  </view>");
        // grid
        view.appendLine("  <view id='%s_grid' name='%s-表格' model='%s'>", key, r.get("name"), r.get("model"));
        view.appendLine("    <grid>");
        view.appendLine("      <toolbar buttons='default'></toolbar>");
        for (String line : grid) {
            view.appendLine("      %s", line);
        }
        view.appendLine("    </grid>");
        view.appendLine("  </view>");
        // form
        view.appendLine("  <view id='%s_form' name='%s-表单' model='%s'>", key, r.get("name"), r.get("model"));
        view.appendLine("    <form>");
        view.appendLine("      <toolbar buttons='default'></toolbar>");
        for (String line : form) {
            view.appendLine("      %s", line);
        }
        if (tabs.size() > 0) {
            view.appendLine("      <tabs>");
            for (String line : tabs) {
                view.appendLine("        %s", line);
            }
            view.appendLine("      </tabs>");
        }
        view.appendLine("    </form>");
        view.appendLine("  </view>");
        view.appendLine("</data>");
        return view.toString();
    }
}
