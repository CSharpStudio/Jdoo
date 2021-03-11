package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;

public class Lang extends Model {
    public Lang() {
        _name = "res.lang";
        _description = "Languages";
        _order = "active desc,name";
    }

    static Field name = fields.Char().required(true);
}
