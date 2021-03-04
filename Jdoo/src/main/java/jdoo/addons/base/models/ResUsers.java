package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;
import jdoo.util.Dict;

public class ResUsers extends Model {
        public ResUsers() {
                _name = "res.users";
                _description = "Users";
                _inherits = new Dict<>(d->d.put("res.partner", "partner_id"));
                _order = "name, login";
        }

        static Field partner_id = fields.Many2one("res.partner").required(true).ondelete("restrict").auto_join(true)
                        .string("Related Partner").help("Partner-related data of the user");
        static Field login = fields.Char().required(true).help("Used to log into the system");
        static Field password = fields.Char().compute("_compute_password").inverse("_set_password")// .invisible(true)
                        .copy(false)
                        .help("Keep empty if you don\"t want the user to be able to connect on the system.");
        static Field new_password = fields.Char().string("Set Password").compute("_compute_password")
                        .inverse("_set_new_password")
                        .help("Specify a value only when creating a user or if you\"re changing the user\"s password, otherwise leave empty. After a change of password, the user has to login again.");
        static Field signature = fields.Html().string("Email Signature");

        static Field name = fields.Char().related("partner_id.name").inherited(true).readonly(false);
        static Field email = fields.Char().related("partner_id.email").inherited(true).readonly(false);

}
