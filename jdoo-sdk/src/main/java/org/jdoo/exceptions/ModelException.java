package org.jdoo.exceptions;

/**
 * 模型异常
 * 
 * @author lrz
 */
public class ModelException extends PlatformException {
    final static int ERROR_CODE = 13;

    public ModelException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    public ModelException(String message) {
        super(message, ERROR_CODE);
    }

    public ModelException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public ModelException(int errorCode) {
        super(errorCode);
    }

    public ModelException(Throwable cause, int errorCode) {
        super(cause, errorCode);
    }

    public ModelException(String message, int errorCode) {
        super(message, errorCode);
    }

    public ModelException(String message, Throwable cause, int errorCode) {
        super(message, cause, errorCode);
    }
}
