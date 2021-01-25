package jdoo.apis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.util.ObjectUtils;

import jdoo.exceptions.CacheMissException;
import jdoo.models.Field;
import jdoo.models.Self;
import jdoo.tools.Tuple2;

public class Cache {
    HashMap<Field, HashMap<String, Object>> _data = new HashMap<Field, HashMap<String, Object>>();

    public boolean contains(Self record, Field field) {
        if (_data.containsKey(field)) {
            HashMap<String, Object> values = _data.get(field);
            return values.containsKey(record.id());
        }
        return false;
    }

    public Object remove(Self record, Field field) {
        if (_data.containsKey(field)) {
            HashMap<String, Object> values = _data.get(field);
            return values.remove(record.id());
        }
        return null;
    }

    public Object get(Self record, Field field) {
        if (_data.containsKey(field)) {
            HashMap<String, Object> values = _data.get(field);
            if (values.containsKey(record.id())) {
                Object value = values.get(record.id());
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
        values.put(record.id(), value);
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

    public void invalidate() {
        _data.clear();
    }

    public void invalidate(List<Tuple2<Field, Collection<String>>> spec) {
        for (Tuple2<Field, Collection<String>> tuple : spec) {
            if (tuple.T2.isEmpty()) {
                _data.remove(tuple.T1);
            } else {
                HashMap<String, Object> field_cache = _data.get(tuple.T1);
                if (field_cache != null) {
                    for (String id : tuple.T2) {
                        field_cache.remove(id);
                    }
                }
            }
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
}
