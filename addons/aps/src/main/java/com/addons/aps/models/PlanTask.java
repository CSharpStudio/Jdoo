package com.addons.aps.models;

import org.jdoo.*;

@Model.Meta(name = "aps.plan_task")
public class PlanTask extends Model {
    static Field name = Field.Char().label("名称");
    static Field start_time = Field.DateTime().label("开始时间");
    static Field end_time = Field.DateTime().label("结束时间");
    static Field duration = Field.Float().label("持续时间");
    static Field item_id = Field.Many2one("mom.item").label("物料");
    static Field qty = Field.Float().label("数量");
}
