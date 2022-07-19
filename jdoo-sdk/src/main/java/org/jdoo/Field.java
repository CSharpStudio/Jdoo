package org.jdoo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jdoo.core.Constants;
import org.jdoo.core.MetaField;
import org.jdoo.exceptions.TypeException;
import org.jdoo.fields.*;

/**
 * 模型字段
 * 
 * @author lrz
 */
@SuppressWarnings("all")
public class Field extends MetaField {
    static Map<String, Class> typeFields = new HashMap<>();

    static {
        registerField(Constants.BINARY, BinaryField.class);
        registerField(Constants.BOOLEAN, BooleanField.class);
        registerField(Constants.CHAR, CharField.class);
        registerField(Constants.DATE, DateField.class);
        registerField(Constants.DATETIME, DateTimeField.class);
        registerField(Constants.FLOAT, FloatField.class);
        registerField(Constants.HTML, HtmlField.class);
        registerField(Constants.IMAGE, ImageField.class);
        registerField(Constants.INTEGER, IntegerField.class);
        registerField(Constants.MANY2MANY, Many2manyField.class);
        registerField(Constants.MANY2ONE, Many2manyField.class);
        registerField(Constants.ONE2MANY, One2manyField.class);
        registerField(Constants.SELECTION, SelectionField.class);
        registerField(Constants.TEXT, TextField.class);
    }

    public static Set<String> getFieldTypes() {
        return typeFields.keySet();
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

    public static void registerField(String type, Class<?> fieldClass) {
        typeFields.put(type, fieldClass);
    }

    public static BinaryField Binary() {
        return new BinaryField();
    }

    public static BooleanField Boolean() {
        return new BooleanField();
    }

    public static CharField Char() {
        return new CharField();
    }

    public static DateField Date() {
        return new DateField();
    }

    public static DateTimeField DateTime() {
        return new DateTimeField();
    }

    public static FloatField Float() {
        return new FloatField();
    }

    public static HtmlField Html() {
        return new HtmlField();
    }

    public static ImageField Image() {
        return new ImageField();
    }

    public static IntegerField Integer() {
        return new IntegerField();
    }

    public static Many2manyField Many2many(String comodel, String relation, String column1, String column2) {
        return new Many2manyField(comodel, relation, column1, column2);
    }

    public static Many2oneField Many2one(String comodel) {
        return new Many2oneField(comodel);
    }

    public static One2manyField One2many(String comodel, String inverseName) {
        return new One2manyField(comodel, inverseName);
    }

    public static SelectionField Selection() {
        return new SelectionField();
    }

    public static SelectionField Selection(Selection selection) {
        return new SelectionField().selection(selection);
    }

    public static TextField Text() {
        return new TextField();
    }
}
