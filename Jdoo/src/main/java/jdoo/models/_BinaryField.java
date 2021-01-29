package jdoo.models;

import java.util.HashSet;
import jdoo.util.Pair;

public abstract class _BinaryField<T extends _BinaryField<T>> extends BaseField<T> {
    public _BinaryField() {
        prefetch = false;
        depends_context = new HashSet<String>();
        depends_context.add("bin_size");
    }

    Boolean attachment;

    @SuppressWarnings("unchecked")
    public T attachment(boolean attachment) {
        this.attachment = attachment;
        return (T) this;
    }

    @Override
    Pair<String, Object> column_type() {
        if (column_type == null && (attachment == null || !attachment)) {
            column_type = new Pair<>("bytea", "bytea");
        }
        return column_type;
    }
}
