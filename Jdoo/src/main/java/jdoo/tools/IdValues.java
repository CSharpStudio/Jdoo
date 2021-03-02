package jdoo.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class IdValues {
    // HashMap<id, HashMap<field, value>>
    HashMap<Object, HashMap<String, Object>> data = new HashMap<Object, HashMap<String, Object>>();

    public Set<Object> keySet() {
        return data.keySet();
    }

    public Collection<HashMap<String, Object>> values() {
        return data.values();
    }

    public boolean contains(Object id, String field) {
        if (data.containsKey(id)) {
            HashMap<String, Object> values = data.get(id);
            return values.containsKey(field);
        }
        return false;
    }

    public HashMap<String, Object> get(Object id) {
        return data.get(id);
    }

    public Object get(Object id, String field) {
        if (data.containsKey(id)) {
            HashMap<String, Object> values = data.get(id);
            if (values.containsKey(field)) {
                Object value = values.get(field);
                return value;
            }
        }
        return null;
    }

    public void set(Object id, String field, Object value) {
        HashMap<String, Object> values;
        if (data.containsKey(id)) {
            values = data.get(id);
        } else {
            values = new HashMap<String, Object>();
            data.put(id, values);
        }
        values.put(field, value);
    }
}
