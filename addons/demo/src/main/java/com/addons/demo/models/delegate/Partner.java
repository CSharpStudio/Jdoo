package com.addons.demo.models.delegate;

import org.jdoo.*;

@Model.Meta(name = "demo.partner", label = "伙伴")
public class Partner extends Model {
    static Field name = Field.Char().label("名称").index(true).required(true);
    static Field address = Field.Char().label("地址");
    static Field zip = Field.Char().label("邮编");
    static Field contact_user_id = Field.Many2one("rbac.user").label("联系人");
}
