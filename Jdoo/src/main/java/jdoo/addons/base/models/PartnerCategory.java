package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;

public class PartnerCategory extends Model {

    public PartnerCategory() {
        _name = "res.partner.category";
        _description = "Partner Tags";
        _order = "name";
        _parent_store = true;
    }

    static Field name = fields.Char("Tag Name").required(true).translate(true);
    static Field color = fields.Integer("Color Index");
    static Field parent_id = fields.Many2one("res.partner.category").string("Parent Category").index(true)
            .ondelete("cascade");
    static Field child_ids = fields.One2many("res.partner.category", "parent_id").string("Child Tags");
    static Field active = fields.Boolean().default_(true)
            .help("The active field allows you to hide the category without removing it.");
    static Field parent_path = fields.Char().index(true);
    static Field partner_ids = fields.Many2many("res.partner").column1("category_id").column2("partner_id")
            .string("Partners");
}
