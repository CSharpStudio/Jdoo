package com.addons.wms.models;

import org.jdoo.*;

/**
 * 仓库
 * 
 * @author lrz
 */
@Model.Meta(name = "wms.storage", present = { "name", "code" }, presentFormat = "({code}) {name}")
public class Storage extends Model {
    static Field name = Field.Char().label("名称");
    static Field code = Field.Char().label("编码");
}
