package jdoo.exceptions;

public class AccessException extends JdooException {


    /**
     *
     */
    private static final long serialVersionUID = -286004599619699513L;

    public AccessException() {
    }

    public AccessException(Throwable cause) {
        super(cause);
    }

    public AccessException(String message) {
        super(message);
    }

    public AccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

