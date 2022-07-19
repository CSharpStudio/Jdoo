package org.jdoo.core;

/**
 * @author lrz
 */
public class UniqueConstraint {
    String name;
    String[] fields;
    String message;

    public UniqueConstraint(String name, String[] fields, String message) {
        this.name = name;
        this.fields = fields;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String[] getFields() {
        return fields;
    }

    public String getMessage() {
        return message;
    }
}
