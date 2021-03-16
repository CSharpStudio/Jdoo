package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;

public class ViewCustom extends Model {
    public ViewCustom() {
        _name = "ir.ui.view.custom";
        _description = "Custom View";
        _order = "create_date desc"; // search(limit=1) should return the last customization
    }

    static Field ref_id = fields.Many2one("ir.ui.view").string("Original View").index(true).required(true)
            .ondelete("cascade");
    static Field user_id = fields.Many2one("res.users").string("User").index(true).required(true).ondelete("cascade");
    static Field arch = fields.Text().string("View Architecture").required(true);
}
