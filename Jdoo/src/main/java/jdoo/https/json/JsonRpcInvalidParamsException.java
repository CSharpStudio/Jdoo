package jdoo.https.json;

public class JsonRpcInvalidParamsException extends JsonRpcException {

    /**
     *
     */
    private static final long serialVersionUID = 3359270147002989970L;

    public JsonRpcInvalidParamsException() {
    }

    public JsonRpcInvalidParamsException(Throwable cause) {
        super(cause);
    }

    public JsonRpcInvalidParamsException(String message) {
        super(message);
    }

    public JsonRpcInvalidParamsException(String message, Throwable cause) {
        super(message, cause);
    }

}
