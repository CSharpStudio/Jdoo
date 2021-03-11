package jdoo.models._fields;

import jdoo.data.PgVarchar;
import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.tools.Sql;
import jdoo.util.Tuple;
import jdoo.util.Kvalues;
import jdoo.util.Pair;

/**
 * Basic string field, can be length-limited, usually displayed as a single-line
 * string in clients.
 * 
 * :param int size: the maximum size of values stored for that field
 * 
 * :param bool trim: states whether the value is trimmed or not (by default,
 * ``True``). Note that the trim operation is applied only by the web client.
 * 
 * :param translate: enable the translation of the field's values; use
 * ``translate=True`` to translate field values as a whole; ``translate`` may
 * also be a callable such that ``translate(callback, value)`` translates
 * ``value`` by using ``callback(term)`` to retrieve the translation of terms.
 */
public class CharField extends _StringField<CharField> {
    /** maximum size of values (deprecated) */
    public static final Slot size = new Slot("size", null);
    /** whether value is trimmed (only by web client) */
    public static final Slot trim = new Slot("trim", true);

    public CharField() {
        column_cast_from = new Tuple<>("text");
    }

    @Override
    public Pair<String, Object> column_type() {
        if (column_type == null) {
            column_type = new Pair<>("varchar", new PgVarchar(getattr(Integer.class, size)));
        }
        return column_type;
    }

    public CharField size(int size) {
        setattr(CharField.size, size);
        return this;
    }

    public CharField trim(boolean trim) {
        setattr(CharField.trim, trim);
        return this;
    }

    @Override
    public void update_db_column(RecordSet model, Kvalues column) {
        if (column != null && "varchar".equals(column.get("udt_name")) && column.get("character_maximum_length") != null
                && (!hasattr(size) || (int) column.get("character_maximum_length") < getattr(Integer.class, size))) {
            // the column's varchar size does not match self.size; convert it
            Sql.convert_column(model.env().cr(), model.table(), getName(), column_type().second().toString());
        }
        super.update_db_column(model, column);
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Kvalues values, boolean validate) {
        if (value == null) {
            return null;
        }
        // we need to convert the string to a unicode object to be able
        // to evaluate its length (and possibly truncate it) reliably
        String str = value.toString().replace("'", "''");
        if (hasattr(size)) {
            str = str.substring(0, getattr(Integer.class, size));
        }
        return str;
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        if (value == null) {
            return null;
        }
        String str = value.toString().replace("'", "''");
        if (hasattr(size)) {
            str = str.substring(0, getattr(Integer.class, size));
        }
        return str;
    }
}
