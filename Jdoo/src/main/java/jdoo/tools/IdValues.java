package jdoo.tools;

import java.util.HashMap;
import java.util.Map;

import jdoo.util.DefaultDict;

/**
 * {id, Map{field, value}
 */
public class IdValues extends DefaultDict<Object, Map<String, Object>> {
    private static final long serialVersionUID = 1L;

    public IdValues() {
        super(HashMap::new);
    }

    public boolean contains(Object id, String field) {
        if (containsKey(id)) {
            Map<String, Object> values = get(id);
            return values.containsKey(field);
        }
        return false;
    }

    public Object get(Object id, String field) {
        if (containsKey(id)) {
            Map<String, Object> values = get(id);
            if (values.containsKey(field)) {
                Object value = values.get(field);
                return value;
            }
        }
        return null;
    }

    public void set(Object id, String field, Object value) {
        get(id).put(field, value);
    }
}
