package jdoo.addons.base.models;

import jdoo.models.Model;

public class Currency extends Model {
    public Currency() {
        _name = "res.currency";
        _description = "Currency";
        _order = "active desc, name";
    }

}
