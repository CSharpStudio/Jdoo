package org.jdoo.data.postgresql;

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

import org.apache.commons.lang3.StringUtils;

/**
 * postgre sql 方言
 * 
 * @author lrz
 */
public class PgSqlDialect implements SqlDialect {

    public String getNowUtc() {
        return "(now() at time zone 'UTC')";
    }

    /**
     * 获取数据库值
     */
    public Object getObject(Object obj) {
        if(obj instanceof Timestamp){
            Timestamp ts = (Timestamp)obj;
            return DateUtils.atZone(ts, TimeZone.getTimeZone("UTC"));
        }        
        if(obj instanceof Date){
            Date date = (Date)obj;
            return DateUtils.atZone(date, TimeZone.getTimeZone("UTC"));
        }
        return obj;
    }

    @Override
    public void createColumn(Cursor cr, String table, String name, String columnType, String comment, boolean notNull) {
        String colDefault = columnType.toUpperCase() == "BOOLEAN" ? "DEFAULT false" : "";
        cr.execute(
                String.format("ALTER TABLE %s ADD COLUMN %s %s %s", quote(table), quote(name), columnType, colDefault));
        if (StringUtils.isNotEmpty(comment)) {
            cr.execute(
                    String.format("COMMENT ON COLUMN %s.%s IS '%s'", quote(table), quote(name),
                            comment.replace("'", "''")));
        }
        schema.debug("Table {} added column {} of type {}", table, name, columnType);
    }

    @Override
    public String getColumnType(ColumnType type) {
        switch (type) {
            case Boolean:
                return "bool";
            case VarChar:
                return "varchar";
            case Text:
                return "text";
            case Binary:
                return "bytea";
            case Integer:
                return "int4";
            case Long:
                return "int8";
            case Float:
                return "float8";
            case Date:
                return "date";
            case DateTime:
                return "timestamp";
            default:
                return null;
        }
    }

    @Override
    public String getColumnType(ColumnType type, Integer length, Integer precision) {
        String result = getColumnType(type);
        if (length != null && length > 0) {
            result += "(" + length + ")";
        }
        return result;
    }

    @Override
    public List<String> existingTables(Cursor cr, List<String> tableNames) {
        String sql = "SELECT c.relname FROM pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)"
                + " WHERE c.relname IN %s AND c.relkind IN ('r', 'v', 'm') AND n.nspname = current_schema";
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
        cr.execute(String.format("CREATE TABLE %s (\"id\" VARCHAR(13), PRIMARY KEY(\"id\"))", quote(table)));
        if (StringUtils.isNotEmpty(comment)) {
            cr.execute(String.format("COMMENT ON TABLE %s IS '%s'", quote(table), comment.replace("'", "''")));
        }
        schema.debug("Table {}: created", table);
    }

    @Override
    public Map<String, DbColumn> tableColumns(Cursor cr, String table) {
        String sql = "SELECT column_name, udt_name, character_maximum_length, is_nullable"
                + " FROM information_schema.columns WHERE table_name=%s";
        cr.execute(sql, Arrays.asList(table));
        List<Object[]> all = cr.fetchAll();
        Map<String, DbColumn> result = new HashMap<>(all.size());
        for (Object[] row : all) {
            result.put((String) row[0],
                    new DbColumn((String) row[0], (String) row[1], (Integer) row[2], "YES".equals((String) row[3])));
        }
        return result;
    }

    @Override
    public String quote(String identify) {
        return String.format("\"%s\"", identify);
    }

    @Override
    public String getPaging(String sql, Integer limit, Integer offset) {
        if (limit != null && limit > 0) {
            sql += " LIMIT " + limit;
        }
        if (offset != null && offset > 0) {
            sql += " OFFSET " + offset;
        }
        return sql;
    }

