package org.jdoo.exceptions;

/**
 * 用户异常
 * 
 * @author lrz
 */
public class UserException extends RuntimeException {
    int errorCode = 1000;

    public int getErrorCode() {
        return errorCode;
    }

    public UserException() {
    }

    public UserException(Throwable cause) {
        super(cause);
    }

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserException(Throwable cause, int errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public UserException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UserException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
