package jdoo.exceptions;

public class AccessErrorException extends JdooException {


    /**
     *
     */
    private static final long serialVersionUID = -286004599619699513L;

    public AccessErrorException() {
    }

    public AccessErrorException(Throwable cause) {
        super(cause);
    }

    public AccessErrorException(String message) {
        super(message);
    }

    public AccessErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}

