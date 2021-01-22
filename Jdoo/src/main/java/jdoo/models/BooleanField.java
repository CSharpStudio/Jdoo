package jdoo.models;

import jdoo.tools.Tuple2;

public class BooleanField extends BaseField<BooleanField> {
    public BooleanField() {
        column_type = new Tuple2<String, Object>("bool", "bool");
    }
}
