package com.addons.demo.models.fields;

import org.jdoo.*;

/**
 * 示例
 * 
 * @author lrz
 */
@Model.Meta(name = "demo.many2one")
public class Many2One extends Model {
    static Field name = Field.Char().label("名称").index(true).required(true);
}
