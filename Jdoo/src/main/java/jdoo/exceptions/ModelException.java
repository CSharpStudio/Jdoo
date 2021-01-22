package jdoo.exceptions;

public class ModelException extends JdooException {


    /**
     *
     */
    private static final long serialVersionUID = -2531922469905055887L;

    public ModelException() {
    }

    public ModelException(Throwable cause) {
        super(cause);
    }

    public ModelException(String message) {
        super(message);
    }

    public ModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
