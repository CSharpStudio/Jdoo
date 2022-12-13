package org.jdoo.https;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdoo.core.Constants;
import org.jdoo.core.Environment;
import org.jdoo.core.Registry;
import org.jdoo.data.Cursor;
import org.jdoo.exceptions.ValueException;
import org.jdoo.tenants.Tenant;
import org.jdoo.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 控制器
 * 
 * @author lrz
 */
public class Controller {
    @Autowired
    protected HttpServletRequest httpServletRequest;
    @Autowired
    protected HttpServletResponse httpServletResponse;
    @Autowired
    TenantResolver tenantResolver;

    static ThreadLocal<Environment> env = new ThreadLocal<>();

    public Environment getEnv() {
        Environment result = env.get();
        if (result == null) {
            throw new ValueException("env未初始化");
        }
        return result;
    }

    void before(Tenant tenant, Map<String, Object> ctx) {
        env.set(createEnv(tenant, ctx));
    }

    void after() {
        Environment e = env.get();
        if (e != null) {
            e.close();
        }
        env.remove();
    }

    private Environment createEnv(Tenant tenant, Map<String, Object> ctx) {
        if (ctx == null) {
            ctx = new HashMap<>();
        }
        initContextFromCookie(ctx);
        Registry reg = tenant.getRegistry();
        Cursor cr = tenant.getDatabase().openCursor();
        String token = (String) ctx.get("token");
        String uid = (String) new Environment(reg, cr, Constants.SYSTEM_USER, ctx)
                .get("rbac.token").call("getUserId", token);
        if (StringUtils.isEmpty(uid)) {
            ctx.remove("token");
        }
        return new Environment(reg, cr, uid, ctx);
    }

    String getCookieValue(Cookie cookie) {
        try {
            return java.net.URLDecoder.decode(cookie.getValue(), "UTF-8");
        } catch (Exception e) {
            return cookie.getValue();
        }
    }

    void initContextFromCookie(Map<String, Object> ctx) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.startsWith("ctx_")) {
                    ctx.putIfAbsent(name.substring(4), getCookieValue(cookie));
                }
            }
        }
    }

    protected void redirectToLogin() {
        try {
            String path = tenantResolver.resolveTenantKey(httpServletRequest);
            String url = httpServletRequest.getRequestURI();
            String query = httpServletRequest.getQueryString();
            if (StringUtils.isNotEmpty(query)) {
                url += "?" + query;
            }
            httpServletResponse.sendRedirect("/" + path + "/login?url=" + url);
        } catch (Exception e) {

        }
    }
}
