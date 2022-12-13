package org.jdoo.exceptions;

/**
 * 用户异常
 * 
 * @author lrz
 */
public class UserException extends PlatformException {
    final static int ERROR_CODE = 1020;

    public UserException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    public UserException(String message) {
        super(message, ERROR_CODE);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public UserException(int errorCode) {
        super(errorCode);
    }

    public UserException(Throwable cause, int errorCode) {
        super(cause, errorCode);
    }

    public UserException(String message, int errorCode) {
        super(message, errorCode);
    }

    public UserException(String message, Throwable cause, int errorCode) {
        super(message, cause, errorCode);
    }
}
