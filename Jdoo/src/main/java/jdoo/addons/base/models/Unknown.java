package jdoo.addons.base.models;

import jdoo.models.Model;

/**
 * Abstract model used as a substitute for relational fields with an unknown
 * comodel.
 */
public class Unknown extends Model {
    public Unknown() {
        _name = "_unknown";
        _description = "Unknown";
    }
}