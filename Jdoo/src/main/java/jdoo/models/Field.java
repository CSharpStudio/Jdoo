package jdoo.models;

import jdoo.apis.Cache;
import jdoo.tools.Default;
import jdoo.tools.IdValues;

public class Field extends MetaField {
    public Object get(Self record) {
        return record.env().cache().get(record, this);
    }

    public void set(Self record, Object value) {
        record.env().cache().set(record, this, value);
    }

    public Self write(Self records, Object value) {
        // records.env().remove_to_compute(this, records);
        Cache cache = records.env().cache();
        Object cache_value = convert_to_cache(value, records, true);
        records = cache.get_records_different_from(records, this, cache_value);
        if (!records.hasId())
            return records;
        for (Self rec : records) {
            cache.set(rec, this, cache_value);
        }
        if (store()) {
            IdValues towrite = records.env().all().towrite(records.getName());
            Self record = records.browse(records.id());
            Object write_value = convert_to_record(cache_value, record);
            Object column_value = convert_to_column(write_value, record, null, true);
            for (Self rec : records) {
                towrite.set(rec.id(), getName(), column_value);
            }
        }
        return records;
    }

    String convert_to_display_name(Object value, Self record) {
        return value == null ? "" : value.toString();
    }

    Object convert_to_column(Object value, Self record, @Default Object values, @Default("true") boolean validate) {
        if (value == null)
            return null;
        return value.toString();
        // return pycompat.to_text(value)
    }

    /**
     * Convert ``value`` to the cache format; ``value`` may come from an assignment,
     * or have the format of methods :meth:`Model.read` or :meth:`Model.write`. If
     * the value represents a recordset, it should be added for prefetching on
     * ``record``.
     * 
     * @param value
     * @param record
     * @param validate
     * @return
     */
    public Object convert_to_cache(Object value, Self record, @Default("true") boolean validate) {
        return value;
    }

    /**
     * Convert ``value`` from the cache format to the record format. If the value
     * represents a recordset, it should share the prefetching of ``record``
     * 
     * @param value
     * @param record
     * @return
     */
    public Object convert_to_record(Object value, Self record) {
        return value;
    }

    /**
     * Convert ``value`` from the record format to the format returned by method
     * :meth:`Model.read`.
     * 
     * @param value
     * @param record
     * @return
     */
    public Object convert_to_read(Object value, Self record) {
        return value;
    }

    /**
     * Convert ``value`` from any format to the format of method
     * :meth:`Model.write`.
     * 
     * @param value
     * @param record
     * @return
     */
    public Object convert_to_write(Object value, Self record) {
        Object cache_value = convert_to_cache(value, record, false);
        Object record_value = convert_to_record(cache_value, record);
        return convert_to_read(record_value, record);
    }
}
