package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;

public class IrSequenceDateRange extends Model {
    public IrSequenceDateRange() {
        _name = "ir.sequence.date_range";
        _description = "Sequence Date Range";
        _rec_name = "sequence_id";
    }

    static Field date_from = fields.Date().string("From").required(true);
    static Field date_to = fields.Date().string("To").required(true);
    static Field sequence_id = fields.Many2one("ir.sequence").string("Main Sequence").required(true)
            .ondelete("cascade");
    static Field number_next = fields.Integer().string("Next Number").required(true).$default(1)
            .help("Next number of this sequence");
    static Field number_next_actual = fields.Integer().compute("_get_number_next_actual")
            .inverse("_set_number_next_actual").string("Actual Next Number")
            .help("Next number that will be used. This number can be incremented " + //
                    "frequently so the displayed value might already be obsolete");

}
