package org.jdoo.exceptions;

/**
 * 缺失异常
 * 
 * @author lrz
 */
public class MissingException extends RuntimeException {

    public MissingException() {
    }

    public MissingException(Throwable cause) {
        super(cause);
    }

    public MissingException(String message) {
        super(message);
    }

    public MissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
