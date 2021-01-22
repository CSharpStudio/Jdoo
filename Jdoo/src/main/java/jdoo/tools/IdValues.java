package jdoo.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class IdValues {
    HashMap<String, HashMap<String, Object>> data = new HashMap<String, HashMap<String, Object>>();

    public Set<String> ids(){
        return data.keySet();
    }

    public Collection<HashMap<String, Object>> values(){
        return data.values();
    }

    public boolean contains(String id, String field) {
        if (data.containsKey(id)) {
            HashMap<String, Object> values = data.get(id);
            return values.containsKey(field);
        }
        return false;
    }

    public HashMap<String, Object> get(String id){
        return data.get(id);
    }

    public Object get(String id, String field) {
        if (data.containsKey(id)) {
            HashMap<String, Object> values = data.get(id);
            if (values.containsKey(field)) {
                Object value = values.get(field);
                return value;
            }
        }
        return null;
    }

    public void set(String id, String field, Object value) {
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
