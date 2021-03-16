package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.TransientModel;
import jdoo.models.fields;

public class WizardModelMenu extends TransientModel {
    public WizardModelMenu() {
        _name = "wizard.ir.model.menu.create";
        _description = "Create Menu Wizard";
    }

    static Field menu_id = fields.Many2one("ir.ui.menu").string("Parent Menu").required(true).ondelete("cascade");
    static Field name = fields.Char().string("Menu Name").required(true);
}
