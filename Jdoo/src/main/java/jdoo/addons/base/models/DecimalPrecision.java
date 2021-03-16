package jdoo.addons.base.models;

import java.util.Arrays;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;
import jdoo.util.Tuple;

public class DecimalPrecision extends Model {
    public DecimalPrecision() {
        _name = "decimal.precision";
        _description = "Decimal Precision";
        _sql_constraints = Arrays.asList(
                new Tuple<>("name_uniq", "unique (name)", "Only one value can be defined for each given usage!"));
    }

    static Field name = fields.Char("Usage").index(true).required(true);
    static Field digits = fields.Integer("Digits").required(true).$default(2);
}
