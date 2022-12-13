package com.addons.modeling.models;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdoo.*;
import org.jdoo.util.StringBuilderPlus;
import org.jdoo.utils.StringUtils;

@Model.Meta(name = "modeling.model.field", label = "字段", authModel = "modeling.model", order = "id")
public class DesignModelField extends Model {
    static Map<String, String> FIELD_TYPES = Field.getFieldTypes().stream().collect(Collectors.toMap(k -> k, v -> v));
    static Map<String, String> BOOLEAN = new HashMap<String, String>() {
        {
            put("", "继承");
            put("false", "否");
            put("true", "是");
        }
    };

    static Field name = Field.Char().label("名称").help("字段名").index(true).required(true);
    static Field model_id = Field.Many2one("modeling.model").label("模型").required().index().help("字段所属的模型")
            .ondelete(DeleteMode.Cascade);
    static Field field_type = Field.Selection(FIELD_TYPES).label("类型").help("字段类型").required(true)
            .defaultValue("char");
    static Field relation = Field.Char().label("关联的模型");
    static Field relation_field = Field.Char().label("一对多关联字段");
    static Field label = Field.Char().label("标题");
    static Field help = Field.Char().label("帮助");
    static Field related = Field.Char().label("关联");
    static Field required = Field.Selection(BOOLEAN).label("是否必填").defaultValue("");
    static Field readonly = Field.Selection(BOOLEAN).label("是否只读").defaultValue("");
    static Field index = Field.Selection(BOOLEAN).label("是否索引").defaultValue("");
    static Field translate = Field.Selection(BOOLEAN).label("是否翻译").defaultValue("");
    static Field length = Field.Integer().label("最大长度");
    static Field on_delete = Field.Selection(new HashMap<String, String>() {
        {
            put("", "继承");
            put("Cascade", "级联");
            put("SetNull", "设置为null");
            put("Restrict", "限制");
        }
    }).label("删除操作").defaultValue("");
    static Field auth = Field.Selection(BOOLEAN).label("是否需要权限访问").defaultValue("");
    static Field relation_table = Field.Char().label("多对多关联表");
    static Field column1 = Field.Char().label("左联字段");
    static Field column2 = Field.Char().label("右联字段");
    static Field compute = Field.Char().label("计算表达式");
    static Field default_value = Field.Char().label("默认值");
    static Field depends = Field.Char().label("依赖");
    static Field store = Field.Selection(BOOLEAN).label("是否存储").defaultValue("");
    static Field copy = Field.Selection(BOOLEAN).label("是否复制").defaultValue("");

    public static String getName(Records rec) {
        return (String) rec.get(name);
    }

    public static void setName(Records rec, String value) {
        rec.set(name, value);
    }

    public Map<String, String> getCode(Records rec) {
        rec.ensureOne();
        Map<String, String> result = new HashMap<>();
        result.put("field", getFieldCode(rec));
        result.put("method", getMethodCode(rec));
        return result;
    }

    String formatName(String name) {
        String result = "";
        for (String n : name.split("_")) {
            result += Character.toUpperCase(n.charAt(0));
            if (n.length() > 1) {
                result += n.substring(1);
            }
        }
        return result;
    }

    String getMethodCode(Records rec) {
        StringBuilderPlus sb = new StringBuilderPlus();
        String type = (String) rec.get("field_type");
        String javaClass = Field.getJavaClass(type).getSimpleName();
        String name = formatName(getName(rec));
        sb.appendLine("    public static %s %s%s(Records rec){", javaClass,
                "Boolean".equals(javaClass) ? "is" : "get", name);
        sb.appendLine("        return (%s)rec.get(%s);", javaClass, getName(rec));
        sb.appendLine("    }");
        sb.appendLine();
        sb.appendLine("    public static void %s%s(Records rec, %s value){", "set", name, javaClass);
        sb.appendLine("        rec.set(%s, value);", getName(rec));
        sb.appendLine("    }");
        return sb.toString();
    }

    String getFieldCode(Records rec) {
        StringBuilderPlus sb = new StringBuilderPlus();
        String type = (String) rec.get("field_type");
        sb.append("    static Field %s = Field.%s(", getName(rec), Field.getFieldName(type).replace("Field", ""));
        if ("many2one".equals(type)) {
            sb.append("\"%s\")", rec.get("relation"));
            String ondelete = (String) rec.get("on_delete");
            if (StringUtils.isNotEmpty(ondelete)) {
                sb.append(".ondelete(DeleteMode.%s)", ondelete);
            }
        } else if ("one2many".equals(type)) {
            sb.append("\"%s\", \"%s\")", rec.get("relation"), rec.get("relation_field"));
        } else if ("many2many".equals(type)) {
            sb.append("\"%s\", \"%s\", \"%s\", \"%s\")", rec.get("relation"), rec.get("relation_table"),
                    rec.get("column1"), rec.get("column2"));
            String ondelete = (String) rec.get("on_delete");
            if (StringUtils.isNotEmpty(ondelete)) {
                sb.append(".ondelete(DeleteMode.%s)", ondelete);
            }
        } else {
            sb.append(")");
        }
        String label = (String) rec.get("label");
        if (StringUtils.isNotEmpty(label)) {
            sb.append(".label(\"%s\")", label);
        }
        String help = (String) rec.get("help");
        if (StringUtils.isNotEmpty(help)) {
            sb.append(".help(\"%s\")", help);
        }
        String related = (String) rec.get("related");
        if (StringUtils.isNotEmpty(related)) {
            sb.append(".related(\"%s\")", related);
        }
        String required = (String) rec.get("required");
        if (StringUtils.isNotEmpty(required)) {
            sb.append(".required(%s)", required);
        }
        String readonly = (String) rec.get("readonly");
        if (StringUtils.isNotEmpty(readonly)) {
            sb.append(".readonly(%s)", readonly);
        }
        String index = (String) rec.get("index");
        if (StringUtils.isNotEmpty(index)) {
            sb.append(".index(%s)", index);
        }
        String store = (String) rec.get("store");
        if (StringUtils.isNotEmpty(store)) {
            sb.append(".store(%s)", store);
        }
        String copy = (String) rec.get("copy");
        if (StringUtils.isNotEmpty(copy)) {
            sb.append(".copy(%s)", copy);
        }
        String auth = (String) rec.get("auth");
        if (StringUtils.isNotEmpty(auth)) {
            sb.append(".auth(%s)", auth);
        }
        String compute = (String) rec.get("compute");
        if (StringUtils.isNotEmpty(compute)) {
            sb.append(".compute(Callable.script(\"%s\"))", compute);
        }
        String default_value = (String) rec.get("default_value");
        if (StringUtils.isNotEmpty(default_value)) {
            if ("char".equals(type) || "text".equals(type) || "html".equals(type)) {
                default_value = "\"" + default_value + "\"";
            }
            sb.append(".defaultValue(%s)", default_value);
        }
        if ("char".equals(type)) {
            Integer length = (Integer) rec.get("length");
            if (length != null) {
                sb.append(".length(%s)", length);
            }
        }
        sb.append(";");
        return sb.toString();
    }
}
