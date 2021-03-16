package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;
import jdoo.tools.Selector;

public class IrModelFields extends Model {

    public IrModelFields() {
        _name = "ir.model.fields";
        _description = "Fields";
        _order = "name";
        _rec_name = "field_description";
    }

    static Field name = fields.Char().string("Field Name").$default("x_").required(true).index(true);
    static Field complete_name = fields.Char().index(true);
    static Field model = fields.Char().string("Object Name").required(true).index(true)
            .help("The technical name of the model this field belongs to");
    static Field relation = fields.Char().string("Object Relation")
            .help("For relationship fields, the technical name of the target model");
    static Field relation_field = fields.Char().help(
            "For one2many fields, the field on the target model that implement the opposite many2one relationship");
    static Field relation_field_id = fields.Many2one("ir.model.fields").compute("_compute_relation_field_id")
            .store(true).ondelete("cascade").string("Relation field");
    static Field model_id = fields.Many2one("ir.model").string("Model").required(true).index(true).ondelete("cascade")
            .help("The model this field belongs to");
    static Field field_description = fields.Char().string("Field Label").$default("").required(true).translate(true);
    static Field help = fields.Text().string("Field Help").translate(true);
    static Field ttype = fields.Selection(/* selection = FIELD_TYPES */).string("Field Type").required(true);
    static Field selection = fields.Char().string("Selection Options (Deprecated)").compute("_compute_selection")
            .inverse("_inverse_selection");
    static Field selection_ids = fields.One2many("ir.model.fields.selection", "field_id").string("Selection Options")
            .copy(true);
    static Field copied = fields.Boolean().string("Copied")
            .help("Whether the value is copied when duplicating a record.");
    static Field related = fields.Char().string("Related Field")
            .help("The corresponding related field, if any. This must be a dot-separated list of field names.");
    static Field related_field_id = fields.Many2one("ir.model.fields").compute("_compute_related_field_id").store(true)
            .string("Related field").ondelete("cascade");
    static Field required = fields.Boolean();
    static Field readonly = fields.Boolean();
    static Field index = fields.Boolean().string("Indexed");
    static Field translate = fields.Boolean().string("Translatable")
            .help("Whether values for this field can be translated (enables the translation mechanism for that field)");
    static Field size = fields.Integer();
    static Field state = fields.Selection((Selector s) -> s.add("manual", "Custom Field").add("base", "Base Field"))
            .string("Type").$default("manual").required(true).readonly(true).index(true);
    static Field on_delete = fields
            .Selection(
                    (Selector s) -> s.add("cascade", "Cascade").add("set null", "Set NULL").add("restrict", "Restrict"))
            .string("On Delete").$default("set null").help("On delete property for many2one fields");
    static Field domain = fields.Char().$default("[]")
            .help("The optional domain to restrict possible values for relationship fields, " + //
                    "specified as a Python expression defining a list of triplets. " + //
                    "For example: [('color','=','red')]");
    // # CLEANME unimplemented field (empty table)
    static Field groups = fields.Many2many("res.groups", "ir_model_fields_group_rel", "field_id", "group_id");

    static Field selectable = fields.Boolean().$default(true);
    static Field modules = fields.Char().compute("_in_modules").string("In Apps")
            .help("List of modules in which the field is defined");
    static Field relation_table = fields.Char()
            .help("Used for custom many2many fields to define a custom relation table name");
    static Field column1 = fields.Char().string("Column 1").help("Column referring to the record in the model table");
    static Field column2 = fields.Char().string("Column 2").help("Column referring to the record in the comodel table");
    static Field compute = fields.Text().help("Code to compute the value of the field.\n" + //
            "Iterate on the recordset 'self' and assign the field's value:\n\n" + //
            "    for record in self:\n" + //
            "        record['size'] = len(record.name)\n\n" + //
            "Modules time, datetime, dateutil are available.");
    static Field depends = fields.Char().string("Dependencies").help("Dependencies of compute method; " + //
            "a list of comma-separated field names, like\n\n" + //
            "    name, partner_id.name");
    static Field store = fields.Boolean().string("Stored").$default(true)
            .help("Whether the value is stored in the database.");
}
