package com.addons.wms.models;

import org.jdoo.*;

/**
 * RoHS
 */
@Model.Meta(name = "wms.rohs", present = { "name", "code" }, presentFormat = "({code}) {name}")
public class Rohs extends Model {
    static Field name = Field.Char().label("名称");
    static Field code = Field.Char().label("编码");
    static Field description = Field.Char().label("描述");
}
