package jdoo.addons.base.models;

import java.util.Arrays;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;
import jdoo.util.Tuple;

public class IrRule extends Model {
    public IrRule() {
        _name = "ir.rule";
        _description = "Record Rule";
        _order = "model_id DESC,id";

        _sql_constraints = Arrays.asList(new Tuple<>("no_access_rights",
                "CHECK (perm_read!=False or perm_write!=False or perm_create!=False or perm_unlink!=False)",
                "Rule must have at least one checked access right !"));
    }

    static Field name = fields.Char().index(true);
    static Field active = fields.Boolean().$default(true).help(
            "If you uncheck the active field, it will disable the record rule without deleting it (if you delete a native record rule, it may be re-created when you reload the module).");
    static Field model_id = fields.Many2one("ir.model").string("Object").index(true).required(true).ondelete("cascade");
    static Field groups = fields.Many2many("res.groups", "rule_group_rel", "rule_group_id", "group_id");
    static Field domain_force = fields.Text().string("Domain");
    static Field perm_read = fields.Boolean().string("Apply for Read").$default(true);
    static Field perm_write = fields.Boolean().string("Apply for Write").$default(true);
    static Field perm_create = fields.Boolean().string("Apply for Create").$default(true);
    static Field perm_unlink = fields.Boolean().string("Apply for Delete").$default(true);
}
