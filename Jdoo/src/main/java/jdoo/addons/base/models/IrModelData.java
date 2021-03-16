package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;
import jdoo.models._fields.DateTimeField;

public class IrModelData extends Model {
    public IrModelData() {
        _name = "ir.model.data";
        _description = "Model Data";
        _order = "module, model, name";
    }

    static Field name = fields.Char().string("External Identifier").required(true)
            .help("External Key/Identifier that can be used for " + //
                    "data integration with third-party systems");
    static Field complete_name = fields.Char().compute("_compute_complete_name").string("Complete ID");
    static Field model = fields.Char().string("Model Name").required(true);
    static Field module = fields.Char().$default("").required(true);
    static Field res_id = fields.Integer().string("Record ID").help("ID of the target record in the database");
    static Field noupdate = fields.Boolean().string("Non Updatable").$default(false);
    static Field date_update = fields.Datetime().string("Update Date").$default(DateTimeField::now);
    static Field date_init = fields.Datetime().string("Init Date").$default(DateTimeField::now);
    static Field reference = fields.Char().string("Reference").compute("_compute_reference").readonly(true)
            .store(false);
}
