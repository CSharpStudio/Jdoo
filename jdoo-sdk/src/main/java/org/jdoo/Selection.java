package org.jdoo;

import java.util.HashMap;
import java.util.Map;

/**
 * 生成SelectionField的选项值
 * 
 * @author lrz
 */
public interface Selection {
    /**
     * 获取选项
     * 
     * @param rec
     * @return
     */
    Map<String, String> get(Records rec);

    /**
     * 扩展添加的选项
     * 
     * @param toAdd
     */
    void add(Map<String, String> toAdd);

    /**
     * 方法返回选项值
     * 
     * @param method
     * @return
     */
    public static Selection method(String method) {
        return new MethodSelection(method);
    }

    /**
     * 常量选项值
     * 
     * @param value
     * @return
     */
    public static Selection value(Map<String, String> value) {
        return new StaticSelection(value);
    }
}

class MethodSelection implements Selection {
    String method;
    Map<String, String> toAdd = new HashMap<>(0);

    public MethodSelection(String method) {
        this.method = method;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> get(Records rec) {
        Map<String, String> result = (Map<String, String>) rec.call(method);
        result.putAll(toAdd);
        return result;
    }

    @Override
    public void add(Map<String, String> toAdd) {
        this.toAdd.putAll(toAdd);
    }
}

class StaticSelection implements Selection {
    Map<String, String> selection;
    Map<String, String> toAdd = new HashMap<>(0);

    public StaticSelection(Map<String, String> selection) {
        this.selection = selection;
    }

    @Override
    public Map<String, String> get(Records rec) {
        Map<String, String> result = new HashMap<>(selection);
        result.putAll(toAdd);
        return result;
    }

    @Override
    public void add(Map<String, String> toAdd) {
        this.toAdd.putAll(toAdd);
    }
}