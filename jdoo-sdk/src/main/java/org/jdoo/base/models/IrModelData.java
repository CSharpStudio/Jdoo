package org.jdoo.base.models;

import java.util.Arrays;

import org.jdoo.*;
import org.jdoo.data.Cursor;

@Model.Meta(name = "ir.model.data", description = "模型数据", order = "module, model, name")
public class IrModelData extends Model {
    static Field name = Field.Char().label("扩展ID").required();
    static Field model = Field.Char().label("模型名称").required();
    static Field module = Field.Char().defaultValue(Default.value("")).required();
    static Field res_id = Field.Char();

    public Records findRef(Records rec, String xmlid) {
        String[] parts = xmlid.split("\\.", 2);
        String sql = "SELECT id, model, res_id FROM ir_model_data WHERE module=%s AND name=%s";
        Cursor cr = rec.getEnv().getCursor();
        cr.execute(sql, Arrays.asList(parts));
        Object[] row = cr.fetchOne();
        if (row.length == 0) {
            return null;
        }
        return rec.getEnv().get((String) row[1]).browse((String) row[2]);
    }
}
