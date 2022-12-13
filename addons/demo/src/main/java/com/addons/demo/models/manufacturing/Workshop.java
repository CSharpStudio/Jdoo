package com.addons.demo.models.manufacturing;

import org.jdoo.*;

/**
 * 示例
 * 
 * @author lrz
 */
@Model.Meta(name = "demo.workshop", label = "车间")
public class Workshop extends Model {
    static Field name = Field.Char().label("名称").index(true).required(true);
    static Field factory_id = Field.Many2one("demo.factory").label("工厂");
}