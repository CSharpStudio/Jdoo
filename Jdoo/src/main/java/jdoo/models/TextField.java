package jdoo.models;

import jdoo.tools.Tuple2;

public class TextField extends StringField<TextField> {
    public TextField() {
        column_type = new Tuple2<String, Object>("text", "text");
    }
}
