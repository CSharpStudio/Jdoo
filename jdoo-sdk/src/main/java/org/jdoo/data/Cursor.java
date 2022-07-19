package org.jdoo.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jdoo.exceptions.DataException;
import org.jdoo.util.KvMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 游标
 * 
 * @author lrz
 */
public class Cursor implements AutoCloseable {
    private static Logger logger = LogManager.getLogger(Cursor.class);
    /** decent limit on size of IN queries - guideline = Oracle limit */
    static int IN_MAX = 1000;
    Connection connection;
    PreparedStatement statement;
    ResultSet resultSet;
    SqlDialect sqlDialect;
    CursorState state;

    /** 游标状态 */
    public enum CursorState {
        /** 未执行execute，此时调用fetch*()将抛异常 */
        Unexecuted,
        /** 已执行execute但没有返回数据 */
        ExecuteNonQuery,
        /** 数据可读，此时可执行fetch*() */
        Fetchable,
        /** 数据已读完 */
        EndOfFetch,
    }

    int rowcount;

    protected Cursor(SqlDialect dialect) {
        sqlDialect = dialect;
    }

    public Cursor(Connection conn, SqlDialect dialect) {
        connection = conn;
        sqlDialect = dialect;
        setAutoCommit(false);
    }

    public SqlDialect getSqlDialect() {
        return sqlDialect;
    }

    public void setAutoCommit(boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataException("事务提交失败", e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataException("事务回滚失败", e);
        }
    }

    public SqlFormat mogrify(String sql, Collection<?> params) {
        List<Object> args = new ArrayList<>();
        for (Object p : params) {
            if (p instanceof Collection<?>) {
                Collection<?> col = (Collection<?>) p;
                String v = col.stream().map(c -> "?").collect(Collectors.joining(","));
                sql = sql.replaceFirst("%s", "(" + v + ")");
                args.addAll(col);
            } else {
                sql = sql.replaceFirst("%s", "?");
                args.add(p);
            }
        }
        return new SqlFormat(sql, args);
    }

    public void close() {
        reset();
        try {
            connection.rollback();
        } catch (SQLException e) {
            logger.warn("数据库事务回滚失败", e);
        }
        try {
            connection.close();
        } catch (SQLException e) {
            logger.warn("数据库连接关闭失败", e);
        }
    }

    public void execute(String sql) {
        execute(sql, Collections.emptyList(), false);
    }

    public void execute(String sql, Collection<?> params) {
        execute(sql, params, false);
    }

    public boolean execute(String sql, Collection<?> params, boolean logExceptions) {
        reset();
        SqlFormat format = mogrify(sql, params);
        try {
            statement = connection.prepareStatement(format.getSql());
            int parameterIndex = 1;
            for (Object p : format.getParmas()) {
                statement.setObject(parameterIndex++, p);
            }
            System.out.println(format.toString());
            boolean res = statement.execute();
            if (res) {
                resultSet = statement.getResultSet();
                rowcount = resultSet.getRow();
                scroll();
            } else {
                int count = statement.getUpdateCount();
                reset();
                rowcount = count;
                state = CursorState.ExecuteNonQuery;
            }
            return res;
        } catch (SQLException e) {
            throw new DataException("执行SQL失败:" + sql, e);
        }
    }

    public int getRowCount() {
        return rowcount;
    }

    public Object[] fetchOne() {
        ensureExecuted();
        if (state == CursorState.Fetchable) {
            Object[] row = readRow();
            scroll();
            return row;
        }
        return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    public List<Object[]> fetchMany(int size) {
        ensureExecuted();
        List<Object[]> list = new ArrayList<>();
        int i = 0;
        while (state == CursorState.Fetchable && i++ < size) {
            list.add(readRow());
            scroll();
        }
        return list;
    }

    public List<Object[]> fetchAll() {
        ensureExecuted();
        List<Object[]> list = new ArrayList<>();
        while (state == CursorState.Fetchable) {
            list.add(readRow());
            scroll();
        }
        return list;
    }

    public KvMap fetchMapOne() {
        ensureExecuted();
        if (state == CursorState.Fetchable) {
            KvMap map = readMap();
            scroll();
            return map;
        }
        return KvMap.empty();
    }

    public List<KvMap> fetchMapMany(int size) {
        ensureExecuted();
        List<KvMap> list = new ArrayList<>();
        int i = 0;
        while (state == CursorState.Fetchable && i++ < size) {
            list.add(readMap());
            scroll();
        }
        return list;
    }

    public List<KvMap> fetchMapAll() {
        ensureExecuted();
        List<KvMap> list = new ArrayList<>();
        while (state == CursorState.Fetchable) {
            list.add(readMap());
            scroll();
        }
        return list;
    }

    KvMap readMap() {
        try {
            ResultSetMetaData meta = resultSet.getMetaData();
            Object[] values = new Object[meta.getColumnCount()];
            KvMap map = new KvMap(values.length);
            for (int i = 1; i <= values.length; i++) {
                map.put(meta.getColumnLabel(i), resultSet.getObject(i));
            }
            return map;
        } catch (SQLException e) {
            throw new DataException("读取行失败", e);
        }
    }

    Object[] readRow() {
        try {
            ResultSetMetaData meta = resultSet.getMetaData();
            Object[] values = new Object[meta.getColumnCount()];
            for (int i = 0; i < values.length; i++) {
                values[i] = resultSet.getObject(i + 1);
            }
            return values;
        } catch (SQLException e) {
            throw new DataException("读取行失败", e);
        }
    }

    void ensureExecuted() {
        if (state == CursorState.Unexecuted) {
            throw new DataException("没有执行SQL");
        }
    }

    private void scroll() {
        try {
            if (resultSet.next()) {
                rowcount++;
                state = CursorState.Fetchable;
            } else {
                reset();
                state = CursorState.EndOfFetch;
            }
        } catch (Exception e) {
            throw new DataException("记录集滚动失败", e);
        }
    }

    private void reset() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            logger.warn("记录集关闭失败", e);
        }
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            logger.warn("SQL关闭失败", e);
        }
        resultSet = null;
        statement = null;
        rowcount = 0;
        state = CursorState.Unexecuted;
    }

    public String quote(String identify) {
        return sqlDialect.quote(identify);
    }

    public List<Object[]> splitForInConditions(Object[] ids) {
        List<Object[]> result = new ArrayList<>();
        if (ids.length < IN_MAX) {
            result.add(ids);
        } else {
            Integer from = 0;
            while (from < ids.length) {
                Integer to = from + IN_MAX;
                if (to > ids.length) {
                    to = ids.length;
                }
                result.add(Arrays.copyOfRange(ids, from, to));
                from += IN_MAX;
            }
        }
        return result;
    }
}
