package jdoo.apis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.util.ObjectUtils;

import jdoo.models.Field;
import jdoo.models.Self;
import jdoo.tools.Tuple2;

public class Cache {
    HashMap<Field, HashMap<String, Object>> data = new HashMap<Field, HashMap<String, Object>>();

    public boolean contains(Self record, Field field) {
        if (data.containsKey(field)) {
            HashMap<String, Object> values = data.get(field);
            return values.containsKey(record.id());
        }
        return false;
    }

    public Object remove(Self record, Field field) {
        if (data.containsKey(field)) {
            HashMap<String, Object> values = data.get(field);
            return values.remove(record.id());
        }
        return null;
    }

    public Object get(Self record, Field field) {
        if (data.containsKey(field)) {
            HashMap<String, Object> values = data.get(field);
            if (values.containsKey(record.id())) {
                Object value = values.get(record.id());
                return value;
            }
        }
        throw new CacheMiss("Cache " + record.getMeta().getName() + "." + field.getName() + " not found");
    }

    public Object get(Self record, Field field, Object defalult_) {
        if (data.containsKey(field)) {
            HashMap<String, Object> values = data.get(field);
            if (values.containsKey(record.id())) {
                Object value = values.get(record.id());
                return value;
            }
        }
        return defalult_;
    }

    public void set(Self record, Field field, Object value) {
        HashMap<String, Object> values;
        if (data.containsKey(field)) {
            values = data.get(field);
        } else {
            values = new HashMap<String, Object>();
            data.put(field, values);
        }
        values.put(record.id(), value);
    }

    public Self get_records_different_from(Self records, Field field, Object value) {
        List<String> ids = new ArrayList<String>();
        HashMap<String, Object> field_cache = data.get(field);
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
        data.clear();
    }

    public void invalidate(List<Tuple2<Field, List<String>>> spec) {
        for (Tuple2<Field, List<String>> tuple : spec) {
            if (tuple.T2.isEmpty()) {
                data.remove(tuple.T1);
            } else {
                HashMap<String, Object> field_cache = data.get(tuple.T1);
                if (field_cache != null) {
                    for (String id : tuple.T2) {
                        field_cache.remove(id);
                    }
                }
            }
        }
    }
}
