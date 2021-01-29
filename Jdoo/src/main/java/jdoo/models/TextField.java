package jdoo.models;

import jdoo.tools.Tuple;
import jdoo.tools.Tuple2;

public class TextField extends StringField<TextField> {
    public TextField() {
        column_type = new Tuple2<String, Object>("text", "text");
        column_cast_from = new Tuple<>("varchar");
    }
}
