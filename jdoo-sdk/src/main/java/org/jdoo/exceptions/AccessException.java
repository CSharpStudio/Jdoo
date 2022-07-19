package org.jdoo.exceptions;

/**
 * 访问异常
 * 
 * @author lrz
 */
public class AccessException extends UserException {

    public AccessException() {
    }

    public AccessException(Throwable cause) {
        super(cause);
    }

    public AccessException(String message) {
        super(message);
    }

    public AccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessException(Throwable cause, int errorCode) {
        super(cause, errorCode);
    }

    public AccessException(String message, int errorCode) {
        super(message, errorCode);
    }

    public AccessException(String message, Throwable cause, int errorCode) {
        super(message, cause, errorCode);
    }
}
