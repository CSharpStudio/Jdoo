package org.jdoo.data;

import java.util.List;

/**
 * sql 格式化
 * 
 * @author lrz
 */
public class SqlFormat {
    String sql;
    List<Object> params;

    public SqlFormat(String sql, List<Object> params) {
        this.sql = sql;
        this.params = params;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParmas() {
        return params;
    }

    @Override
    public String toString() {
        String result = sql;
        for (Object p : params) {
            if (p instanceof String) {
                result = result.replaceFirst("\\?", String.format("'%s'", p));
            } else {// TODO date and datetime
                result = result.replaceFirst("\\?", String.format("%s", p));
            }
        }
        return result;
    }
}
