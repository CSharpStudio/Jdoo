package jdoo.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jdoo.exceptions.DataException;
import jdoo.tools.Dict;
import jdoo.tools.Tuple;

public class Cursor implements AutoCloseable {
    private static Logger logger = LogManager.getLogger(Cursor.class);
    static int IN_MAX = 1000;// decent limit on size of IN queries - guideline = Oracle limit
    Connection connection;
    PreparedStatement statement;
    ResultSet resultSet;
    CursorState state;

    public enum CursorState {
        Unexecuted, // 未执行execute，此时调用fetch*()将抛异常
        ExecuteNonQuery, // 已执行execute但没有返回数据
        Fetchable, // 数据可读，此时可执行fetch*()
        EndOfFetch,
    }

    public Cursor(DataSource ds) {
        try {
            connection = ds.getConnection();
        } catch (SQLException e) {
            throw new DataException("get connection error", e);
        }
        setAutoCommit(false);
    }

    public Cursor(Database ds) {
        connection = ds.getConnection();
        setAutoCommit(false);
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
            throw new DataException("commit error", e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataException("rollback error", e);
        }
    }

    public void close() {
        reset();
        try {
            connection.rollback();
        } catch (SQLException e) {
            logger.warn("connection rollback error", e);
        }
        try {
            connection.close();
        } catch (SQLException e) {
            logger.warn("connection close error", e);
        }
    }

    private void reset() {
        try {
            if (resultSet != null)
                resultSet.close();
        } catch (SQLException e) {
            logger.warn("resultSet close error", e);
        }
        try {
            if (statement != null)
                statement.close();
        } catch (SQLException e) {
            logger.warn("statement close error", e);
        }
        resultSet = null;
        statement = null;
        rowcount = 0;
        state = CursorState.Unexecuted;
    }

    public boolean closed() {
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    public List<Tuple<?>> split_for_in_conditions(Collection<?> set) {
        return split_for_in_conditions(set, IN_MAX);
    }

    public List<Tuple<?>> split_for_in_conditions(Collection<?> set, int size) {
        List<Tuple<?>> result = new ArrayList<>();
        Object[] original = set.toArray();
        if (size > 0) {
            int left = set.size();
            int from = 0;
            int to;
            while (left > 0) {
                if (left > size) {
                    to = from + size;
                } else {
                    to = from + left;
                }
                Object[] batch = Arrays.copyOfRange(original, from, to);
                result.add(new Tuple<>(batch));
                left -= size;
                from += size;
            }
        } else {
            result.add(new Tuple<>(original));
        }
        return result;
    }

    int rowcount;

    public int rowcount() {
        return rowcount;
    }

    public boolean execute(String sql) {
        return execute(sql, new Object[0], false);
    }

    public boolean execute(String sql, boolean log_exceptions) {
        return execute(sql, new Object[0], log_exceptions);
    }

    public boolean execute(String sql, Object... params) {
        return execute(sql, params, false);
    }

    public boolean execute(String sql, Object[] params, boolean log_exceptions) {
        logger.debug(mogrify(sql, params));
        reset();
        try {
            statement = connection.prepareStatement(sql.replace("%s", "?"));
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof Date) {
                    Date dt = (Date) param;
                    statement.setDate(i + 1, new java.sql.Date(dt.getTime()));
                } else if (param instanceof Collection<?>) {
                    Collection<?> c = (Collection<?>) param;
                    statement.setObject(i + 1, c.toArray());
                } else {
                    statement.setObject(i + 1, param);
                }
            }
            boolean result = statement.execute();
            if (!result) {
                rowcount = statement.getUpdateCount();
                reset();
                state = CursorState.ExecuteNonQuery;
            } else {
                resultSet = statement.getResultSet();
                scroll();
            }
            return result;
        } catch (

        SQLException e) {
            throw new DataException("execute sql error:" + sql, e);
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
            throw new DataException("result set scroll error", e);
        }
    }

    public String mogrify(String sql, Object... params) {
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param instanceof String) {
                args[i] = "'" + ((String) param).replace("'", "''") + "'";
            } else if (param instanceof Date) {
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                args[i] = "'" + sf.format((Date) param) + "'";
            } else {
                args[i] = param;
            }
        }
        return String.format(sql, args);
    }

    void ensureExecuted() {
        if (state == CursorState.Unexecuted)
            throw new DataException("no sql executed");
    }

    public Tuple<?> fetchone() {
        ensureExecuted();
        if (state == CursorState.Fetchable) {
            Tuple<?> row = readTuple();
            scroll();
            return row;
        }
        return Tuple.emptyTuple();
    }

    public List<Tuple<?>> fetchmany(int size) {
        ensureExecuted();
        List<Tuple<?>> list = new ArrayList<Tuple<?>>();
        int i = 0;
        while (state == CursorState.Fetchable && i++ < size) {
            list.add(readTuple());
            scroll();
        }
        return list;
    }

    public List<Tuple<?>> fetchall() {
        ensureExecuted();
        List<Tuple<?>> list = new ArrayList<>();
        while (state == CursorState.Fetchable) {
            list.add(readTuple());
            scroll();
        }
        return list;
    }

    public Dict dictfetchone() {
        ensureExecuted();
        if (state == CursorState.Fetchable) {
            Dict dict = readDict();
            scroll();
            return dict;
        }
        return new Dict();
    }

    public List<Dict> dictfetchmany(int size) {
        ensureExecuted();
        List<Dict> list = new ArrayList<>();
        int i = 0;
        while (state == CursorState.Fetchable && i++ < size) {
            list.add(readDict());
            scroll();
        }
        return list;
    }

    public List<Dict> dictfetchall() {
        ensureExecuted();
        List<Dict> list = new ArrayList<>();
        while (state == CursorState.Fetchable) {
            list.add(readDict());
            scroll();
        }
        return list;
    }

    Tuple<?> readTuple() {
        try {
            ResultSetMetaData meta = resultSet.getMetaData();
            Object[] values = new Object[meta.getColumnCount()];
            for (int i = 0; i < values.length; i++) {
                values[i] = resultSet.getObject(i + 1);
            }
            return new Tuple<>(values);
        } catch (SQLException e) {
            throw new DataException("read row error", e);
        }
    }

    Dict readDict() {
        try {
            ResultSetMetaData meta = resultSet.getMetaData();
            Dict dict = new Dict();
            Object[] values = new Object[meta.getColumnCount()];
            for (int i = 1; i <= values.length; i++) {
                dict.set(meta.getColumnLabel(i), resultSet.getObject(i));
            }
            return dict;
        } catch (SQLException e) {
            throw new DataException("read row error", e);
        }
    }
}
