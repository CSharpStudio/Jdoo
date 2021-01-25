package jdoo.exceptions;

public class ValueErrorException extends JdooException {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ValueErrorException() {
    }

    public ValueErrorException(Throwable cause) {
        super(cause);
    }

    public ValueErrorException(String message) {
        super(message);
    }

    public ValueErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
