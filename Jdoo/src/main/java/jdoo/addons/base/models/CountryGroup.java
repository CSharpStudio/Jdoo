package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;

public class CountryGroup extends Model {
    public CountryGroup() {
        _description = "Country Group";
        _name = "res.country.group";
    }

    static Field name = fields.Char().required(true).translate(true);
    static Field country_ids = fields
            .Many2many("res.country", "res_country_res_country_group_rel", "res_country_group_id", "res_country_id")
            .string("Countries");            
}
