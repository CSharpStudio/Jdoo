package jdoo.models;

import java.util.List;
import java.util.function.Function;

import jdoo.util.Pair;

public class fields {
    public static BooleanField Boolean() {
        return new BooleanField();
    }

    public static BooleanField Boolean(String string) {
        return new BooleanField().string(string);
    }

    public static IntegerField Integer() {
        return new IntegerField();
    }

    public static IntegerField Integer(String string) {
        return new IntegerField().string(string);
    }

    public static FloatField Float() {
        return new FloatField();
    }

    public static FloatField Float(String string) {
        return new FloatField().string(string);
    }

    public static FloatField Float(String string, Pair<Integer, Integer> digits) {
        return new FloatField().string(string).digits(digits);
    }

    public static MonetaryField Monetary() {
        return new MonetaryField();
    }

    public static MonetaryField Monetary(String string) {
        return new MonetaryField().string(string);
    }

    public static MonetaryField Monetary(String string, String currency_field) {
        return new MonetaryField().string(string).currency_field(currency_field);
    }

    public static CharField Char() {
        return new CharField();
    }

    public static CharField Char(String string) {
        return new CharField().string(string);
    }

    public static TextField Text() {
        return new TextField();
    }

    public static TextField Text(String string) {
        return new TextField().string(string);
    }

    public static HtmlField Html() {
        return new HtmlField();
    }

    public static HtmlField Html(String string) {
        return new HtmlField().string(string);
    }

    public static DateField Date() {
        return new DateField();
    }

    public static DateField Date(String string) {
        return new DateField().string(string);
    }

    public static DateTimeField DateTime() {
        return new DateTimeField();
    }

    public static DateTimeField DateTime(String string) {
        return new DateTimeField().string(string);
    }

    public static BinaryField Binary() {
        return new BinaryField();
    }

    public static BinaryField Binary(String string) {
        return new BinaryField().string(string);
    }

    public static ImageField Image() {
        return new ImageField();
    }

    public static ImageField Image(String string) {
        return new ImageField().string(string);
    }

    public static SelectionField Selection() {
        return new SelectionField();
    }

    public static SelectionField Selection(List<Pair<String, String>> selection) {
        return new SelectionField().selection(selection);
    }

    public static SelectionField Selection(List<Pair<String, String>> selection, String string) {
        return new SelectionField().string(string).selection(selection);
    }

    public static SelectionField Selection(Function<RecordSet, Object> func) {
        return new SelectionField().selection(func);
    }

    public static ReferenceField Reference() {
        return new ReferenceField();
    }

    public static ReferenceField Reference(List<Pair<String, String>> selection) {
        return (ReferenceField) new ReferenceField().selection(selection);
    }

    public static ReferenceField Reference(List<Pair<String, String>> selection, String string) {
        return (ReferenceField) new ReferenceField().string(string).selection(selection);
    }

    public static Many2oneField Many2one() {
        return new Many2oneField();
    }

    public static Many2oneField Many2one(String comodel_name) {
        return new Many2oneField().comodel_name(comodel_name);
    }

    public static Many2oneField Many2one(String comodel_name, String string) {
        return new Many2oneField().string(string).comodel_name(comodel_name);
    }

    public static One2manyField One2many() {
        return new One2manyField();
    }

    public static One2manyField One2many(String comodel_name) {
        return new One2manyField().comodel_name(comodel_name);
    }

    public static One2manyField One2many(String comodel_name, String inverse_name) {
        return new One2manyField().inverse_name(inverse_name).comodel_name(comodel_name);
    }

    public static One2manyField One2many(String comodel_name, String inverse_name, String string) {
        return new One2manyField().inverse_name(inverse_name).comodel_name(comodel_name).string(string);
    }

    public static Many2manyField Many2many() {
        return new Many2manyField();
    }

    public static Many2manyField Many2many(String comodel_name) {
        return new Many2manyField().comodel_name(comodel_name);
    }

    public static Many2manyField Many2many(String comodel_name, String relation) {
        return new Many2manyField().relation(relation).comodel_name(comodel_name);
    }

    public static Many2manyField Many2many(String comodel_name, String relation, String column1) {
        return new Many2manyField().relation(relation).comodel_name(comodel_name).column1(column1);
    }

    public static Many2manyField Many2many(String comodel_name, String relation, String column1, String column2) {
        return new Many2manyField().relation(relation).comodel_name(comodel_name).column1(column1).column2(column2);
    }

    public static Many2manyField Many2many(String comodel_name, String relation, String column1, String column2,
            String string) {
        return new Many2manyField().relation(relation).comodel_name(comodel_name).column1(column1).column2(column2)
                .string(string);
    }
}
