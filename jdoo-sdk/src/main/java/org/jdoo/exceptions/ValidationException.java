package org.jdoo.exceptions;

/**
 * 验证异常
 * 
 * @author lrz
 */
public class ValidationException extends UserException {

    public ValidationException() {
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause, int errorCode) {
        super(cause, errorCode);
    }

    public ValidationException(String message, int errorCode) {
        super(message, errorCode);
    }

    public ValidationException(String message, Throwable cause, int errorCode) {
        super(message, cause, errorCode);
    }
}
