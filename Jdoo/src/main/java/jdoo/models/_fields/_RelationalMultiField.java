package jdoo.models._fields;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jdoo.models.Field;
import jdoo.models.RecordSet;
import jdoo.util.Tuple;

/** Abstract class for relational fields *2many. */
public abstract class _RelationalMultiField<T extends _RelationalMultiField<T>> extends _RelationalField<T> {
    // todos

    @Override
    public Object convert_to_record(Object value, RecordSet record) {
        Collection<Object> prefetch_ids = prefetch_x2many_ids(record, this);
        Collection<?> ids = value instanceof Collection ? (Collection<?>) value : new Tuple<>(value);
        RecordSet corecords = record.pool(_comodel_name()).browse(record.env(), ids, prefetch_ids);
        if (corecords.hasField("active") && (Boolean) record.env().context().getOrDefault("active_test", true)) {
            corecords = corecords.filtered("active").with_prefetch(prefetch_ids);
        }
        return corecords;
    }

    @Override
    public Object convert_to_read(Object value, RecordSet record) {
        return ((RecordSet) value).ids();
    }

    Collection<Object> prefetch_x2many_ids(RecordSet record, Field field) {
        RecordSet records = record.browse(record.prefetch_ids());
        Collection<Object> ids_list = record.env().cache().get_values(records, field);
        Set<Object> unique = new HashSet<>();
        for (Object o : ids_list) {
            if (o instanceof Collection) {
                unique.addAll((Collection<Object>) o);
            } else {
                unique.add(o);
            }
        }
        return unique;
    }
}
