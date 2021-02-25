package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.RecordSet;
import jdoo.models.fields;
import jdoo.util.Default;
import jdoo.util.Kvalues;

public class IrDefault extends Model {
        public IrDefault() {
                _name = "ir.default";
                _description = "Default Values";
                //_rec_name = "field_id";
        }

        // public static Field field_id = fields.Many2one("ir.model.fields").string("Field").required(true)
        //                 .ondelete("cascade").index(true);
        public static Field user_id = fields.Many2one("res.users").string("User").ondelete("cascade").index(true)
                        .help("If set, action binding only applies for this user.");
        // public static Field company_id =
        // fields.Many2one("res.company").string("Company").ondelete("cascade")
        // .index(true).help("If set, action binding only applies for this company");
        public static Field condition = fields.Char("Condition").help("If set, applies the default upon condition.");
        public static Field json_value = fields.Char("Default Value (JSON format)").required(true);

        public Kvalues get_model_defaults(RecordSet self, String model_name, @Default boolean condition) {
                Kvalues result = new Kvalues();
                return result;
        }
}
