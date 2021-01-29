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
import jdoo.models.Self;
import jdoo.util.Tuple;
import jdoo.tools.Tuple2;
import jdoo.util.Utils;

public class Cache {
    HashMap<Field, HashMap<String, Object>> _data = new HashMap<Field, HashMap<String, Object>>();

    public boolean contains(Self record, Field field) {
        if (_data.containsKey(field)) {
            HashMap<String, Object> values = _data.get(field);
            if (!field.depends_context().isEmpty()) {
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

    public Object get(Self record, Field field) {
        if (_data.containsKey(field)) {
            HashMap<String, Object> values = _data.get(field);
            if (values.containsKey(record.id())) {
                Object value = values.get(record.id());
                if (!field.depends_context().isEmpty()) {

                    Map<?, ?> map = (Map<?, ?>) value;
                    value = map.get(field.cache_key(record.env()));
                }
                return value;
            }
        }
        throw new CacheMissException("Cache " + record.getMeta().getName() + "." + field.getName() + " not found");
    }

    public Object get(Self record, Field field, Object defalult_) {
        if (_data.containsKey(field)) {
            HashMap<String, Object> values = _data.get(field);
            if (values.containsKey(record.id())) {
                Object value = values.get(record.id());
                if (!field.depends_context().isEmpty()) {

                    Map<?, ?> map = (Map<?, ?>) value;
                    value = map.get(field.cache_key(record.env()));
                }
                return value;
            }
        }
        return defalult_;
    }

    public void set(Self record, Field field, Object value) {
        HashMap<String, Object> values;
        if (_data.containsKey(field)) {
            values = _data.get(field);
        } else {
            values = new HashMap<String, Object>();
            _data.put(field, values);
        }
        if (!field.depends_context().isEmpty()) {
            Object key = field.cache_key(record.env());
            Map<Object, Object> map = new HashMap<>();
            map.put(key, value);
            values.put(record.id(), map);
        } else {
            values.put(record.id(), value);
        }
    }

    @SuppressWarnings("unchecked")
    public void update(Self records, Field field, Collection<Object> values) {
        HashMap<String, Object> field_cache;
        if (_data.containsKey(field)) {
            field_cache = _data.get(field);
        } else {
            field_cache = new HashMap<String, Object>();
            _data.put(field, field_cache);
        }
        if (!field.depends_context().isEmpty()) {
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
                field_cache.put((String) t.get(0), t.get(1));
            }
        }
    }

    public Object remove(Self record, Field field) {
        if (_data.containsKey(field)) {
            HashMap<String, Object> values = _data.get(field);
            return values.remove(record.id());
        }
        return null;
    }

    public Collection<Object> get_values(Self records, Field field) {
        List<Object> values = new ArrayList<>();
        if (_data.containsKey(field)) {
            HashMap<String, Object> field_cache = _data.get(field);
            for (String recrod_id : records.ids()) {
                if (field_cache.containsKey(recrod_id)) {
                    Object value = field_cache.get(recrod_id);
                    if (!field.depends_context().isEmpty()) {

                        Map<?, ?> map = (Map<?, ?>) value;
                        value = map.get(field.cache_key(records.env()));
                    }
                    values.add(value);
                }
            }
        }
        return values;
    }

    public Self get_records_different_from(Self records, Field field, Object value) {
        List<String> ids = new ArrayList<String>();
        HashMap<String, Object> field_cache = _data.get(field);
        for (String record_id : records.ids()) {
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

    @SuppressWarnings("unchecked")
    public Collection<Field> get_fields(Self record) {
        List<Field> fields = new ArrayList<>();
        for (Field field : record.getFields()) {
            if ("id".equals(field.getName())) {
                continue;
            }
            if (!_data.containsKey(field)) {
                continue;
            }
            HashMap<String, Object> field_cache = _data.get(field);
            if (!field_cache.containsKey(record.id())) {
                continue;
            }
            if (!field.depends_context().isEmpty()) {
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

    public Self get_records(Self model, Field field) {
        if (_data.containsKey(field)) {
            HashMap<String, Object> field_cache = _data.get(field);
            return model.browse(field_cache.keySet());
        } else {
            return model.browse();
        }
    }

    public Collection<String> get_missing_ids(Self records, Field field) {
        if (!_data.containsKey(field)) {
            return Collections.emptyList();
        }
        HashMap<String, Object> field_cache = _data.get(field);
        Collection<String> result = new ArrayList<>();
        for (String id : records.ids()) {
            if (!field_cache.containsKey(id)) {
                result.add(id);
            }
        }
        return result;
    }

    public void invalidate() {
        _data.clear();
    }

    public void invalidate(List<Tuple2<Field, Collection<String>>> spec) {
        for (Tuple2<Field, Collection<String>> tuple : spec) {
            if (tuple.T1.isEmpty()) {
                _data.remove(tuple.T0);
            } else {
                HashMap<String, Object> field_cache = _data.get(tuple.T0);
                if (field_cache != null) {
                    for (String id : tuple.T1) {
                        field_cache.remove(id);
                    }
                }
            }
        }
    }
}
