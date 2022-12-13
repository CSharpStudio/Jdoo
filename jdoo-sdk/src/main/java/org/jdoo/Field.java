package org.jdoo;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jdoo.core.Constants;
import org.jdoo.core.MetaField;
import org.jdoo.exceptions.TypeException;
import org.jdoo.fields.*;
import org.jdoo.DeleteMode;

/**
 * 模型字段
 * 
 * @author lrz
 */
@SuppressWarnings("all")
public class Field extends MetaField {
    static Map<String, Class> typeFields = new HashMap<>();
    static Map<String, Class> typeJavaClass = new HashMap<>();

    static {
        registerField(Constants.BINARY, BinaryField.class, byte[].class);
        registerField(Constants.BOOLEAN, BooleanField.class, Boolean.class);
        registerField(Constants.CHAR, CharField.class, String.class);
        registerField(Constants.DATE, DateField.class, java.sql.Date.class);
        registerField(Constants.DATETIME, DateTimeField.class, Timestamp.class);
        registerField(Constants.FLOAT, FloatField.class, Double.class);
        registerField(Constants.HTML, HtmlField.class, String.class);
        registerField(Constants.IMAGE, ImageField.class, byte[].class);
        registerField(Constants.INTEGER, IntegerField.class, Integer.class);
        registerField(Constants.MANY2MANY, Many2manyField.class, Records.class);
        registerField(Constants.MANY2ONE, Many2oneField.class, Records.class);
        registerField(Constants.ONE2MANY, One2manyField.class, Records.class);
        registerField(Constants.SELECTION, SelectionField.class, String.class);
        registerField(Constants.TEXT, TextField.class, String.class);
        registerField(Constants.MANY2ONEREFERENCE, Many2oneReferenceField.class, String.class);
    }

    public static Set<String> getFieldTypes() {
        return typeFields.keySet();
    }

    public static String getFieldName(String type) {
        return typeFields.get(type).getSimpleName();
    }

    public static Class getJavaClass(String type) {
        return typeJavaClass.get(type);
    }

    public static MetaField create(String type) {
        Class<?> clazz = typeFields.get(type);
        if (clazz == null) {
            throw new TypeException(String.format("找不到type=%s的字段", type));
        }
        try {
            return (MetaField) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new TypeException(String.format("创建字段%s失败,class=%s", type, clazz.getName()));
        }
    }

    /**
     * 注册字段类
     * 
     * @param type       类型名称
     * @param fieldClass java类
     */
    public static void registerField(String type, Class<?> fieldClass, Class<?> javaClass) {
        typeFields.put(type, fieldClass);
        typeJavaClass.put(type, javaClass);
    }

    /**
     * 二进制字段
     * 
     * @return
     */
    public static BinaryField Binary() {
        return new BinaryField();
    }

    /**
     * 布尔字段
     * 
     * @return
     */
    public static BooleanField Boolean() {
        return new BooleanField();
    }

    /**
     * 字符字段
     * 
     * @return
     */
    public static CharField Char() {
        return new CharField();
    }

    /**
     * 日期字段
     * 
     * @return
     */
    public static DateField Date() {
        return new DateField();
    }

    /**
     * 日期时间字段
     * 
     * @return
     */
    public static DateTimeField DateTime() {
        return new DateTimeField();
    }

    /**
     * 小数字段
     * 
     * @return
     */
    public static FloatField Float() {
        return new FloatField();
    }

    /**
     * HTML字段
     * 
     * @return
     */
    public static HtmlField Html() {
        return new HtmlField();
    }

    /**
     * 图片字段
     * 
     * @return
     */
    public static ImageField Image() {
        return new ImageField();
    }

    /**
     * 整数字段
     * 
     * @return
     */
    public static IntegerField Integer() {
        return new IntegerField();
    }

    /**
     * 多对多字段
     * 
     * @param comodel  关联的模型
     * @param relation 中间表名称
     * @param column1  中间表中当前模型的字段名
     * @param column2  中间表中关联模型的字段名
     * @return
     */
    public static Many2manyField Many2many(String comodel, String relation, String column1, String column2) {
        return new Many2manyField(comodel, relation, column1, column2);
    }

    /**
     * 多对一字段
     * 
     * @param comodel 关联的模型
     * @return
     */
    public static Many2oneField Many2one(String comodel) {
        return new Many2oneField(comodel);
    }

    /**
     * 一对多字段
     * 
     * @param comodel     关联的模型
     * @param inverseName 关联模型对应Many2one字段的名称
     * @return
     */
    public static One2manyField One2many(String comodel, String inverseName) {
        return new One2manyField(comodel, inverseName);
    }

    /**
     * 选择字段
     * 
     * @return
     */
    public static SelectionField Selection() {
        return new SelectionField();
    }

    /**
     * 选择字段
     * 
     * @param selection 选项
     * @return
     */
    public static SelectionField Selection(Selection selection) {
        return new SelectionField().selection(selection);
    }

    /**
     * 选择字段
     * 
     * @param selection 选项
     * @return
     */
    public static SelectionField Selection(Map<String, String> selection) {
        return new SelectionField().selection(selection);
    }

    /**
     * 大文本字段，超过4000长度的字符串
     * 
     * @return
     */
    public static TextField Text() {
        return new TextField();
    }

    /**
     * 委托继承
     * 
     * @param parentModel
     * @return
     */
    public static Field Delegate(String parentModel) {
        Field field = new Many2oneField(parentModel).ondelete(DeleteMode.Cascade).required();
        field.args.put("delegate", true);
        return field;
    }

    /**
     * 引用关系
     * 
     * @return
     */
    public static Many2oneReferenceField Many2oneReference(String model_field) {
        return new Many2oneReferenceField(model_field);
    }
}
