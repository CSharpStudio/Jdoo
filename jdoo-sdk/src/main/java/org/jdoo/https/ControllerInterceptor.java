package org.jdoo.https;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jdoo.core.Environment;
import org.jdoo.data.Cursor;
import org.jdoo.https.RequestHandler.AuthType;
import org.jdoo.tenants.Tenant;
import org.jdoo.utils.HttpUtils;
import org.jdoo.utils.ThrowableUtils;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;

/**
 * 控制器拦截器
 * 
 * @author lrz
 */
public class ControllerInterceptor implements HandlerInterceptor {

    @Autowired
    JsonHandler jsonHandler;

    @Autowired
    TenantResolver tenantResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Object bean = handlerMethod.getBean();
            if (bean instanceof Controller) {
                Tenant tenant = tenantResolver.resolve(request);
                if (tenant == null) {
                    response.setStatus(404);
                    return false;
                }
                Controller ctl = (Controller) bean;
                RequestHandler requestHandler = handlerMethod.getMethodAnnotation(RequestHandler.class);
                if (requestHandler != null) {
                    if (RequestHandler.HandlerType.JSON.equals(requestHandler.type())) {
                        jsonHandler.process(request, response, ctl, tenant, handlerMethod, requestHandler.auth());
                        return false;
                    }
                    ctl.before(tenant, null);
                    if (requestHandler.auth().equals(AuthType.USER)) {
                        String uid = ctl.getEnv().getUserId();
                        if (StringUtils.isEmpty(uid)) {
                            response.setStatus(401);
                            return false;
                        }
                    }
                }
                ctl.before(tenant, null);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable ModelAndView modelAndView) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Object bean = handlerMethod.getBean();
            if (bean instanceof Controller) {
                Controller ctl = (Controller) bean;
                Environment env = ctl.getEnv();
                env.get("base").flush();
                Cursor cr = env.getCursor();
                // 自动提交事务
                cr.commit();
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception ex)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Object bean = handlerMethod.getBean();
            if (bean instanceof Controller) {
                Controller ctl = (Controller) bean;
                ctl.after();
            }
        }
        if (ex != null) {
            response.setStatus(500);
            HttpUtils.WriteHtml(response, ThrowableUtils.getDebug(ex).replaceAll("\r\n", "<br/>").replaceAll("\t",
                    "&nbsp;&nbsp;&nbsp;&nbsp;"));
        }
    }
}
