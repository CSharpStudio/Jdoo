package org.jdoo.exceptions;

/**
 * 模型异常
 * 
 * @author lrz
 */
public class ModelException extends RuntimeException {

    public ModelException() {
    }

    public ModelException(Throwable cause) {
        super(cause);
    }

    public ModelException(String message) {
        super(message);
    }

    public ModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
