package jdoo.addons.base.models;

import java.util.Arrays;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;
import jdoo.util.Tuple;

public class Company extends Model {
    public Company() {
        _name = "res.company";
        _description = "Companies";
        _order = "sequence, name";

        _sql_constraints = Arrays
                .asList(new Tuple<>("name_uniq", "unique (name)", "The company name must be unique !"));
    }

    static Field name = fields.Char().related("partner_id.name").string("Company Name").required(true).store(true)
            .readonly(false);
    static Field sequence = fields.Integer().help("Used to order Companies in the company switcher").$default(10);
    static Field parent_id = fields.Many2one("res.company").string("Parent Company").index(true);
    static Field child_ids = fields.One2many("res.company", "parent_id").string("Child Companies");
    static Field partner_id = fields.Many2one("res.partner").string("Partner").required(true);
}
