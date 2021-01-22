package jdoo.base;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;

public class ResUsers extends Model {
    public ResUsers() {
        _name = "res.users";
        _description = "Users";
    }

    public static Field name = fields.Char("name of user");
    public static Field bool = fields.Boolean("name of user").required(true);

}
