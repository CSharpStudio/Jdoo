package org.jdoo.exceptions;

/**
 * 类型异常
 * 
 * @author lrz
 */
public class TypeException extends RuntimeException {

    public TypeException() {
    }

    public TypeException(Throwable cause) {
        super(cause);
    }

    public TypeException(String message) {
        super(message);
    }

    public TypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
