package com.addons.demo.models;

import java.util.HashMap;

import org.jdoo.*;

/**
 * 示例
 * 
 * @author lrz
 */
@Model.Meta(name = "demo.field_type")
public class FieldType extends Model {
    static Field f_char = Field.Char().label("字符").help("字符字段").index(true).required(true);
    static Field f_bool = Field.Boolean().label("布尔").help("布尔字段").required(true);
    static Field f_date = Field.Date().label("日期").help("日期字段").required(true);
    static Field f_datetime = Field.DateTime().label("日期时间").help("日期时间字段");
    static Field f_float = Field.Float().label("小数").help("小数字段");
    static Field f_html = Field.Html().label("HTML").help("HTML字段");
    static Field f_int = Field.Integer().label("整数").help("整数字段");
    static Field f_text = Field.Text().label("大文本").help("大文本字段");
    static Field f_selection = Field.Selection(Selection.value(new HashMap<String, String>(16) {
        {
            put("1", "星期一");
            put("2", "星期二");
            put("3", "星期三");
            put("4", "星期四");
            put("5", "星期五");
        }
    })).label("选择").help("选择字段");
}