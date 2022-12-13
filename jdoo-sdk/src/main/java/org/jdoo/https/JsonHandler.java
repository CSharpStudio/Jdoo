package org.jdoo.https;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdoo.core.Environment;
import org.jdoo.exceptions.UserException;
import org.jdoo.https.RequestHandler.AuthType;
import org.jdoo.https.jsonrpc.JsonRpcError;
import org.jdoo.https.jsonrpc.JsonRpcParameter;
import org.jdoo.https.jsonrpc.JsonRpcRequest;
import org.jdoo.https.jsonrpc.JsonRpcResponse;
import org.jdoo.https.jsonrpc.RpcId;
import org.jdoo.https.jsonrpc.exceptions.JsonRpcInvalidParamsException;
import org.jdoo.tenants.Tenant;
import org.jdoo.util.SecurityCode;
import org.jdoo.utils.StringUtils;
import org.jdoo.utils.ThrowableUtils;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * json 处理
 * 
 * @author lrz
 */
@Component
public class JsonHandler {
    final static String CONTEXT = "context";
    private static Logger logger = LoggerFactory.getLogger(JsonHandler.class);

    @SuppressWarnings("unchecked")
    public void process(HttpServletRequest request, HttpServletResponse response, Controller ctl, Tenant tenant,
            HandlerMethod handlerMethod, AuthType auth) {
        JsonRpcRequest rpcRequest;
        Object[] args;
        ObjectMapper mapper = new ObjectMapper();
        try {
            rpcRequest = mapper.readValue(request.getReader(), JsonRpcRequest.class);
        } catch (Exception e) {
            JsonRpcResponse res = new JsonRpcResponse(JsonRpcError.createParseError(e));
            setResponse(res, response, mapper);
            logger.warn("json process error", e);
            return;
        }
        JsonRpcParameter params = rpcRequest.getParams();
        Map<String, Object> context = null;
        if (!params.isList()) {
            Map<String, ?> map = params.getMap();
            if (map.containsKey(CONTEXT)) {
                context = (Map<String, Object>) map.get(CONTEXT);
                request.setAttribute(CONTEXT, context);
                map.remove(CONTEXT);
            }
        }
        ctl.before(tenant, context);
        if (auth == AuthType.USER) {
            String uid = ctl.getEnv().getUserId();
            if (StringUtils.isEmpty(uid)) {
                JsonRpcResponse res = new JsonRpcResponse(rpcRequest.getId(),
                        new JsonRpcError(SecurityCode.UNAUTHORIZED, "身份验证失败"));
                setResponse(res, response, mapper);
                return;
            }
        }

        // TODO check auth
        try {
            Method method = handlerMethod.getMethod();
            args = getArgs(method, rpcRequest);
        } catch (JsonRpcInvalidParamsException e) {
            JsonRpcResponse res = new JsonRpcResponse(rpcRequest.getId(), JsonRpcError.createInvalidParams(e));
            setResponse(res, response, mapper);
            logger.warn("json process error", e);
            return;
        }
        try {
            JsonRpcResponse res = getResponse(handlerMethod, ctl, args, rpcRequest.getId());
            setResponse(res, response, mapper);
        } catch (Throwable e) {
            logger.warn("json process error", e);
            Throwable cause = ThrowableUtils.getCause(e);
            if (cause instanceof IllegalArgumentException) {
                JsonRpcResponse res = new JsonRpcResponse(rpcRequest.getId(), JsonRpcError.createInvalidParams(e));
                setResponse(res, response, mapper);
                return;
            }
            if (cause instanceof UserException) {
                UserException userError = (UserException) cause;
                JsonRpcResponse res = new JsonRpcResponse(rpcRequest.getId(),
                        new JsonRpcError(userError.getErrorCode(), userError.getMessage(), e));
                setResponse(res, response, mapper);
                return;
            }
            JsonRpcResponse res = new JsonRpcResponse(rpcRequest.getId(), JsonRpcError.createInternalError(e));
            setResponse(res, response, mapper);
        }
    }

    void setResponse(Object res, HttpServletResponse response, ObjectMapper mapper) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter pw = response.getWriter()) {
            String json = mapper.writeValueAsString(res);
            pw.write(json);
        } catch (Exception e) {
            logger.warn("json set response error", e);
            throw new RuntimeException("处理Json响应失败", e);
        }
    }

    JsonRpcResponse getResponse(HandlerMethod handler, Controller ctl, Object[] args, RpcId rpcId)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object result;
        try {
            result = handler.getMethod().invoke(ctl, args);
            Environment env = ctl.getEnv();
            env.get("base").flush();
            env.getCursor().commit();
        } finally {
            if (ctl != null) {
                ctl.after();
            }
        }
        if (result instanceof JsonRpcResponse) {
            JsonRpcResponse res = (JsonRpcResponse) result;
            res.setId(rpcId);
            return res;
        } else {
            return new JsonRpcResponse(rpcId, result);
        }
    }

    Object[] getArgs(Method method, JsonRpcRequest request) throws JsonRpcInvalidParamsException {
        Parameter[] params = method.getParameters();
        if (params.length == 1 && params[0].getType() == JsonRpcRequest.class) {
            return new Object[] { request };
        }
        JsonRpcParameter args = request.getParams();
        if (args.isList()) {
            List<?> list = args.getList();
            if (list.size() != params.length) {
                throw new JsonRpcInvalidParamsException(
                        "length of params not match, require:" + params.length + ", but get:" + list.size());
            }
            return list.toArray();
        }
        Object[] result = new Object[params.length];
        Map<String, ?> map = args.getMap();
        for (int i = 0; i < params.length; i++) {
            Parameter p = params[i];
            String key = p.getName();
            if (map.containsKey(key)) {
                result[i] = map.get(key);
            } else {
                throw new JsonRpcInvalidParamsException("missing params:" + key);
            }
        }
        return result;
    }
}
