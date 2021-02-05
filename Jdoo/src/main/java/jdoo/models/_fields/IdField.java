package jdoo.models._fields;

import java.util.Collection;
import java.util.Map;

import jdoo.data.PgVarchar;
import jdoo.exceptions.TypeErrorException;
import jdoo.models.RecordSet;
import jdoo.util.Dict;
import jdoo.util.Pair;

public class IdField extends BaseField<IdField> {
    public IdField() {
        column_type = new Pair<>("varchar", new PgVarchar(32));
        setattr(Slots.string, "ID");
        setattr(Slots.store, true);
        setattr(Slots.readonly, true);
        setattr(Slots.prefetch, false);
    }

    @Override
    public boolean update_db(RecordSet model, Map<String, Dict> columns) {
        return false;// this column is created with the table
    }

    @Override
    public Object get(RecordSet record) {
        Collection<?> ids = record.ids();
        if (ids.size() == 0) {
            return null;
        } else if (ids.size() == 1) {
            return record.id();
        }
        throw new TypeErrorException(String.format("Expected singleton: %s", record));
    }

    @Override
    public void set(RecordSet records, Object value) {
        throw new TypeErrorException("field 'id' cannot be assigned");
    }
}
