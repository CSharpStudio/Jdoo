package org.jdoo.data;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库方言
 * 
 * @author lrz
 */
public interface SqlDialect {
    public static Logger schema = LoggerFactory.getLogger("sql.schema");
    
    /**
     * 数据库标识符最大长度
     */
    default int getIdentityMaxLength() {
        return 63;
    }

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
        String alias = limitIdentity(srcTableAlias + "__" + link);
        return alias;
    }

    /**
     * 处理列名
     * 
     * @param column
     * @return
     */
    default String getColumnLabel(String column) {
        return column;
    }

    /**
     * 处理数据值
     * 
     * @param obj
     * @return
     */
    default Object getObject(Object obj) {
        return obj;
    }

    /**
     * 准备参数值
     * 
     * @param obj
     * @return
     */
    default Object prepareObject(Object obj) {
        return obj;
    }

    /**
     * 标识符长度限制
     * 
     * @param identity
     * @return
     */
    default String limitIdentity(String identity) {
        if (identity.length() > getIdentityMaxLength()) {
            CRC32 crc = new CRC32();
            crc.update(identity.getBytes(Charset.forName("UTF-8")));
            identity = identity.substring(0, getIdentityMaxLength() - 10) + "_" + Long.toString(crc.getValue(), 32);
        }
        return identity;
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

    /**
     * 创建多对多中间表
     * 
     * @param cr
     * @param table
     * @param column1
     * @param column2
     * @param comment
     */
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
    String addUniqueConstraint(Cursor cr, String table, String constraint, String[] fields);

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

    /**
     * 获取指定表的外键信息
     * 
     * @param cr
     * @param tables
     * @return
     */
    List<Object[]> getForeignKeys(Cursor cr, Collection<String> tables);

    /**
     * 添加外键
     * 
     * @param cr
     * @param table1
     * @param column1
     * @param table2
     * @param column2
     * @param ondelete
     * @return
     */
    String addForeignKey(Cursor cr, String table1, String column1, String table2, String column2, String ondelete);

    /**
     * 删除约束
     * 
     * @param cr
     * @param table
     * @param name
     */
    void dropConstraint(Cursor cr, String table, String name);

    /**
     * 处理异常
     * 
     * @param err
     * @param sql
     * @return
     */
    RuntimeException getError(SQLException err, SqlFormat sql);
}
