package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;

public class PartnerTitle extends Model {
    public PartnerTitle() {
        _name = "res.partner.title";
        _order = "name";
        _description = "Partner Title";
    }

    static Field name = fields.Char().string("Title").required(true).translate(true);
    static Field shortcut = fields.Char().string("Abbreviation").translate(true);
}
