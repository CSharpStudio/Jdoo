package jdoo.exceptions;

public class UserException extends JdooException {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UserException() {
    }

    public UserException(Throwable cause) {
        super(cause);
    }

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}

