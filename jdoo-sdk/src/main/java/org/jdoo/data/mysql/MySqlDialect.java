package org.jdoo.data.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdoo.data.ColumnType;
import org.jdoo.data.Cursor;
import org.jdoo.data.DbColumn;
import org.jdoo.data.SqlDialect;
import org.jdoo.utils.StringUtils;

/**
 * @author lrz
 */
public class MySqlDialect implements SqlDialect {

    @Override
    public String getNowUtc() {
        return "UTC_TIMESTAMP()";
    }

    @Override
    public void createColumn(Cursor cr, String table, String name, String columnType, String comment, boolean notNull) {
        String sql = String.format("ALTER TABLE `%s` ADD COLUMN `%s` %s %s", table, name, columnType,
                notNull ? "NOT NULL" : "NULL");
        if (StringUtils.isNotEmpty(comment)) {
            sql += String.format(" COMMENT '%s'", comment.replace("'", "''"));
        }
        cr.execute(sql);
        schema.debug("Table {} added column {} of type {} {}", table, name, columnType, notNull ? "NOT NULL" : "NULL");
    }

    @Override
    public String getColumnType(ColumnType type) {
        switch (type) {
            case Boolean:
                return "bit(1)";
            case VarChar:
                return "varchar";
            case Text:
                return "text";
            case Binary:
                return "blob(60000)";
            case Integer:
                return "int";
            case Long:
                return "bigint";
            case Float:
                return "double";
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
    public boolean tableExists(Cursor cr, String table) {
        return existingTables(cr, Arrays.asList(table)).size() == 1;
    }

    @Override
    public List<String> existingTables(Cursor cr, List<String> tableNames) {
        String sql = "select TABLE_NAME, TABLE_TYPE from information_schema.tables"
                + " where TABLE_TYPE='BASE TABLE' AND TABLE_SCHEMA=(select database()) AND TABLE_NAME in (%s)";
        cr.execute(sql, Arrays.asList(tableNames));
        List<String> result = new ArrayList<>();
        for (Object[] row : cr.fetchAll()) {
            result.add((String) row[0]);
        }
        return result;
    }

    @Override
    public void createModelTable(Cursor cr, String table, String comment) {
        String sql = String.format("CREATE TABLE `%s` (`id` VARCHAR(13) NOT NULL, PRIMARY KEY(`id`))", table);
        if (StringUtils.isNotEmpty(comment)) {
            sql += String.format("COMMENT = '%s'", comment.replace("'", "''"));
        }
        cr.execute(sql);
        schema.debug("Table {}: created", table);
    }

    @Override
    public Map<String, DbColumn> tableColumns(Cursor cr, String table) {
        String sql = "SELECT column_name, CASE WHEN  left(COLUMN_TYPE,LOCATE('(',COLUMN_TYPE)-1)='' THEN COLUMN_TYPE ELSE left(COLUMN_TYPE,LOCATE('(',COLUMN_TYPE)-1) END AS COLUMN_TYPE, CAST(SUBSTRING(COLUMN_TYPE,LOCATE('(',COLUMN_TYPE)+1,LOCATE(')',COLUMN_TYPE)-LOCATE('(',COLUMN_TYPE)-1) AS signed) AS LENGTH, IS_NULLABLE"
                + " FROM Information_schema.columns where TABLE_SCHEMA=(select database()) and TABLE_NAME = %s";
        cr.execute(sql, Arrays.asList(table));
        List<Object[]> all = cr.fetchAll();
        Map<String, DbColumn> result = new HashMap<>(all.size());
        for (Object[] row : all) {
            result.put((String) row[0],
                    new DbColumn((String) row[0], (String) row[1], Integer.valueOf(row[2].toString()),
                            "YES".equals((String) row[3])));
        }
        return result;
    }

    @Override
    public String quote(String identify) {
        return String.format("`%s`", identify);
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
        return column;
    }

    @Override
    public void addUniqueConstraint(Cursor cr, String table, String constraint, String[] fields) {
        String definition = String.format("unique(%s)", StringUtils.join(fields, ','));
        String oldDefinition = getConstraintDefinition(cr, table, constraint);
        if (oldDefinition != null) {
            if (!definition.equals(oldDefinition)) {
                dropConstraint(cr, table, constraint);
            } else {
                return;
            }
        }
        String sql = String.format("ALTER TABLE %s ADD CONSTRAINT %s %s COMMENT '%s'", quote(table), quote(constraint),
                definition, definition);
        // TODO cr.savepoint();
        cr.execute(sql);
        schema.debug("Table {} add unique constaint {} as {}", table, constraint, definition);
    }

    void dropConstraint(Cursor cr, String table, String constraint) {
        try {
            // TODO savepoint
            cr.execute("ALTER TABLE %s DROP CONSTRAINT %s", Arrays.asList(quote(table), quote(constraint)));
            schema.debug("Table {}: dropped constraint {}", table, constraint);

        } catch (Exception e) {
            schema.warn("Table {}: unable to drop constraint {}", table, constraint);
        }
    }

    String getConstraintDefinition(Cursor cr, String table, String constraint) {
        String sql = "SELECT INDEX_COMMENT FROM INFORMATION_SCHEMA.STATISTICS s where TABLE_SCHEMA=(select database()) and TABLE_NAME=%s and INDEX_NAME=%s";
        cr.execute(sql, Arrays.asList(table, constraint));
        Object[] row = cr.fetchOne();
        return row.length > 0 ? (String) row[0] : null;
    }

    @Override
    public String getConstraint(SQLException cause) {
        // java.sql.SQLIntegrityConstraintViolationException
        return null;
    }

    @Override
    public void setNotNull(Cursor cr, String table, String column, String columnType) {
        String sql = String.format("ALTER TABLE %s MODIFY %s %s NOT NULL", quote(table), quote(column), columnType);
        // TODO savepoint
        cr.execute(sql);
        schema.debug("Table {}: column {}: added constraint NOT NULL", table, column);
    }

    @Override
    public void dropNotNull(Cursor cr, String table, String column, String columnType) {
        String sql = String.format("ALTER TABLE %s MODIFY %s %s NULL", quote(table), quote(column), columnType);
        // TODO savepoint
        cr.execute(sql);
        schema.debug("Table {}: column {}: dropped constraint NOT NULL", table, column);
    }

    @Override
    public void createM2MTable(Cursor cr, String table, String column1, String column2, String comment) {
        table = quote(table);
        column1 = quote(column1);
        column2 = quote(column2);
        String sql = "CREATE TABLE " + table + " (" + column1 + " VARCHAR(13) NOT NULL, " + column2
                + " VARCHAR(13) NOT NULL, PRIMARY KEY(" + column1 + "," + column2 + "))"
                + " COMMENT = '" + comment + "'";
        cr.execute(sql);
        schema.debug("Create table %s: %s", table, comment);
    }
}
