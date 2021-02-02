package jdoo.models._fields;

import jdoo.util.Tuple;
import jdoo.util.Pair;

public class DateTimeField extends BaseField<DateTimeField> {
    public DateTimeField() {
        column_type = new Pair<>("timestamp", "timestamp");
        column_cast_from = new Tuple<>("date");
    }
}
