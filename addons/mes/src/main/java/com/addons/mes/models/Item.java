package com.addons.mes.models;

import java.util.HashMap;

import org.jdoo.*;

/**
 * 物料
 * 
 * @author lrz
 */
@Model.Meta(inherit = "mom.item")
public class Item extends Model {
    static Field barcode = Field.Char().label("商品条码");
    static Field type = Field.Selection().label("基本分类").selection(Selection.value(new HashMap<String, String>() {
        {
            put("material", "原材料");
            put("semi-manufactures", "半成品");
            put("finished-product", "成品");
        }
    }));
}
