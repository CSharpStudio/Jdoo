package org.jdoo.exceptions;

/**
 * 验证异常
 * 
 * @author lrz
 */
public class ValidationException extends UserException {
    final static int ERROR_CODE = 1000;

    public ValidationException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    public ValidationException(String message) {
        super(message, ERROR_CODE);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public ValidationException(int errorCode) {
        super(errorCode);
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
