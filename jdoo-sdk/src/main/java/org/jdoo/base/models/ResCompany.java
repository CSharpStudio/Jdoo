package org.jdoo.base.models;

import org.jdoo.*;

@Model.Meta(name = "res.company", label = "公司")
public class ResCompany extends Model {
    static Field name = Field.Char().label("名称").required();
    static Field parent_id = Field.Many2one("res.company").label("母公司");
    static Field child_ids = Field.One2many("res.company", "parent_id").label("子公司");
    static Field address = Field.Char().label("地址");
}
