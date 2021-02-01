package jdoo.exceptions;

public class TypeErrorException extends JdooException {

    private static final long serialVersionUID = 1L;

    public TypeErrorException() {
    }

    public TypeErrorException(Throwable cause) {
        super(cause);
    }

    public TypeErrorException(String message) {
        super(message);
    }

    public TypeErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}