    @Override
    public String cast(String column, ColumnType type) {
        if (type == ColumnType.Text) {
            return column + "::text";
        }
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

    final static String CONSTRAINT_ERROR_STATE = "23505";
    final static String NOT_NULL_ERROR_STATE = "23502";

    @Override
    public RuntimeException getError(SQLException err, SqlFormat sql) {

        return new DataException(String.format("执行SQL[%s]失败", sql.getSql()), err);
    }

    // @Override
    // public String getConstraint(SQLException cause) {
    // if (CONSTRAINT_ERROR_STATE.equals(cause.getSQLState()) && cause instanceof
    // PSQLException) {
    // PSQLException p = (PSQLException) cause;
    // return p.getServerErrorMessage().getConstraint();
    // }
    // return null;
    // }

    // public String getNullConstraint(SQLException cause) {
    // if (NOT_NULL_ERROR_STATE.equals(cause.getSQLState()) && cause instanceof
    // PSQLException) {
    // PSQLException p = (PSQLException) cause;
    // return p.getServerErrorMessage().getColumn();
    // }
    // return null;
    // }

    @Override
    public void setNotNull(Cursor cr, String table, String column, String columnType) {
        String sql = String.format("ALTER TABLE %s ALTER COLUMN %s SET NOT NULL", quote(table), quote(column));
        // TODO savepoint
        cr.execute(sql);
        schema.debug("Table {}: column {}: added constraint NOT NULL", table, column);
    }

    @Override
    public void dropNotNull(Cursor cr, String table, String column, String columnType) {
        String sql = String.format("ALTER TABLE %s ALTER COLUMN %s DROP NOT NULL", quote(table), quote(column));
        cr.execute(sql);
        schema.debug("Table {}: column {}: dropped constraint NOT NULL", table, column);
    }

    @Override
    public void createM2MTable(Cursor cr, String table, String column1, String column2, String comment) {
        table = quote(table);
        column1 = quote(column1);
        column2 = quote(column2);
        if (comment == null) {
            comment = "";
        }
        String sql = "CREATE TABLE " + table + " (" + column1 + " VARCHAR(13) NOT NULL, " + column2
                + " VARCHAR(13) NOT NULL, PRIMARY KEY(" + column1 + "," + column2 + "))";
        cr.execute(sql);
        if (StringUtils.isNotEmpty(comment)) {
            cr.execute(String.format("COMMENT ON TABLE %s IS '%s'", table, comment.replace("'", "''")));
        }
        // + " COMMENT ON TABLE " + table + " IS '" + comment.replace("'", "''") + "'"
        // + " CREATE INDEX ON " + table + "(" + column1 + "," + column2 + ")";
        // cr.execute(sql);
        schema.debug("Create table %s: %s", table, comment);
    }

    @Override
    public List<Object[]> getForeignKeys(Cursor cr, Collection<String> tables) {
        String sql = "SELECT fk.conname, c1.relname, a1.attname, c2.relname, a2.attname, fk.confdeltype"
                + " FROM pg_constraint AS fk"
                + " JOIN pg_class AS c1 ON fk.conrelid = c1.oid"
                + " JOIN pg_class AS c2 ON fk.confrelid = c2.oid"
                + " JOIN pg_attribute AS a1 ON a1.attrelid = c1.oid AND fk.conkey[1] = a1.attnum"
                + " JOIN pg_attribute AS a2 ON a2.attrelid = c2.oid AND fk.confkey[1] = a2.attnum"
                + " WHERE fk.contype = 'f' AND c1.relname IN %s";
        cr.execute(sql, Arrays.asList(tables));
        return cr.fetchAll();
    }

    @Override
    public String addForeignKey(Cursor cr, String table1, String column1, String table2, String column2,
            String ondelete) {
        String fk = limitIdentity(String.format("fk_%s_%s", table1, column1));
        String sql = String.format("ALTER TABLE %s"
                + " ADD CONSTRAINT %s"
                + " FOREIGN KEY (%s)"
                + " REFERENCES %s (%s)"
                + " ON DELETE %s",
                cr.quote(table1), cr.quote(fk), cr.quote(column1), cr.quote(table2), cr.quote(column2), ondelete);
        cr.execute(sql);
        return fk;
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
