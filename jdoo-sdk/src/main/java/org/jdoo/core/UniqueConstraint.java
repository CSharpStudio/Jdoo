package org.jdoo.core;

/**
 * @author lrz
 */
public class UniqueConstraint {
    String name;
    String[] fields;
    String message;
    String module;

    public UniqueConstraint(String name, String[] fields, String message, String module) {
        this.name = name;
        this.fields = fields;
        this.message = message;
        this.module = module;
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

    public String getModule() {
        return module;
    }
}
