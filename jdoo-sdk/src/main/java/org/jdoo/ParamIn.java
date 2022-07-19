package org.jdoo;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdoo.core.Environment;

/**
 * 服务入参
 * 
 * @author lrz
 */
public class ParamIn {
    String model;
    String service;
    Map<String, Object> context;
    Map<String, Object> args;
    Environment env;

    public ParamIn(Environment env, String model, String serivce, Map<String, Object> args) {
        this.env = env;
        this.model = model;
        this.service = serivce;
        this.args = args;
        this.context = new HashMap<>(env.getContext());
    }

    public Environment getEnv() {
        return env;
    }

    public String getModel() {
        return model;
    }

    public String getService() {
        return service;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public Object getArg(String argName) {
        return args.get(argName);
    }

    @SuppressWarnings("unchecked")
    public <T> T getArgs(Class<T> clazz) {
        if (args == null) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Object obj = objectMapper.convertValue(args, clazz);
        return (T) obj;
    }
}
