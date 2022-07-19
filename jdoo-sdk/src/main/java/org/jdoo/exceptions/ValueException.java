package org.jdoo.exceptions;

/**
 * 值异常
 * 
 * @author lrz
 */
public class ValueException extends RuntimeException {

    public ValueException() {
    }

    public ValueException(Throwable cause) {
        super(cause);
    }

    public ValueException(String message) {
        super(message);
    }

    public ValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
