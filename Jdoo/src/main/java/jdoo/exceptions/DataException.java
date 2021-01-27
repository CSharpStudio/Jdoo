package jdoo.exceptions;

public class DataException extends JdooException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DataException() {
    }

    public DataException(Throwable cause) {
        super(cause);
    }

    public DataException(String message) {
        super(message);
    }

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }
}