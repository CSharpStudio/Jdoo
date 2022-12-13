package com.addons.demo.models.fields;

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
    static Field f_bool = Field.Boolean().label("布尔").help("布尔字段").required(true).auth();
    static Field f_date = Field.Date().label("日期").help("日期字段").required(true).auth();
    static Field f_date_range = Field.Date().label("日期范围").help("日期字段");
    static Field f_datetime = Field.DateTime().label("日期时间").help("日期时间字段");
    static Field f_datetime_range = Field.DateTime().label("时间范围").help("时间范围字段");
    static Field f_float = Field.Float().label("小数").help("小数字段").auth();
    static Field f_html = Field.Html().label("HTML").help("HTML字段").auth();
    static Field f_int = Field.Integer().label("整数").help("整数字段");
    static Field f_text = Field.Text().label("大文本").help("大文本字段");
    static Field f_image = Field.Image().label("图片").help("图片字段");
    static Field f_radio = Field.Selection(Selection.value(new HashMap<String, String>(16) {
        {
            put("1", "星期一");
            put("2", "星期二");
            put("3", "星期三");
            put("4", "星期四");
            put("5", "星期五");
        }
    })).label("单选").help("单选字段");

    static Field f_priority = Field.Selection(Selection.value(new HashMap<String, String>(16) {
        {
            put("1", "不推荐");
            put("2", "一般");
            put("3", "不错");
            put("4", "很棒");
            put("5", "极力推荐");
        }
    })).label("评分").help("评分字段");

    static Field f_selection = Field.Selection(Selection.value(new HashMap<String, String>(16) {
        {
            put("1", "星期一");
            put("2", "星期二");
            put("3", "星期三");
            put("4", "星期四");
            put("5", "星期五");
        }
    })).label("选择").help("选择字段");

    static Field f_m2o_id = Field.Many2one("demo.many2one").label("多对一");
    static Field f_o2m_ids = Field.One2many("demo.one2many", "field_id").label("一对多");
    static Field f_m2m_tags = Field.Many2many("demo.many2many", "demo_m2mtag", "f_m2m_tags", "field_ids").label("多对多");
    static Field f_m2m_ids = Field.Many2many("demo.many2many", "demo_m2m", "f_m2m_ids", "field_ids").label("多对多");

    static Field f_compute = Field.Char().label("计算字段")
            .compute(Callable.script("r->r.get('f_char')+'('+r.get('f_bool')+')'"));
}