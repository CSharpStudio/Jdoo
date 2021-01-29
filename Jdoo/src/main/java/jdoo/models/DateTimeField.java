package jdoo.models;

import jdoo.tools.Tuple;
import jdoo.tools.Tuple2;

public class DateTimeField extends BaseField<DateTimeField> {
    public DateTimeField() {
        column_type = new Tuple2<String, Object>("timestamp", "timestamp");
        column_cast_from = new Tuple<>("date");
    }
}
