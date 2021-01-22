package jdoo.exceptions;

public class MissingException extends JdooException {


    /**
     *
     */
    private static final long serialVersionUID = 592831104646264580L;

    public MissingException() {
    }

    public MissingException(Throwable cause) {
        super(cause);
    }

    public MissingException(String message) {
        super(message);
    }

    public MissingException(String message, Throwable cause) {
        super(message, cause);
    }
}

