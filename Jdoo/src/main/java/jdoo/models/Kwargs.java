package jdoo.models;

import java.util.HashMap;
import java.util.Map;

public class Kwargs {
    HashMap<String, Object> values;

    public Kwargs(){
        values = new HashMap<String, Object>();
    }

    public Kwargs(Map<String, Object> args){
        values = new HashMap<String, Object>(args);
    }

    public Kwargs set(String key, Object value) {
        values.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<? extends T> c, String key) {
        if (values.containsKey(key)) {
            return (T) values.get(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defalult_) {
        if (values.containsKey(key)) {
            return (T) values.get(key);
        }
        return defalult_;
    }

    public Object get(String key) {
        return values.get(key);
    }
}
