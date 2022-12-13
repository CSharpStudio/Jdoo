package org.jdoo.exceptions;

/**
 * 数据异常
 * 
 * @author lrz
 */
public class DataException extends PlatformException {

    public DataException(Throwable cause) {
        super(cause);
    }

    public DataException(String message) {
        super(message);
    }

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataException(int errorCode) {
        super(errorCode);
    }

    public DataException(Throwable cause, int errorCode) {
        super(cause, errorCode);
    }

    public DataException(String message, int errorCode) {
        super(message, errorCode);
    }

    public DataException(String message, Throwable cause, int errorCode) {
        super(message, cause, errorCode);
    }
}
