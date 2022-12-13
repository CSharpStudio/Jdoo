package com.addons.modeling.models;

import org.jdoo.*;

@Model.Service(remove = "@all")
@Model.Meta(name = "modeling.diagram_shape", label = "模型", authModel = "modeling.diagram")
public class DiagramShape extends Model {
    static Field diagram_id = Field.Many2one("modeling.diagram").label("模型图");
    static Field model_id = Field.Many2one("modeling.model").label("模型").ondelete(DeleteMode.Cascade);
    static Field bgcolor = Field.Char().label("背景色");
    static Field x = Field.Char().label("横坐标");
    static Field y = Field.Char().label("纵坐标");
}
