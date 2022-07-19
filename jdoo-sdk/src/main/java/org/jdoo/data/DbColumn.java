package org.jdoo.data;

/**
 * 数据库字段信息
 * 
 * @author lrz
 */
public class DbColumn {
    String column;
    String type;
    Integer length;
    boolean nullable;

    public DbColumn(String column, String type, Integer length, boolean nullable) {
        this.column = column;
        this.type = type;
        this.length = length;
        this.nullable = nullable;
    }

    public String getColumn() {
        return column;
    }

    public String getType() {
        return type;
    }

    public Integer getLength() {
        return length;
    }

    public boolean getNullable() {
        return nullable;
    }
}
