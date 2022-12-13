package org.jdoo.data.oracle;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.jdoo.data.ColumnType;
import org.jdoo.data.Cursor;
import org.jdoo.data.DbColumn;
import org.jdoo.data.SqlDialect;
import org.jdoo.data.SqlFormat;
import org.jdoo.exceptions.DataException;
import org.jdoo.utils.DateUtils;
import org.jdoo.utils.ObjectUtils;
import org.jdoo.utils.StringUtils;

public class OracleDialect implements SqlDialect {
    /**
     * 最大长度30
     */
    public int getIdentityMaxLength() {
        return 30;
    }

    /**
     * 列名转小写
     */
    public String getColumnLabel(String column) {
        return column.toLowerCase();
    }

    /**
     * 获取数据库值
     */
    public Object getObject(Object obj) {
        if (obj instanceof oracle.jdbc.internal.OracleClob) {
            try {
                return ((oracle.jdbc.internal.OracleClob) obj).stringValue();
            } catch (SQLException e) {
                throw new DataException("读取CLOB字符值失败", e);
            }
        }
        if (obj instanceof oracle.jdbc.internal.OracleBlob) {
            oracle.jdbc.internal.OracleBlob blob = (oracle.jdbc.internal.OracleBlob) obj;
            try {
                return blob.getBytes(1, (int) blob.length());
            } catch (SQLException e) {
                throw new DataException("读取BLOB字符值失败", e);
            }
        }
        if (obj instanceof Timestamp) {
            Timestamp ts = (Timestamp) obj;
            return DateUtils.atZone(ts, TimeZone.getTimeZone("UTC"));
        }
        if (obj instanceof Date) {
            Date date = (Date) obj;
            return DateUtils.atZone(date, TimeZone.getTimeZone("UTC"));
        }
        return obj;
    }

