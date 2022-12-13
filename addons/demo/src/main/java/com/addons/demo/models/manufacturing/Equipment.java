package com.addons.demo.models.manufacturing;

import org.jdoo.*;

/**
 * 示例
 * 
 * @author lrz
 */
@Model.UniqueConstraint(fields = "name", name = "unqirue_name")
@Model.Meta(name = "demo.equipment", label = "设备")
public class Equipment extends Model {
    static Field name = Field.Char().label("名称").index(true).required(true);
    static Field factory_id = Field.Many2one("demo.factory").label("工厂");
    static Field workshop_id = Field.Many2one("demo.workshop").label("车间");
}