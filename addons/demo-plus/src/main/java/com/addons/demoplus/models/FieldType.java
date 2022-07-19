package com.addons.demoplus.models;

import java.util.HashMap;

import org.jdoo.*;

/**
 * 租户
 * 
 * @author lrz
 */
@Model.Meta(inherit = "demo.field_type")
public class FieldType extends Model {
    static Field f_char_ext = Field.Char().label("文本扩展").help("扩展的文本字段");
    static Field f_selection = Field.Selection().addSelection(new HashMap<String, String>(16) {
        {
            put("6", "星期六");
            put("7", "星期天");
        }
    });
    static Field f_char = Field.Char();
}