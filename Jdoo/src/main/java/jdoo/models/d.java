package jdoo.models;

public class d {
    public static Domain on(String property, String op, Object value) {
        return new Domain().on(property, op, value);
    }

    public static Domain on(String domain) {
        Domain d = new Domain();
        //TODO解析domain
        return d;
    }

    public static Domain and() {
        return new Domain().and();
    }

    public static Domain or() {
        return new Domain().or();
    }
}
