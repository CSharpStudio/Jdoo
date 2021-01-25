package jdoo.exceptions;

public class UserErrorException extends JdooException {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UserErrorException() {
    }

    public UserErrorException(Throwable cause) {
        super(cause);
    }

    public UserErrorException(String message) {
        super(message);
    }

    public UserErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}

