package jdoo.models._fields;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jdoo.apis.Cache;
import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.util.Utils;

/** Abstract class for string fields. */
public abstract class _StringField<T extends _StringField<T>> extends BaseField<T> {
    /** whether the field is translated */
    public static final Slot translate = new Slot("translate");
    static {
        default_slots.put(translate, false);
    }

    @SuppressWarnings("unchecked")
    public T translate(boolean translate) {
        set(_StringField.translate, translate);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T translate(BiConsumer<Consumer<Object>, Object> translate) {
        set(_StringField.translate, translate);
        return (T) this;
    }

    @Override
    public RecordSet write(RecordSet records, Object value) {
        records.env().remove_to_compute(this, records);
        Cache cache = records.env().cache();
        Object cache_value = convert_to_cache(value, records, true);
        records = cache.get_records_different_from(records, this, cache_value);
        if (!records.hasId()) {
            return records;
        }
        cache.update(records, this, Utils.mutli(Arrays.asList(cache_value), records.size()));

        if (!store()) {
            return records;
        }
        // todo
        return records;
    }
}
