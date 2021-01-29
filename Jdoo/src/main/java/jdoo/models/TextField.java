package jdoo.models;

import jdoo.util.Pair;
import jdoo.util.Tuple;

public class TextField extends StringField<TextField> {
    public TextField() {
        column_type = new Pair<>("text", "text");
        column_cast_from = new Tuple<>("varchar");
    }
}
