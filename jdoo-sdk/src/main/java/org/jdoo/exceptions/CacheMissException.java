package org.jdoo.exceptions;

/**
 * 内存数据不存在异常
 * 
 * @author lrz
 */
public class CacheMissException extends RuntimeException {

    public CacheMissException() {
    }

    public CacheMissException(Throwable cause) {
        super(cause);
    }

    public CacheMissException(String message) {
        super(message);
    }

    public CacheMissException(String message, Throwable cause) {
        super(message, cause);
    }
}
