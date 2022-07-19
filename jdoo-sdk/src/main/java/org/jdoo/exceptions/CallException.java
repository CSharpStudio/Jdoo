package org.jdoo.exceptions;

/**
 * 调用异常
 * 
 * @author lrz
 */
public class CallException extends RuntimeException {

    public CallException() {
    }

    public CallException(Throwable cause) {
        super(cause);
    }

    public CallException(String message) {
        super(message);
    }

    public CallException(String message, Throwable cause) {
        super(message, cause);
    }
}
