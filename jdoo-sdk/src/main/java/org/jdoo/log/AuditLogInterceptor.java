package org.jdoo.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.jdoo.https.Controller;
import org.jdoo.https.RequestHandler;
import org.jdoo.utils.SnowFlakeUtil;
import org.jdoo.utils.StringUtils;
import org.jdoo.utils.ThrowableUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;

public class AuditLogInterceptor implements AsyncHandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(AuditLogInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
        }
        HandlerMethod method = (HandlerMethod) handler;
        Object bean = method.getBean();
        if (bean instanceof Controller && method.getMethodAnnotation(RequestHandler.class) != null) {
            MDC.put("logID", String.valueOf(SnowFlakeUtil.getSnowflakeId()));
            MDC.put("isSuccess", String.valueOf(true));
            MDC.put("ticks", String.valueOf(System.currentTimeMillis()));
            MDC.put("ip", getIpAddress(request));
            MDC.put("methodType", request.getMethod());
            String query = request.getQueryString();
            if (StringUtils.isNotEmpty(query)) {
                String[] qArr = query.split("=");
                if (qArr.length > 1) {
                    MDC.put(qArr[0], qArr[1]);
                }
            }
            // MDC.put("Module", request.getParameter("module"));
            /*
             * Object[] paraArr = request.getParameterMap().keySet().toArray();
             * if(paraArr.length>1) {
             * MDC.put("Request", String.valueOf(paraArr[1]));
             * }
             */
        }
        return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception ex)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            Object bean = method.getBean();
            if (bean instanceof Controller && method.getMethodAnnotation(RequestHandler.class) != null) {
                String ticks = MDC.get("ticks");
                if (StringUtils.isNotEmpty(ticks)) {
                    long executionTime = System.currentTimeMillis() - Long.valueOf(ticks);
                    MDC.put("executionTime", String.valueOf(executionTime));
                }
                if (StringUtils.isNotEmpty(MDC.get("errorCode"))) {
                    MDC.put("isSuccess", String.valueOf(false));
                }
                if (ex != null) {
                    MDC.put("errorCode", "-1");
                    MDC.put("errorMsg", ThrowableUtils.getDebug(ex));
                }
                logger.info("auditLog");
            }
        }
        AsyncHandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && !"".equals(ip) && !"unknown".equalsIgnoreCase(ip)) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
            if (ip.equals("127.0.0.1")) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (request.getHeader("X-Real-IP") != null && !"".equals(request.getHeader("X-Real-IP"))
                && !"unknown".equalsIgnoreCase(request.getHeader("X-Real-IP"))) {
            ip = request.getHeader("X-Real-IP");
        }

        if (request.getHeader("Proxy-Client-IP") != null && !"".equals(request.getHeader("Proxy-Client-IP"))
                && !"unknown".equalsIgnoreCase(request.getHeader("Proxy-Client-IP"))) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (request.getHeader("WL-Proxy-Client-IP") != null && !"".equals(request.getHeader("WL-Proxy-Client-IP"))
                && !"unknown".equalsIgnoreCase(request.getHeader("WL-Proxy-Client-IP"))) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
