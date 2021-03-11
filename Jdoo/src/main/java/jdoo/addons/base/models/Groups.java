package jdoo.addons.base.models;

import java.util.Arrays;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.d;
import jdoo.models.fields;
import jdoo.util.Tuple;

public class Groups extends Model {
    public Groups() {
        _name = "res.groups";
        _description = "Access Groups";
        _rec_name = "full_name";
        _order = "name";

        _sql_constraints = Arrays.asList(new Tuple<>("name_uniq", "unique (category_id, name)",
                "The name of the group must be unique within an application!"));
    }

    static Field name = fields.Char().required(true).translate(true);
    static Field users = fields.Many2many("res.users", "res_groups_users_rel", "gid", "uid");
    // static Field model_access = fields.One2many("ir.model.access", "group_id").string("Access Controls").copy(true);
    // static Field rule_groups = fields.Many2many("ir.rule", "rule_group_rel", "group_id", "rule_group_id")
    //         .string("Rules").domain(d.on("global", "=", false));
    // static Field menu_access = fields.Many2many("ir.ui.menu", "ir_ui_menu_group_rel", "gid", "menu_id")
    //         .string("Access Menu");
    // static Field view_access = fields.Many2many("ir.ui.view", "ir_ui_view_group_rel", "group_id", "view_id")
    //         .string("Views");
    // static Field comment = fields.Text().translate(true);
    // static Field category_id = fields.Many2one("ir.module.category").string("Application").index(true);
    static Field color = fields.Integer().string("Color Index");
    static Field full_name = fields.Char().compute("_compute_full_name").string("Group Name")
            .search("_search_full_name");
    static Field share = fields.Boolean().string("Share Group")
            .help("Group created to set access rights for sharing data with some users.");
}
