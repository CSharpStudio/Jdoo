package com.addons.wms.models;

import org.jdoo.*;

/**
 * 物料
 * 
 * @author lrz
 */
@Model.Meta(inherit = "mom.item")
public class Item extends Model {
    static Field rohs_id = Field.Many2one("wms.rohs").label("RoHS等级");
    static Field is_batch = Field.Boolean().label("批次管理").defaultValue(Default.value(true));
    static Field quality_guarantee_period = Field.Integer().label("保质期(天)");

    static Field receive_send_control_ids = Field.One2many("wms.receive_send_ctl", "item_id").label("收发控制");
}
