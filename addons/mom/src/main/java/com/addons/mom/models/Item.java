package com.addons.mom.models;

import org.jdoo.*;

/**
 * 物料
 * 
 * @author lrz
 */
@Model.Meta(name = "mom.item", description = "物料", present = { "name", "code" }, presentFormat = "({code}) {name}")
public class Item extends Model {
    static Field name = Field.Char().label("名称").index(true).required(true);
    static Field code = Field.Char().label("编号").required(true);
}
