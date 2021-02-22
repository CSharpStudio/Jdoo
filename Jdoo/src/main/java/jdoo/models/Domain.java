package jdoo.models;

import java.util.ArrayList;

import jdoo.util.Tuple;

public class Domain extends ArrayList<Object> {
    private static final long serialVersionUID = 1L;

    public Domain on(String property, String op, Object value) {
        add(new Tuple<>(property, op, value));
        return this;
    }

    public Domain and() {
        add("&");
        return this;
    }

    public Domain or() {
        add("|");
        return this;
    }

    public class Criteria {
        String property;
        String op;
        Object value;

        public Criteria(String property, String op, Object value) {
            this.property = property;
            this.op = op;
            this.value = value;
        }
    }

    public class Group {
        String group;

        public Group(String group) {
            this.group = group;
        }
    }
}