package jdoo.exceptions;

public class ValidationErrorException extends JdooException {

    private static final long serialVersionUID = 1L;

    public ValidationErrorException() {
    }

    public ValidationErrorException(Throwable cause) {
        super(cause);
    }

    public ValidationErrorException(String message) {
        super(message);
    }

    public ValidationErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}

