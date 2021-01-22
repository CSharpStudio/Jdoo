package jdoo.exceptions;

public class ValidationException extends JdooException {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ValidationException() {
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
