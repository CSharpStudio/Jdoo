package com.addons.demo.models.fields;

import org.jdoo.*;

/**
 * 示例
 * 
 * @author lrz
 */
@Model.Meta(name = "demo.many2many")
public class Many2Many extends Model {
    static Field name = Field.Char().label("名称").index(true).required(true);
    static Field field_ids = Field.Many2many("demo.field_type", "demo_m2m", "field_ids", "f_m2m_ids").label("字段");
}
