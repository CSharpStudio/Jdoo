package jdoo.https.json;

public class JsonRpcError {
    int code;
    String message;

    public JsonRpcError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }

    public static JsonRpcError ParseError = new JsonRpcError(-32700, "Parse error");
    public static JsonRpcError InvalidRequest = new JsonRpcError(-32600, "Invalid Request");
    public static JsonRpcError MethodNotFound = new JsonRpcError(-32601, "Method not found");
    public static JsonRpcError InvalidParams = new JsonRpcError(-32602, "Invalid params");
    public static JsonRpcError InternalError = new JsonRpcError(-32603, "Internal error");
}
