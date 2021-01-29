package jdoo.models;

import jdoo.util.Tuple;
import jdoo.tools.Tuple2;

public class DateField extends BaseField<DateField> {
    public DateField() {
        column_type = new Tuple2<String, Object>("date", "date");
        column_cast_from = new Tuple<>("timestamp");
    }
}
