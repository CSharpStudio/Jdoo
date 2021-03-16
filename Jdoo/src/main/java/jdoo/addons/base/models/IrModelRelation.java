package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;

public class IrModelRelation extends Model {
    public IrModelRelation() {
        _name = "ir.model.relation";
        _description = "Relation Model";
    }

    static Field name = fields.Char().string("Relation Name").required(true).index(true)
            .help("PostgreSQL table name implementing a many2many relation.");
    static Field model = fields.Many2one("ir.model").required(true).index(true);
    static Field module = fields.Many2one("ir.module.module").required(true).index(true);
    static Field write_date = fields.Datetime();
    static Field create_date = fields.Datetime();

}
