package org.jdoo.https;

import javax.servlet.http.HttpServletRequest;

import org.jdoo.tenants.Tenant;
import org.jdoo.tenants.TenantService;
import org.jdoo.utils.StringUtils;

import org.springframework.stereotype.Component;

@Component
public class TenantResolver {
    public Tenant resolve(HttpServletRequest request) {
        String key = resolveTenantKey(request);
        return TenantService.find(key);
    }

    public String resolveTenantKey(HttpServletRequest request) {
        String[] parts = request.getRequestURI().split("/");
        String key = null;
        for (String part : parts) {
            if (StringUtils.isNoneBlank(part)) {
                key = part;
                break;
            }
        }
        return key;
    }
}
