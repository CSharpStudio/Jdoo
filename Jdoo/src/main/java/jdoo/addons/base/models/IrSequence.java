package jdoo.addons.base.models;

import jdoo.tools.Selector;
import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;

public class IrSequence extends Model {
    public IrSequence() {
        _name = "ir.sequence";
        _description = "Sequence";
        _order = "name";
    }

    static Field name = fields.Char().required(true);
    static Field code = fields.Char().string("Sequence Code");
    static Field implementation = fields
            .Selection((Selector s) -> s.add("standard", "Standard").add("no_gap", "No gap")).string("Implementation")
            .required(true).$default("standard")
            .help("While assigning a sequence number to a record, the 'no gap' sequence implementation ensures that each previous sequence number has been assigned already. "
                    + //
                    "While this sequence implementation will not skip any sequence number upon assignation, there can still be gaps in the sequence if records are deleted. "
                    + //
                    "The 'no gap' implementation is slower than the standard one.");
    static Field active = fields.Boolean().$default(true);
    static Field prefix = fields.Char().help("Prefix value of the record for the sequence").trim(false);
    static Field suffix = fields.Char().help("Suffix value of the record for the sequence").trim(false);
    static Field number_next = fields.Integer().string("Next Number").required(true).$default(1)
            .help("Next number of this sequence");
    static Field number_next_actual = fields.Integer().compute("_get_number_next_actual")
            .inverse("_set_number_next_actual").string("Actual Next Number")
            .help("Next number that will be used. This number can be incremented " + //
                    "frequently so the displayed value might already be obsolete");
    static Field number_increment = fields.Integer().string("Step").required(true).$default(1)
            .help("The next number of the sequence will be incremented by this number");
    static Field padding = fields.Integer().string("Sequence Size").required(true).$default(0)
            .help("Odoo will automatically adds some '0' on the left of the " + //
                    "'Next Number' to get the required padding size.");
    static Field company_id = fields.Many2one("res.company").string("Company").$default(s -> s.env().company());
    static Field use_date_range = fields.Boolean().string("Use subsequences per date_range");
    static Field date_range_ids = fields.One2many("ir.sequence.date_range", "sequence_id").string("Subsequences");

}
