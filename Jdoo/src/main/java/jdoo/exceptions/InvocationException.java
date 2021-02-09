package jdoo.exceptions;

public class InvocationException extends JdooException {

    private static final long serialVersionUID = 1L;

    public InvocationException() {
    }

    public InvocationException(Throwable cause) {
        super(cause);
    }

    public InvocationException(String message) {
        super(message);
    }

    public InvocationException(Object key) {
        super(String.format("key %s", key));
    }

    public InvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
