package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;

public class IrUiMenu extends Model {
    public IrUiMenu() {
        _name = "ir.ui.menu";
        _description = "Menu";
        _order = "sequence,id";
        _parent_store = true;
    }

    static Field name = fields.Char().string("Menu").required(true).translate(true);
    static Field active = fields.Boolean().$default(true);
    static Field sequence = fields.Integer().$default(10);
    static Field child_id = fields.One2many("ir.ui.menu", "parent_id").string("Child IDs");
    static Field parent_id = fields.Many2one("ir.ui.menu").string("Parent Menu").index(true).ondelete("restrict");
    static Field parent_path = fields.Char().index(true);
    static Field groups_id = fields.Many2many("res.groups", "ir_ui_menu_group_rel", "menu_id", "gid").string("Groups")
            .help("If you have groups, the visibility of this menu will be based on these groups. " + //
                    "If this field is empty, Odoo will compute visibility based on the related object's read access.");
    static Field complete_name = fields.Char().compute("_compute_complete_name").string("Full Path");
    static Field web_icon = fields.Char().string("Web Icon File");
    // static Field action = fields.Reference(selection=[("ir.actions.report",
    // "ir.actions.report"),
    // ("ir.actions.act_window", "ir.actions.act_window"),
    // ("ir.actions.act_url", "ir.actions.act_url"),
    // ("ir.actions.server", "ir.actions.server"),
    // ("ir.actions.client", "ir.actions.client")])

    static Field web_icon_data = fields.Binary().string("Web Icon Image").attachment(true);
}
