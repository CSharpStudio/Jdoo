package com.addons.mom.models;

import org.jdoo.*;
/**
 * 工单
 * 
 * @author lrz
 */
@Model.Meta(name = "mom.work_order")
public class WorkOrder extends Model{
    static Field name = Field.Char().label("工单号").index(true).required(true);
    static Field item_id = Field.Many2one("mom.item").label("物料").required(true);
}
