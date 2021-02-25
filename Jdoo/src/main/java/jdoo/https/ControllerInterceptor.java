package jdoo.https;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.HandlerInterceptor;

import jdoo.https.json.JsonRpcError;
import jdoo.https.json.JsonRpcInvalidParamsException;
import jdoo.https.json.JsonRpcParameter;
import jdoo.https.json.JsonRpcRequest;
import jdoo.https.json.JsonRpcResponse;
import jdoo.util.Default;
import jdoo.util.TypeUtils;

import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

public class ControllerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
        if (object instanceof HandlerMethod) {
            HandlerMethod handler = (HandlerMethod) object;
            http.Route route = handler.getMethodAnnotation(http.Route.class);
            if (route != null && "json".equals(route.type())) {
                boolean debug = StringUtils.hasText(request.getParameter("debug"));
                ObjectMapper mapper = new ObjectMapper();
                JsonRpcRequest req;
                JsonRpcResponse res;
                try {
                    req = mapper.readValue(request.getReader(), JsonRpcRequest.class);
                    res = getResponse(handler, req, debug);
                } catch (Exception exc) {
                    res = new JsonRpcResponse(JsonRpcError.ParseError, getDetails(exc, debug));
                }
                response.setContentType("application/json");
                String json = mapper.writeValueAsString(res);
                PrintWriter pw = response.getWriter();
                pw.write(json);
                pw.flush();
                pw.close();
                return false;
            }
        }
        return true;
    }

    JsonRpcResponse getResponse(HandlerMethod handler, JsonRpcRequest request, boolean debug) {
        try {
            Method method = handler.getMethod();
            Object[] args = getArgs(method, request);
            Object result = method.invoke(handler.getBean(), args);
            if (result instanceof JsonRpcResponse) {
                JsonRpcResponse res = (JsonRpcResponse) result;
                res.setId(request.getId());
                return res;
            } else {
                return new JsonRpcResponse(request.getId(), result);
            }
        } catch (JsonRpcInvalidParamsException e) {
            return new JsonRpcResponse(JsonRpcError.InvalidParams, getDetails(e, debug));
        } catch (IllegalArgumentException e) {
            return new JsonRpcResponse(JsonRpcError.InvalidParams, getDetails(e, debug));
        } catch (Exception e) {
            return new JsonRpcResponse(JsonRpcError.InternalError, getDetails(e, debug));
        }
    }

    String getDetails(Exception e, boolean debug) {
        e.printStackTrace();
        if (debug) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString(); // stack trace as a string
        }
        String msg = e.toString();
        Throwable t = e.getCause();
        while (t != null) {
            msg += ", Caused by: " + t.toString();
            Throwable cause = t.getCause();
            if (cause != t)
                t = cause;
            else
                break;
        }
        return msg;
    }

    Object[] getArgs(Method method, JsonRpcRequest request) throws JsonRpcInvalidParamsException {
        Parameter[] params = method.getParameters();
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
                Default default_ = p.getAnnotation(Default.class);
                if (default_ != null) {
                    String v = default_.value();
                    if ("".equals(v)) {
                        result[i] = TypeUtils.getDefaultValue(p.getType());
                    } else {
                        result[i] = TypeUtils.parse(v, p.getType());
                    }
                } else {
                    throw new JsonRpcInvalidParamsException("missing params:" + key);
                }
            }
        }
        return result;
    }
}
