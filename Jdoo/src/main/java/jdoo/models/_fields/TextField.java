package jdoo.models._fields;

import jdoo.models.RecordSet;
import jdoo.util.Pair;
import jdoo.util.Tuple;

/**
 * Very similar to :class:`~.Char` but used for longer contents, does not have a
 * size and usually displayed as a multiline text box.
 * 
 * :param translate: enable the translation of the field's values; use
 * ``translate=True`` to translate field values as a whole; ``translate`` may
 * also be a callable such that ``translate(callback, value)`` translates
 * ``value`` by using ``callback(term)`` to retrieve the translation of terms.
 */
public class TextField extends _StringField<TextField> {
    public TextField() {
        column_type = new Pair<>("text", "text");
        column_cast_from = new Tuple<>("varchar");
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        if (value == null || Boolean.FALSE.equals(value)) {
            return null;
        }
        return value.toString();
    }
}
