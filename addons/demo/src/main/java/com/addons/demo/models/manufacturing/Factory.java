package com.addons.demo.models.manufacturing;

import org.jdoo.*;

/**
 * 示例
 * 
 * @author lrz
 */
@Model.Meta(name = "demo.factory", label = "工厂")
public class Factory extends Model {
    static Field name = Field.Char().label("名称").index(true).required(true);
    static Field workshop_ids = Field.One2many("demo.workshop", "factory_id").label("车间");
}