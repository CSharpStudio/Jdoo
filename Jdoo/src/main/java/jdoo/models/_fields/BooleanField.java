package jdoo.models._fields;

import jdoo.util.Pair;

public class BooleanField extends BaseField<BooleanField> {
    public BooleanField() {
        column_type = new Pair<>("bool", "bool");
    }
}
