package org.jdoo;

/**
 * 默认值
 * 
 * @author lrz
 */
public interface Search {
    /**
     * 调用返回默认值
     * 
     * @param rec
     * @return
     */
    Criteria call(Records rec, String op, Object value);

    /**
     * 方法返回默认值
     * 
     * @param method
     * @return
     */
    public static Search method(String method) {
        return new MethodSearch(method);
    }
}

class MethodSearch implements Search {
    String method;

    public MethodSearch(String method) {
        this.method = method;
    }

    @Override
    public Criteria call(Records rec, String op, Object value) {
        return (Criteria) rec.call(method, op, value);
    }
}