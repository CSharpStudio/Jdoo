package jdoo.models._fields;

import jdoo.data.PgVarchar;
import jdoo.util.Tuple;
import jdoo.util.Pair;

public class CharField extends StringField<CharField> {
    public CharField() {
        column_cast_from = new Tuple<>("text");
    }

    Integer size;
    Boolean trim;

    boolean get_trim() {
        return trim != null && trim;
    }

    @Override
    public Pair<String, Object> column_type() {
        if (column_type == null) {
            column_type = new Pair<>("varchar", new PgVarchar(size));
        }
        return column_type;
    }

    public CharField size(int size) {
        this.size = size;
        return this;
    }

    public CharField trim(boolean trim) {
        this.trim = trim;
        return this;
    }
}
