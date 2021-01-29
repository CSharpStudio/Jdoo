package jdoo.models;

import jdoo.util.Tuple;
import jdoo.tools.Tuple2;

public class MonetaryField extends BaseField<MonetaryField> {
    public MonetaryField() {
        group_operator = "sum";
        column_type = new Tuple2<String, Object>("numeric", "numeric");
        column_cast_from = new Tuple<>("float8");
    }

    String currency_field;

    public MonetaryField currency_field(String currency_field) {
        this.currency_field = currency_field;
        return this;
    }
}
