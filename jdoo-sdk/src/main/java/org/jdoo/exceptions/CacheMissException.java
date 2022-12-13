package org.jdoo.exceptions;

/**
 * 内存数据不存在异常
 * 
 * @author lrz
 */
public class CacheMissException extends PlatformException {

    public CacheMissException(Throwable cause) {
        super(cause);
    }

    public CacheMissException(String message) {
        super(message);
    }

    public CacheMissException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheMissException(int errorCode) {
        super(errorCode);
    }

    public CacheMissException(Throwable cause, int errorCode) {
        super(cause, errorCode);
    }

    public CacheMissException(String message, int errorCode) {
        super(message, errorCode);
    }

    public CacheMissException(String message, Throwable cause, int errorCode) {
        super(message, cause, errorCode);
    }
}
