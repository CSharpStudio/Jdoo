package jdoo.apis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.ObjectUtils;

import jdoo.exceptions.CacheMissException;
import jdoo.models.Field;
import jdoo.models.RecordSet;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.util.Utils;

public class Cache {
    //HashMap<field, HashMap<id, value>>
    HashMap<Field, Map<Object, Object>> _data = new HashMap<>();

    public boolean contains(RecordSet record, Field field) {
        if (_data.containsKey(field)) {
            Map<?, ?> values = _data.get(field);
            if (!field._depends_context().isEmpty()) {
                if (values.containsKey(record.id())) {
                    Object key = field.cache_key(record.env());
                    Map<?, ?> map = (Map<?, ?>) values.get(record.id());
                    return map.containsKey(key);
                }
                return false;
            }
            return values.containsKey(record.id());
        }
        return false;
    }

    public Object get(RecordSet record, Field field) {
        if (_data.containsKey(field)) {
            Map<?, ?> values = _data.get(field);
            if (values.containsKey(record.id())) {
                Object value = values.get(record.id());
                if (!field._depends_context().isEmpty()) {

                    Map<?, ?> map = (Map<?, ?>) value;
                    value = map.get(field.cache_key(record.env()));
                }
                return value;
            }
        }
        throw new CacheMissException("Cache " + record.type().name() + "." + field.getName() + " not found");
    }

    public Object get(RecordSet record, Field field, Object defalult_) {
        if (_data.containsKey(field)) {
            Map<?, ?> values = _data.get(field);
            if (values.containsKey(record.id())) {
                Object value = values.get(record.id());
                if (!field._depends_context().isEmpty()) {

                    Map<?, ?> map = (Map<?, ?>) value;
                    value = map.get(field.cache_key(record.env()));
                }
                return value;
            }
        }
        return defalult_;
    }

    public void set(RecordSet record, Field field, Object value) {
        Map<Object, Object> values;
        if (_data.containsKey(field)) {
            values = _data.get(field);
        } else {
            values = new HashMap<>();
            _data.put(field, values);
        }
        if (!field._depends_context().isEmpty()) {
            Object key = field.cache_key(record.env());
            Map<? super Object, ? super Object> map = new HashMap<>();
            map.put(key, value);
            values.put(record.id(), map);
        } else {
            values.put(record.id(), value);
        }
    }

    public void update(RecordSet records, Field field, Collection<Object> values) {
        Map<Object, Object> field_cache;
        if (_data.containsKey(field)) {
            field_cache = _data.get(field);
        } else {
            field_cache = new HashMap<>();
            _data.put(field, field_cache);
        }
        if (!field._depends_context().isEmpty()) {
            Object key = field.cache_key(records.env());
            for (Tuple<Object> t : Utils.zip(records.ids(), values)) {
                String record_id = (String) t.get(0);
                Object value = t.get(1);
                if (field_cache.containsKey(record_id)) {
                    Map<Object, Object> map = (Map<Object, Object>) field_cache.get(record_id);
                    map.put(key, value);
                } else {
                    Map<Object, Object> map = new HashMap<>();
                    map.put(key, value);
                    field_cache.put(record_id, map);
                }
            }
        } else {
            for (Tuple<Object> t : Utils.zip(records.ids(), values)) {
                field_cache.put(t.get(0), t.get(1));
            }
        }
    }

    public Object remove(RecordSet record, Field field) {
        if (_data.containsKey(field)) {
            Map<Object, Object> values = _data.get(field);
            return values.remove(record.id());
        }
        return null;
    }

    public Collection<Object> get_values(RecordSet records, Field field) {
        List<Object> values = new ArrayList<>();
        if (_data.containsKey(field)) {
            Map<Object, Object> field_cache = _data.get(field);
            for (Object recrod_id : records.ids()) {
                if (field_cache.containsKey(recrod_id)) {
                    Object value = field_cache.get(recrod_id);
                    if (!field._depends_context().isEmpty()) {

                        Map<?, ?> map = (Map<?, ?>) value;
                        value = map.get(field.cache_key(records.env()));
                    }
                    values.add(value);
                }
            }
        }
        return values;
    }

    public RecordSet get_records_different_from(RecordSet records, Field field, Object value) {
        List<Object> ids = new ArrayList<>();
        Map<Object, Object> field_cache = _data.get(field);
        for (Object record_id : records.ids()) {
            if (field_cache != null && field_cache.containsKey(record_id)) {
                Object val = field_cache.get(record_id);
                if (!ObjectUtils.nullSafeEquals(val, value)) {
                    ids.add(record_id);
                }
            } else {
                ids.add(record_id);
            }
        }
        return records.browse(ids);
    }

    public Collection<Field> get_fields(RecordSet record) {
        List<Field> fields = new ArrayList<>();
        for (Field field : record.getFields()) {
            if ("id".equals(field.getName())) {
                continue;
            }
            if (!_data.containsKey(field)) {
                continue;
            }
            Map<Object, Object> field_cache = _data.get(field);
            if (!field_cache.containsKey(record.id())) {
                continue;
            }
            if (!field._depends_context().isEmpty()) {
                Object key = field.cache_key(record.env());
                Map<Object, Object> map = (Map<Object, Object>) field_cache.get(record.id());
                if (!map.containsKey(key)) {
                    continue;
                }
            }
            fields.add(field);
        }
        return fields;
    }

    public RecordSet get_records(RecordSet model, Field field) {
        if (_data.containsKey(field)) {
            Map<Object, Object> field_cache = _data.get(field);
            return model.browse(field_cache.keySet());
        } else {
            return model.browse();
        }
    }

    public Collection<Object> get_missing_ids(RecordSet records, Field field) {
        if (!_data.containsKey(field)) {
            return Collections.emptyList();
        }
        Map<Object, Object> field_cache = _data.get(field);
        Collection<Object> result = new ArrayList<>();
        for (Object id : records.ids()) {
            if (!field_cache.containsKey(id)) {
                result.add(id);
            }
        }
        return result;
    }

    public void invalidate() {
        _data.clear();
    }

    public void invalidate(List<Pair<Field, Collection<?>>> spec) {
        for (Pair<Field, Collection<?>> pair : spec) {
            if (pair.second().isEmpty()) {
                _data.remove(pair.first());
            } else {
                Map<Object, Object> field_cache = _data.get(pair.first());
                if (field_cache != null) {
                    for (Object id : pair.second()) {
                        field_cache.remove(id);
                    }
                }
            }
        }
    }
}
