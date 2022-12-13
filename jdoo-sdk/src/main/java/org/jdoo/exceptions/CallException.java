package org.jdoo.exceptions;

/**
 * 调用异常
 * 
 * @author lrz
 */
public class CallException extends PlatformException {

    public CallException(Throwable cause) {
        super(cause);
    }

    public CallException(String message) {
        super(message);
    }

    public CallException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallException(int errorCode) {
        super(errorCode);
    }

    public CallException(Throwable cause, int errorCode) {
        super(cause, errorCode);
    }

    public CallException(String message, int errorCode) {
        super(message, errorCode);
    }

    public CallException(String message, Throwable cause, int errorCode) {
        super(message, cause, errorCode);
    }
}
