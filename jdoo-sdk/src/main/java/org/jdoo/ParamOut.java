package org.jdoo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务出参
 * 
 * @author lrz
 */
public class ParamOut {
    List<String> errors = new ArrayList<>();
    Map<String, Object> result = new HashMap<>();
    Map<String, Object> context = new HashMap<>();
    int errorCode = 100;

    public ParamOut() {
        result.put("context", context);
    }

    public ParamOut addError(String error) {
        errors.add(error);
        return this;
    }

    public ParamOut setErrorCode(int code) {
        errorCode = code;
        return this;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public List<String> getErrors() {
        return errors;
    }

    public ParamOut putData(Object data) {        
        result.put("data", data);
        return this;
    }

    public Object getData() {
        return result.get("data");
    }

    public ParamOut putContext(String key, Object value) {
        context.put(key, value);
        return this;
    }

    public ParamOut putContext(Map<String, Object> ctx) {
        context.putAll(ctx);
        return this;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Map<String, Object> getResult() {
        return result;
    }
}
