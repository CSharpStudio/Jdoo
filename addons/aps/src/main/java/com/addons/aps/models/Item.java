package com.addons.aps.models;

import org.jdoo.*;

@Model.Meta(inherit = "mom.item")
public class Item extends Model {
    static Field buyer_id = Field.Many2one("rbac.user").label("采购员");
    static Field purchase_lead_time = Field.Integer().label("采购提前天数");
}
