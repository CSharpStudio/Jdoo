package org.jdoo.exceptions;

public class SqlConstraintException extends RuntimeException {
    String constraint;

    public String getConstraint() {
        return constraint;
    }

    public SqlConstraintException(String constraint) {
        super("违反约束:" + constraint);
        this.constraint = constraint;
    }
}
