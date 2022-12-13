package com.addons.demo.models.delegate;

import java.util.HashMap;

import org.jdoo.*;

@Model.Meta(name = "demo.supplier", label = "客户")
public class Supplier extends Model {
    static Field partner_id = Field.Delegate("demo.partner");

    static Field responsible_id = Field.Many2one("rbac.user").label("负责人");
    static Field send_by = Field.Selection(new HashMap<String, String>() {
        {
            put("phone", "Phone");
            put("mail", "Email");
        }
    }).label("Send Order By").defaultValue("phone");
}