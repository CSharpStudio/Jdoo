package org.jdoo.base.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdoo.*;

@Model.UniqueConstraint(name = "unique_name", fields = "name")
@Model.UniqueConstraint(name = "unique_code", fields = "code")
@Model.UniqueConstraint(name = "unique_iso_code", fields = "iso_code")
@Model.Meta(name = "res.lang", label = "语系", order = "active desc,name")
public class ResLanguage extends Model {
    static Field name = Field.Char().label("名称").required();
    static Field code = Field.Char().label("地区代码").help("用于用户设置本地化").required();
    static Field iso_code = Field.Char().label("ISO代码").help("ISO代码用于翻译").required();
    static Field active = Field.Boolean().label("启用");
    static Field date_format = Field.Char().label("日期格式").required().defaultValue("yyyy/MM/dd");
    static Field time_format = Field.Char().label("时间格式").required().defaultValue("HH:mm:ss");
    static Field week_start = Field.Selection(new HashMap<String, String>() {
        {
            put("1", "星期一");
            put("2", "星期二");
            put("3", "星期三");
            put("4", "星期四");
            put("5", "星期五");
            put("6", "星期六");
            put("7", "星期天");
        }
    }).label("一周的第一天").required().defaultValue("7");

    public Map<String, String> getInstalled(Records rec) {
        return rec.find(Criteria.equal("active", true)).read(Arrays.asList("code", "name")).stream()
                .collect(Collectors.toMap(k -> (String) k.get("code"), v -> (String) v.get("name")));
    }
}
