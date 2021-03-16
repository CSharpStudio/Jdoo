package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.RecordSet;
import jdoo.models.d;
import jdoo.models.fields;

public class IrModelAccess extends Model {

    public IrModelAccess() {
        _name = "ir.model.access";
        _description = "Model Access";
        _order = "model_id,group_id,name,id";
    }

    static Field name = fields.Char().required(true).index(true);
    static Field active = fields.Boolean().$default(true).help(
            "If you uncheck the active field, it will disable the ACL without deleting it (if you delete a native ACL, it will be re-created when you reload the module).");
    static Field model_id = fields.Many2one("ir.model", "Object").required(true).domain(d.on("transient", "=", false))
            .index(true).ondelete("cascade");
    static Field group_id = fields.Many2one("res.groups", "Group").ondelete("cascade").index(true);
    static Field perm_read = fields.Boolean("Read Access");
    static Field perm_write = fields.Boolean("Write Access");
    static Field perm_create = fields.Boolean("Create Access");
    static Field perm_unlink = fields.Boolean("Delete Access");

    public boolean check_groups(RecordSet self, String group) {
        return true;
    }

    public boolean check(RecordSet self, String model, String mode, boolean raise_exception) {
        return true;
    }
}
