package com.addons.modeling.models;

import org.jdoo.*;

@Model.Meta(inherit = "ir.module")
public class IrModule extends Model {
    static Field diagram_ids = Field.One2many("modeling.diagram", "module_id");
    static Field modeling_ids = Field.One2many("modeling.model", "module_id");
}
