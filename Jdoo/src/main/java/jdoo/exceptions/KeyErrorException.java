package jdoo.exceptions;

public class KeyErrorException extends JdooException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public KeyErrorException() {
    }

    public KeyErrorException(Throwable cause) {
        super(cause);
    }

    public KeyErrorException(String message) {
        super(message);
    }

    public KeyErrorException(Object key) {
        super(String.format("key %s", key));
    }

    public KeyErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
