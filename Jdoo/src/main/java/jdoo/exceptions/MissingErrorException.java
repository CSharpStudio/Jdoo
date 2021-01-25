package jdoo.exceptions;

public class MissingErrorException extends JdooException {


    /**
     *
     */
    private static final long serialVersionUID = 592831104646264580L;

    public MissingErrorException() {
    }

    public MissingErrorException(Throwable cause) {
        super(cause);
    }

    public MissingErrorException(String message) {
        super(message);
    }

    public MissingErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}

