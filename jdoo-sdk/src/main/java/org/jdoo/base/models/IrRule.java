package org.jdoo.base.models;

import org.jdoo.*;

@Model.Meta(name = "ir.rule", label = "规则")
public class IrRule extends Model {
    static Field name = Field.Char().label("名称").required();
    static Field model_id = Field.Many2one("ir.model").label("模型").index().required().ondelete(DeleteMode.Cascade);
    static Field all_roles = Field.Boolean().label("所有角色");
    static Field role_ids = Field.Many2many("rbac.role", "rbac_role_rule", "rule", "role");
    static Field criteria = Field.Text().label("过滤条件");

    public void initCompanyRule(Records rec) {

    }
}
