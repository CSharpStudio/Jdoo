package org.jdoo.data;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 数据库方言
 * 
 * @author lrz
 */
public interface SqlDialect {
    /**
     * 数据库标识符最大长度
     */
    public static final int IDENTITY_MAX_LENGTH = 63;
    public static Logger schema = LogManager.getLogger("sql.schema");

    /**
     * 获取数据库时间
     * 
     * @return
     */
    String getNowUtc();

    /**
     * 生成别名
     * 
     * @param srcTableAlias
     * @param link
     * @return
     */
    default String generateTableAlias(String srcTableAlias, String link) {
        String alias = srcTableAlias + "__" + link;
        if (alias.length() > IDENTITY_MAX_LENGTH) {
            CRC32 crc = new CRC32();
            crc.update(alias.getBytes(Charset.forName("UTF-8")));
            alias = alias.substring(0, IDENTITY_MAX_LENGTH - 10) + "_" + Long.toString(crc.getValue(), 32);
        }
        return alias;
    }

    /**
     * 创建字段
     * 
     * @param cr
     * @param table
     * @param name
     * @param columnType
     * @param comment
     * @param notNull
     */
    void createColumn(Cursor cr, String table, String name, String columnType, String comment, boolean notNull);

    /**
     * 根据{@link ColumnType}转换数据库字段类型
     * 
     * @param type
     * @return
     */
    String getColumnType(ColumnType type);

    /**
     * 根据{@link ColumnType}转换数据库字段类型
     * 
     * @param type
     * @param length
     * @param precision
     * @return
     */
    String getColumnType(ColumnType type, Integer length, Integer precision);

    /**
     * 判断指定的表名是否存在
     * 
     * @param cr
     * @param table
     * @return
     */
    boolean tableExists(Cursor cr, String table);

    /**
     * 判断指定的表名是否存在
     * 
     * @param cr
     * @param tableNames
     * @return 存在的表名
     */
    List<String> existingTables(Cursor cr, List<String> tableNames);

    /**
     * 创建模型的表
     * 
     * @param cr
     * @param table
     * @param comment
     */
    void createModelTable(Cursor cr, String table, String comment);

    void createM2MTable(Cursor cr, String table, String column1, String column2, String comment);

    /**
     * 获取指定表的字段
     * 
     * @param cr
     * @param table
     * @return
     */
    Map<String, DbColumn> tableColumns(Cursor cr, String table);

    /**
     * 为数据库标识符添加分隔符：如oracle:"identify", sqlserver:[identify], mysql: `identify`
     * 
     * @param identify
     * @return
     */
    String quote(String identify);

    /**
     * 生成分页sql
     * 
     * @param sql
     * @param limit
     * @param offset
     * @return
     */
    String getPaging(String sql, Integer limit, Integer offset);

    /**
     * 数据类型转换
     * 
     * @param column
     * @param type
     * @return
     */
    String cast(String column, ColumnType type);

    /**
     * 添加唯一约束
     * 
     * @param cr
     * @param table
     * @param constraint
     * @param fields
     */
    void addUniqueConstraint(Cursor cr, String table, String constraint, String[] fields);

    /**
     * 获取约束名
     * 
     * @param cause
     * @return
     */
    String getConstraint(SQLException cause);

    /**
     * 设置非空
     * 
     * @param cr
     * @param table
     * @param column
     * @param columnType
     */
    void setNotNull(Cursor cr, String table, String column, String columnType);

    /**
     * 设置可空
     * 
     * @param cr
     * @param table
     * @param column
     * @param columnType
     */
    void dropNotNull(Cursor cr, String table, String column, String columnType);
}
