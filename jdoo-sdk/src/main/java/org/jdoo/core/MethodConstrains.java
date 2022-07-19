package org.jdoo.core;

/**
 * 方法约束
 * 
 * @author lrz
 */
public class MethodConstrains {
    String method;
    String[] fields;

    public String getMethod() {
        return method;
    }

    public String[] getFields() {
        return fields;
    }

    public MethodConstrains(String method, String[] fields) {
        this.method = method;
        this.fields = fields;
    }
}
