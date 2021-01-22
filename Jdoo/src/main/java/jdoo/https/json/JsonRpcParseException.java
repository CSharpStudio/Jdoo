package jdoo.https.json;

public class JsonRpcParseException extends JsonRpcException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public JsonRpcParseException() {
    }

    public JsonRpcParseException(Throwable cause) {
        super(cause);
    }

    public JsonRpcParseException(String message) {
        super(message);
    }

    public JsonRpcParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
