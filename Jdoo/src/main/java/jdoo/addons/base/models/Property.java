package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;
import jdoo.tools.Selector;

public class Property extends Model {
    public Property() {
        _name = "ir.property";
        _description = "Company Property";
    }

    static Field name = fields.Char().index(true);
    static Field res_id = fields.Char().string("Resource").index(true)
            .help("If not set, acts as a default value for new resources");
    static Field company_id = fields.Many2one("res.company").string("Company").index(true);
    static Field fields_id = fields.Many2one("ir.model.fields").string("Field").ondelete("cascade").required(true)
            .index(true);
    static Field value_float = fields.Float();
    static Field value_integer = fields.Integer();
    static Field value_text = fields.Text();// will contain (char, text)
    static Field value_binary = fields.Binary().attachment(false);
    static Field value_reference = fields.Char();
    static Field value_datetime = fields.Datetime();
    static Field type = fields.Selection((Selector s) -> s.add("char", "Char")//
            .add("float", "Float")//
            .add("boolean", "Boolean")//
            .add("integer", "Integer")//
            .add("text", "Text")//
            .add("binary", "Binary")//
            .add("many2one", "Many2One")//
            .add("date", "Date")//
            .add("datetime", "DateTime")//
            .add("selection", "Selection")//
    ).required(true).$default("many2one").index(true);
}
