package com.addons.demo.models.delegate;

import java.util.HashMap;

import org.jdoo.*;

@Model.Meta(name = "demo.customer", label = "客户")
public class Customer extends Model {
    static Field partner_id = Field.Delegate("demo.partner");
    static Field priority = Field.Selection(Selection.value(new HashMap<String, String>(16) {
        {
            put("1", "不推荐");
            put("2", "一般");
            put("3", "不错");
            put("4", "很棒");
            put("5", "极力推荐");
        }
    })).label("评分").help("评分字段");
}
