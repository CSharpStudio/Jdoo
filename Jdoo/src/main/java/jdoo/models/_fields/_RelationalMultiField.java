package jdoo.models._fields;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jdoo.models.Field;
import jdoo.models.RecordSet;
import jdoo.util.Pair;
import jdoo.util.Tuple;

/** Abstract class for relational fields *2many. */
public abstract class _RelationalMultiField<T extends _RelationalMultiField<T>> extends _RelationalField<T> {
    // todos

    public boolean _update(RecordSet records, Object value) {
        // todo
        return true;
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        // cache format: tuple(ids)
        // todo
        return super.convert_to_cache(value, record, validate);
    }

    @Override
    public Object convert_to_record(Object value, RecordSet record) {
        // use registry to avoid creating a recordset for the model
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

    @Override
    public Object convert_to_write(java.lang.Object value, RecordSet record) {
        // TODO Auto-generated method stub
        return super.convert_to_write(value, record);
    }

    @Override
    public Object convert_to_export(java.lang.Object value, RecordSet record) {
        // TODO Auto-generated method stub
        return super.convert_to_export(value, record);
    }

    @Override
    public String convert_to_display_name(java.lang.Object value, RecordSet record) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void _setup_regular_full(RecordSet model) {
        // TODO Auto-generated method stub
        super._setup_regular_full(model);
    }

    @Override
    public void create(List<Pair<RecordSet, Object>> record_values) {
        // TODO Auto-generated method stub
        super.create(record_values);
    }

    @Override
    public RecordSet write(RecordSet records, Object value) {
        // TODO Auto-generated method stub
        return super.write(records, value);
    }

    RecordSet write_batch() {
        // todo
        return null;
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
