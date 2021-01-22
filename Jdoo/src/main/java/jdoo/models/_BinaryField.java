package jdoo.models;

import java.util.HashSet;

import jdoo.tools.Tuple2;

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
    Tuple2<String, Object> column_type() {
        if (column_type == null && (attachment == null || !attachment)) {
            column_type = new Tuple2<String, Object>("bytea", "bytea");
        }
        return column_type;
    }
}
