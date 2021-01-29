package jdoo.data;

public class AsIs {
    String sql;

    public AsIs(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return sql;
    }
}
