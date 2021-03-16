package jdoo.addons.base.models;

import java.util.Arrays;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.d;
import jdoo.models.fields;
import jdoo.util.Tuple;

public class IrModelSelection extends Model {
    public IrModelSelection() {
        _name = "ir.model.fields.selection";
        _order = "sequence, id";
        _description = "Fields Selection";
        _sql_constraints = Arrays.asList(new Tuple<>("selection_field_uniq", "unique(field_id, value)",
                "Selections values must be unique per field"));
    }

    static Field field_id = fields.Many2one("ir.model.fields").required(true).ondelete("cascade").index(true)
            .domain(d.on("ttype", "in", Arrays.asList("selection", "reference")));
    static Field value = fields.Char().required(true);
    static Field name = fields.Char().translate(true).required(true);
    static Field sequence = fields.Integer().$default(1000);
}
