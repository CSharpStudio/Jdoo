package jdoo.addons.base.models;

import java.util.Arrays;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;
import jdoo.util.Tuple;

public class IrModelConstraint extends Model {
    public IrModelConstraint() {
        _name = "ir.model.constraint";
        _description = "Model Constraint";

        _sql_constraints = Arrays.asList(new Tuple<>("module_name_uniq", "unique(name, module)",
                "Constraints with the same name are unique per module."));
    }

    static Field name = fields.Char().string("Constraint").required(true).index(true)
            .help("PostgreSQL constraint or foreign key name.");
    static Field definition = fields.Char().help("PostgreSQL constraint definition");
    static Field message = fields.Char().help("Error message returned when the constraint is violated.")
            .translate(true);
    static Field model = fields.Many2one("ir.model").required(true).ondelete("cascade").index(true);
    static Field module = fields.Many2one("ir.module.module").required(true).index(true);
    static Field type = fields.Char().string("Constraint Type").required(true).size(1).index(true)
            .help("Type of the constraint: `f` for a foreign key, " + //
                    "`u` for other constraints.");
    static Field write_date = fields.Datetime();
    static Field create_date = fields.Datetime();
}
