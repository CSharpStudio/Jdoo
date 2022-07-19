package org.jdoo.https.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * json rpc 响应
 * 
 * @author lrz
 */
public class JsonRpcResponse {
    RpcId id;
    String jsonrpc = "2.0";
    @JsonInclude(Include.NON_NULL)
    JsonRpcError error;
    @JsonInclude(Include.NON_NULL)
    Object result;

    public JsonRpcResponse() {
    }

    public JsonRpcResponse(RpcId id) {
        this.id = id;
    }

    public JsonRpcResponse(RpcId id, Object result) {
        this.id = id;
        this.result = result;
    }

    public JsonRpcResponse(RpcId id, JsonRpcError error) {
        this.id = id;
        this.error = error;
    }

    public JsonRpcResponse(JsonRpcError error) {
        this.error = error;
    }

    public RpcId getId() {
        return id;
    }

    public void setId(RpcId id) {
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String version) {
        this.jsonrpc = version;
    }

    public JsonRpcError getError() {
        return error;
    }

    public void setError(JsonRpcError error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
