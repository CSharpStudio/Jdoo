package jdoo.exceptions;

public class ParseException extends JdooException {

    /**
     *
     */
    private static final long serialVersionUID = -7028649977105713167L;

    public ParseException() {
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}