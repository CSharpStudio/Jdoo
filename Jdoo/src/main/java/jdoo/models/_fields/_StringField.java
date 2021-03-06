package jdoo.models._fields;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jdoo.apis.Cache;
import jdoo.models.RecordSet;
import jdoo.tools.IdValues;
import jdoo.tools.Slot;
import jdoo.util.Utils;

/** Abstract class for string fields. */
public abstract class _StringField<T extends _StringField<T>> extends BaseField<T> {
    /** whether the field is translated */
    public static final Slot translate = new Slot("translate");
    static {
        default_slots().put(translate, false);
    }

    @Override
    public boolean _translate() {
        return getattr(Boolean.class, _StringField.translate);
    }

    public T translate(boolean translate) {
        setattr(_StringField.translate, translate);
        return (T) this;
    }

    public T translate(BiConsumer<Consumer<Object>, Object> translate) {
        setattr(_StringField.translate, translate);
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

        if (!_store()) {
            return records;
        }

        RecordSet real_recs = records.filtered("id");
        if (!real_recs.hasId()) {
            return records;
        }
        boolean update_column = true;
        boolean update_trans = false;
        if (_translate()) {
            // todo
        }

        if (update_column) {
            IdValues towrite = records.env().all().towrite(model_name());
            for (Object rid : real_recs.ids()) {
                towrite.set(rid, getName(), cache_value);
            }
            // todo
        }

        if (update_trans) {

        }
        // todo
        return records;
    }
}
