package org.jdoo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * 待更新数据
 * 
 * @author lrz
 */
public class ToUpdate {
    /** updates {model: {id: {field: value}}} */
    Map<String, IdValues> data = new HashMap<>();

    public Set<String> getModels() {
        return data.keySet();
    }

    public IdValues get(String model) {
        IdValues result = data.get(model);
        if (result == null) {
            result = new IdValues();
            data.put(model, result);
        }
        return result;
    }

    public Set<Entry<String, IdValues>> entrySet() {
        return data.entrySet();
    }

    public void clear() {
        data.clear();
    }

    public IdValues remove(String model) {
        IdValues result = data.remove(model);
        if (result == null) {
            result = new IdValues();
        }
        return result;
    }

    public class IdValues {
        Map<String, Map<String, Object>> values = new HashMap<>();

        public Set<String> getIds() {
            return values.keySet();
        }

        public int size() {
            return values.size();
        }

        public Map<String, Object> get(String id) {
            Map<String, Object> result = values.get(id);
            if (result == null) {
                result = new HashMap<>(16);
                values.put(id, result);
            }
            return result;
        }

        public Set<Entry<String, Map<String, Object>>> entrySet() {
            return values.entrySet();
        }
    }
}
