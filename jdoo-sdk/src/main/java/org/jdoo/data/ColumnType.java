package org.jdoo.data;

/**
 * 字段类型
 * 
 * @author lrz
 */
public enum ColumnType {
    /** 不映射数据库 */
    None,
    /** 可变文本 */
    VarChar,
    /** 布尔 */
    Boolean,
    /** 长文本 */
    Text,
    /** 二进制 */
    Binary,
    /** 整型 */
    Integer,
    /** 长整型 */
    Long,
    /** 小数 */
    Float,
    /** 日期 */
    Date,
    /** 日期时间 */
    DateTime
}
