package jdoo.models._fields;

import jdoo.models.RecordSet;
import jdoo.util.Dict;
import jdoo.util.Pair;
import jdoo.util.TypeUtils;

public class BooleanField extends BaseField<BooleanField> {
    public BooleanField() {
        column_type = new Pair<>("bool", "bool");
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Dict values, boolean validate) {
        return TypeUtils.toBoolean(value);
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        return TypeUtils.toBoolean(value);
    }

    @Override
    public Object convert_to_export(Object value, RecordSet record) {
        return value;
    }
}
