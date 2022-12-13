package org.jdoo.base.models;

import java.util.Collection;

import org.jdoo.*;
import org.jdoo.core.Environment;
import org.jdoo.core.MetaModel;
import org.jdoo.core.Registry;
import org.jdoo.data.Cursor;
import org.jdoo.data.SqlDialect;
import org.jdoo.util.KvMap;
import org.jdoo.utils.StringUtils;

@Model.Meta(name = "ir.model.constraint", label = "模型约束")
public class IrModelConstraint extends Model {
    static Field name = Field.Char().label("名称").index(true).required(true);
    static Field definition = Field.Text().label("定义");
    static Field message = Field.Char().label("信息");
    static Field model_id = Field.Many2one("ir.model").label("模型");
    static Field module_id = Field.Many2one("ir.module").label("模块");
    static Field type = Field.Text().label("类型");

    public void reflectConstraint(Records rec, String name, String type, String definition, String message,
            String model, String module) {
        Environment env = rec.getEnv();
        rec = rec.find(Criteria.equal("name", name).and(Criteria.equal("module_id.name", module)));
        if (!rec.any()) {
            rec.create(new KvMap()
                    .set("name", name)
                    .set("definition", definition)
                    .set("message", message)
                    .set("model_id", env.get("ir.model").find(Criteria.equal("model", model)).getId())
                    .set("module_id", env.get("ir.module").find(Criteria.equal("name", module)).getId())
                    .set("type", type));
        } else {
            rec.set("definition", definition);
            rec.set("message", message);
            rec.set("type", type);
        }
    }

    public void reflectModels(Records rec, Collection<String> models, String module) {
        Cursor cr = rec.getEnv().getCursor();
        SqlDialect sd = cr.getSqlDialect();
        Registry reg = rec.getEnv().getRegistry();
        for (String model : models) {
            MetaModel meta = reg.get(model);
            for (org.jdoo.core.UniqueConstraint uc : meta.getUniques()) {
                String name = sd.limitIdentity(meta.getTable() + "_" + uc.getName());
                String definition = sd.addUniqueConstraint(cr, meta.getTable(), name, uc.getFields());
                if (StringUtils.isNotEmpty(definition)) {
                    reflectConstraint(rec, name, "u", definition, uc.getMessage(), meta.getName(), uc.getModule());
                }
            }
        }
    }
}
