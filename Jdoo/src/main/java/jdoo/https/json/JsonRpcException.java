package jdoo.https.json;

public class JsonRpcException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -5395094718964613693L;

    public JsonRpcException() {
    }

    public JsonRpcException(Throwable cause) {
        super(cause);
    }

    public JsonRpcException(String message) {
        super(message);
    }

    public JsonRpcException(String message, Throwable cause) {
        super(message, cause);
    }
}
