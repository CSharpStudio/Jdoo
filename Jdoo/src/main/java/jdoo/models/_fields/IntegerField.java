package jdoo.models._fields;

import jdoo.models.RecordSet;
import jdoo.util.Dict;
import jdoo.util.Pair;
import jdoo.util.TypeUtils;

public class IntegerField extends BaseField<IntegerField> {
    public IntegerField() {
        setattr(Slots.group_operator, "sum");
        column_type = new Pair<>("int4", "int4");
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Dict values, boolean validate) {
        return TypeUtils.toInteger(value);
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        return TypeUtils.toInteger(value);
    }

    @Override
    public Object convert_to_record(Object value, RecordSet record) {
        return value == null ? 0 : value;
    }
}
