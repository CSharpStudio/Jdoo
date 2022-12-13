package com.addons.demo.models.fields;

import org.jdoo.*;

/**
 * 示例
 * 
 * @author lrz
 */
@Model.Meta(name = "demo.one2many")
public class One2Many extends Model {
    static Field name = Field.Char().label("名称").index(true).required(true);
    static Field book = Field.Many2one("demo.book").label("图书");
    static Field field_id = Field.Many2one("demo.field_type").label("字段");
}
