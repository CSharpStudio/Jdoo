package jdoo.models;

import jdoo.util.Tuple;
import jdoo.tools.Tuple2;

public class FloatField extends BaseField<FloatField> {
    public FloatField() {
        group_operator = "sum";
        column_cast_from = new Tuple<>("int4", "numeric", "float8");
    }

    @Override
    Tuple2<String, Object> column_type() {
        if (column_type == null) {
            if (digits != null)
                column_type = new Tuple2<String, Object>("numeric", "numeric");
            else
                column_type = new Tuple2<String, Object>("float8", "double precision");
        }
        return column_type;
    }

    Tuple2<Integer, Integer> digits;

    public FloatField digits(Tuple2<Integer, Integer> digits) {
        this.digits = digits;
        return this;
    }

    public FloatField digits(int total, int precision) {
        this.digits = new Tuple2<>(total, precision);
        return this;
    }
}