    /**
     * 准备参数
     */
    public Object prepareObject(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj) ? 1 : 0;
        }
        return obj;
    }

    /**
     * 服务器utc时间
     */
    public String getNowUtc() {
        return "sys_extract_utc(systimestamp)";
    }

    @Override
    public void createColumn(Cursor cr, String table, String name, String columnType, String comment, boolean notNull) {
        cr.execute(String.format("ALTER TABLE %s ADD (%s %s %s)", quote(table), quote(name), columnType,
                notNull ? "NOT NULL" : "NULL"));
        if (StringUtils.isNotEmpty(comment)) {
            cr.execute(String.format("COMMENT ON COLUMN %s.%s IS '%s'", quote(table), quote(name),
                    comment.replace("'", "''")));
        }
        schema.debug("Table {} added column {} of type {}", table, name, columnType);
    }

    @Override
    public String getColumnType(ColumnType type) {
        switch (type) {
            case Boolean:
                return "number(1)";
            case VarChar:
                return "varchar2";
            case Text:
                return "clob";
            case Binary:
                return "blob";
            case Integer:
                return "number(9)";
            case Long:
                return "number(18)";
            case Float:
                return "number";
            case Date:
            case DateTime:
                return "date";
            default:
                return null;
        }
    }

    @Override
    public String getColumnType(ColumnType type, Integer length, Integer precision) {
        switch (type) {
            case VarChar:
                if (length != null && length > 0) {
                    return "varchar2(" + Math.min(4000, length * 3) + ")";
                }
                return "varchar2";
            case Float:
                if (length == null) {
                    length = 18;
                }
                String result = "number(" + length;
                if (precision != null) {
                    result += "," + precision;
                }
                return result + ")";
            default:
                return getColumnType(type);
        }
    }

    @Override
    public List<String> existingTables(Cursor cr, List<String> tableNames) {
        String sql = "SELECT lower(table_name) FROM user_tables WHERE lower(table_name) IN %s";
        cr.execute(sql, Arrays.asList(tableNames));
        List<String> result = new ArrayList<>();
        for (Object[] row : cr.fetchAll()) {
            result.add((String) row[0]);
        }
        return result;
    }

    @Override
    public boolean tableExists(Cursor cr, String table) {
        return existingTables(cr, Arrays.asList(table)).size() == 1;
    }

    @Override
    public void createModelTable(Cursor cr, String table, String comment) {
        cr.execute(String.format("CREATE TABLE %s (ID VARCHAR2(13) PRIMARY KEY)", quote(table)));
        if (StringUtils.isNotEmpty(comment)) {
            cr.execute(String.format("COMMENT ON TABLE %s IS '%s'", quote(table), comment.replace("'", "''")));
        }
        schema.debug("Table {}: created", table);
    }

    @Override
    public Map<String, DbColumn> tableColumns(Cursor cr, String table) {
        String sql = "SELECT lower(column_name), data_type, data_length, nullable"
                + " FROM user_tab_columns WHERE lower(table_name)=%s";
        cr.execute(sql, Arrays.asList(table));
        List<Object[]> all = cr.fetchAll();
        Map<String, DbColumn> result = new HashMap<>(all.size());
        for (Object[] row : all) {
            result.put((String) row[0],
                    new DbColumn((String) row[0], (String) row[1], ObjectUtils.toInt(row[2]),
                            "Y".equals((String) row[3])));
        }
        return result;
    }

    @Override
    public String quote(String identify) {
        return String.format("\"%s\"", identify.toUpperCase());
    }

    @Override
    public String getPaging(String sql, Integer limit, Integer offset) {
        String result = sql;

        if (limit != null && limit > 0 || offset != null && offset > 0) {
            Integer total = 0;
            if (limit != null) {
                total += limit;
            }
            if (offset != null) {
                total += offset;
            }
            result = "SELECT t#1.*, ROWNUM RN# \r\nFROM (" + result + ") t#1 \r\nWHERE ROWNUM <= " + total;
        }
        if (offset != null && offset > 0) {
            result = "SELECT * \r\nFROM (" + result + ") t#2 \r\nWHERE t#2.RN# > " + offset;
        }
        return result;
    }

    @Override
    public String cast(String column, ColumnType type) {
        // if (type == ColumnType.Text) {
        // return column + "::text";
        // }
        // TODO other cast
        return column;
    }

    @Override
    public String addUniqueConstraint(Cursor cr, String table, String constraint, String[] fields) {
        try {
            String definition = String.format("unique(%s)",
                    Arrays.stream(fields).map(f -> quote(f)).collect(Collectors.joining(",")));
            String oldDefinition = getConstraintDefinition(cr, constraint);
            if (oldDefinition != null) {
                if (!definition.equals(oldDefinition)) {
                    dropConstraint(cr, table, constraint);
                } else {
                    return null;
                }
            }
            String sql = String.format("ALTER TABLE %s ADD CONSTRAINT %s %s", quote(table), quote(constraint),
                    definition.replace("'", "''"));
            // TODO cr.savepoint();
            cr.execute(sql);
            schema.debug("Table {} add unique constaint {} as {}", table, constraint, definition);
            return definition;
        } catch (Exception exc) {
            schema.warn("表{}添加唯一约束{}失败:{}", table, constraint, exc.getMessage());
            return null;
        }
    }

    String getConstraintDefinition(Cursor cr, String constraint) {
        String sql = "SELECT definition FROM ir_model_constraint where name=%s";
        cr.execute(sql, Arrays.asList(constraint));
        Object[] row = cr.fetchOne();
        return row.length > 0 ? (String) row[0] : null;
    }

    @Override
    public RuntimeException getError(SQLException err, SqlFormat sql) {
        return new DataException(String.format("执行SQL[%s]失败", sql.getSql()), err);
    }

    @Override
    public void setNotNull(Cursor cr, String table, String column, String columnType) {
        String sql = String.format("ALTER TABLE %s MODIFY (%s NOT NULL)", quote(table), quote(column));
        cr.execute(sql);
        schema.debug("Table {}: column {}: added constraint NOT NULL", table, column);
    }

    @Override
    public void dropNotNull(Cursor cr, String table, String column, String columnType) {
        String sql = String.format("ALTER TABLE %s MODIFY (%s NULL)", quote(table), quote(column));
        cr.execute(sql);
        schema.debug("Table {}: column {}: dropped constraint NOT NULL", table, column);
    }

    @Override
    public void createM2MTable(Cursor cr, String table, String column1, String column2, String comment) {
        table = quote(table);
        column1 = quote(column1);
        column2 = quote(column2);
        String sql = "CREATE TABLE " + table + " (" + column1 + " VARCHAR2(13) NOT NULL, " + column2
                + " VARCHAR2(13) NOT NULL, PRIMARY KEY(" + column1 + "," + column2 + "))";
        cr.execute(sql);
        if (StringUtils.isNotEmpty(comment)) {
            cr.execute(String.format("COMMENT ON TABLE %s IS '%s'", table, comment.replace("'", "''")));
        }
        schema.debug("Create table %s: %s", table, comment);
    }

    @Override
    public List<Object[]> getForeignKeys(Cursor cr, Collection<String> tables) {
        String sql = "SELECT lower(c.constraint_name), lower(c.table_name), lower(lc.column_name), lower(rc.table_name) r_table_name, lower(rc.column_name) r_column_name, c.delete_rule"
                + " FROM user_constraints c"
                + "  LEFT JOIN user_cons_columns lc ON lc.constraint_name=c.constraint_name"
                + "  LEFT JOIN user_cons_columns rc ON rc.constraint_name=c.r_constraint_name"
                + " WHERE c.constraint_type = 'R' AND lower(c.table_name) in %s AND c.owner=user";
        cr.execute(sql, Arrays.asList(tables));
        return cr.fetchAll();
    }

    @Override
    public String addForeignKey(Cursor cr, String table1, String column1, String table2, String column2,
            String ondelete) {
        String fk = limitIdentity(String.format("fk_%s_%s", table1, column1));
        try {
            String sql = String.format("ALTER TABLE %s"
                    + " ADD CONSTRAINT %s"
                    + " FOREIGN KEY (%s)"
                    + " REFERENCES %s (%s)",
                    cr.quote(table1), cr.quote(fk), cr.quote(column1), cr.quote(table2), cr.quote(column2));
            if (!"RESTRICT".equals(ondelete)) {
                sql += " ON DELETE " + ondelete;
            }
            cr.execute(sql);
            return fk;
        } catch (Exception exc) {
            schema.warn("表{}添加外键约束{}失败:{}", table1, fk, exc.getMessage());
            return null;
        }
    }

    @Override
    public void dropConstraint(Cursor cr, String table, String constraint) {
        try {
            // TODO savepoint
            cr.execute(String.format("ALTER TABLE %s DROP CONSTRAINT %s", quote(table), quote(constraint)));
            schema.debug("Table {}: dropped constraint {}", table, constraint);
        } catch (Exception e) {
            schema.warn("Table {}: unable to drop constraint {}", table, constraint);
        }
    }
}
