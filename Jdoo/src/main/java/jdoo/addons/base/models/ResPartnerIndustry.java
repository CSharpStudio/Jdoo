package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;

public class ResPartnerIndustry extends Model {
    public ResPartnerIndustry() {
        _description = "Industry";
        _name = "res.partner.industry";
        _order = "name";
    }

    static Field name = fields.Char("Name").translate(true);
    static Field full_name = fields.Char("Full Name").translate(true);
    static Field active = fields.Boolean("Active").$default(true);
}
