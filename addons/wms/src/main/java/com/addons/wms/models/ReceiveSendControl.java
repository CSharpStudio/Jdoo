package com.addons.wms.models;

import org.jdoo.*;

@Model.Meta(name = "wms.receive_send_ctl")
public class ReceiveSendControl extends Model {
    static Field storage_id = Field.Many2one("wms.storage").label("仓库");
    static Field over_receive_rate = Field.Float().label("超收比例上限");
    static Field over_receive_qty = Field.Float().label("超收数量上限");
    static Field over_send_rate = Field.Float().label("超发比例上限");
    static Field over_send_qty = Field.Float().label("超发数量上限");

    static Field item_id = Field.Many2one("mom.item").label("物料");
}
