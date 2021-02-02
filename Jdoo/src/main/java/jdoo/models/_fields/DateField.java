package jdoo.models._fields;

import jdoo.util.Tuple;
import jdoo.util.Pair;

public class DateField extends BaseField<DateField> {
    public DateField() {
        column_type = new Pair<>("date", "date");
        column_cast_from = new Tuple<>("timestamp");
    }
}
