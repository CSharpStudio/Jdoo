package org.jdoo.base.controllers;

import java.util.HashMap;
import java.util.Map;

import org.jdoo.ParamIn;
import org.jdoo.ParamOut;
import org.jdoo.base.models.Security;
import org.jdoo.core.BaseService;
import org.jdoo.core.Constants;
import org.jdoo.core.Environment;
import org.jdoo.exceptions.AccessException;
import org.jdoo.https.Controller;
import org.jdoo.https.RequestHandler;
import org.jdoo.https.RequestHandler.AuthType;
import org.jdoo.https.RequestHandler.HandlerType;
import org.jdoo.https.jsonrpc.JsonRpcError;
import org.jdoo.https.jsonrpc.JsonRpcParameter;
import org.jdoo.https.jsonrpc.JsonRpcRequest;
import org.jdoo.https.jsonrpc.JsonRpcResponse;
import org.jdoo.tenants.Tenant;
import org.jdoo.tenants.TenantService;
import org.jdoo.util.SecurityCode;
import org.jdoo.utils.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 服务控制器
 * 
 * @author lrz
 */
@org.springframework.stereotype.Controller
public class ServiceController extends Controller {

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{tenant}/rpc/service/**", method = RequestMethod.POST)
    @RequestHandler(auth = AuthType.USER, type = HandlerType.JSON)
    public Object execute(JsonRpcRequest request) {
        JsonRpcParameter params = request.getParams();
        if (params.isList()) {
            throw new IllegalArgumentException();
        }
        Environment env = getEnv();
        Map<String, Object> paramMap = params.getMap();
        ParamIn in = new ParamIn(env, (String) paramMap.get("model"), request.getMethod(),
                (Map<String, Object>) paramMap.get("args"));
        BaseService svc = env.getRegistry().get(in.getModel()).findService(in.getService());
        if (svc == null) {
            throw new IllegalArgumentException(String.format("模型[%s]未定义服务[%s]", in.getModel(), in.getService()));
        }
        // 权限
        Security security = env.get("rbac.security").as(Security.class);
        if (!Constants.ANONYMOUS.equals(svc.getAuth()) && !env.isAdmin()
                && !security.hasPermission(security.getRecords(), env.getUserId(), in.getModel(), svc.getAuth())) {
            throw new AccessException("没有权限", SecurityCode.NO_PERMISSION);
        }
        // TODO 幂等校验
        ParamOut out = new ParamOut();
        svc.executeService(in, out);
        if (!out.getErrors().isEmpty()) {
            return new JsonRpcResponse(request.getId(),
                    new JsonRpcError(out.getErrorCode(), StringUtils.join(out.getErrors())));
        }
        if (!out.getResult().isEmpty()) {
            String token = (String) env.getContext().get("token");
            if (StringUtils.isNotEmpty(token)) {
                out.putContext("token", token);
            }
            return new JsonRpcResponse(request.getId(), out.getResult());
        }
        return new JsonRpcResponse(request.getId());
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{tenant}/rpc/login/**", method = RequestMethod.POST)
    @RequestHandler(auth = AuthType.NONE, type = HandlerType.JSON)
    public Object login(String login, String password, boolean remember) {
        Map<String, Object> userAgent = new HashMap<>(5);
        userAgent.put("HTTP_HOST", httpServletRequest.getRemoteHost());
        userAgent.put("REMOTE_ADDR", httpServletRequest.getRemoteAddr());
        Map<String, Object> result = (Map<String, Object>) getEnv().get("rbac.user")
                .call("login", login, password, remember, userAgent);
        return result;
    }

    @RequestMapping(value = "/{tenant}/reset*", method = RequestMethod.GET)
    @RequestHandler(auth = AuthType.USER, type = HandlerType.HTTP)
    public void reset() {
        Tenant tenant = getEnv().getRegistry().getTenant();
        Tenant newTenant = new Tenant(tenant.getKey(), tenant.getName(), tenant.getProperties());
        TenantService.register(newTenant);
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @RequestHandler(auth = AuthType.USER, type = HandlerType.HTTP)
    public Object test() {
        return "test";
    }
}
