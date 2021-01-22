package jdoo.exceptions;

public class JdooException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 7914951921574187300L;

    public JdooException() {
    }

    public JdooException(Throwable cause) {
        super(cause);
    }

    public JdooException(String message) {
        super(message);
    }

    public JdooException(String message, Throwable cause) {
        super(message, cause);
    }
}
