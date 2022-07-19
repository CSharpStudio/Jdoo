package org.jdoo.exceptions;

/**
 * 数据异常
 * 
 * @author lrz
 */
public class DataException extends RuntimeException {

    public DataException() {
    }

    public DataException(Throwable cause) {
        super(cause);
    }

    public DataException(String message) {
        super(message);
    }

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }
}
