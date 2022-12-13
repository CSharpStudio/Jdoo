package org.jdoo.exceptions;

/**
 * 类型异常
 * 
 * @author lrz
 */
public class TypeException extends PlatformException {

    public TypeException(Throwable cause) {
        super(cause);
    }

    public TypeException(String message) {
        super(message);
    }

    public TypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeException(int errorCode) {
        super(errorCode);
    }

    public TypeException(Throwable cause, int errorCode) {
        super(cause, errorCode);
    }

    public TypeException(String message, int errorCode) {
        super(message, errorCode);
    }

    public TypeException(String message, Throwable cause, int errorCode) {
        super(message, cause, errorCode);
    }
}
