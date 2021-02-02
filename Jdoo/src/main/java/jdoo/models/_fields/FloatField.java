package jdoo.models._fields;

import jdoo.util.Tuple;
import jdoo.util.Pair;

public class FloatField extends BaseField<FloatField> {
    public FloatField() {
        group_operator = "sum";
        column_cast_from = new Tuple<>("int4", "numeric", "float8");
    }

    @Override
    public Pair<String, Object> column_type() {
        if (column_type == null) {
            if (digits != null)
                column_type = new Pair<>("numeric", "numeric");
            else
                column_type = new Pair<>("float8", "double precision");
        }
        return column_type;
    }

    Pair<Integer, Integer> digits;

    public FloatField digits(Pair<Integer, Integer> digits) {
        this.digits = digits;
        return this;
    }

    public FloatField digits(int total, int precision) {
        this.digits = new Pair<>(total, precision);
        return this;
    }
}
