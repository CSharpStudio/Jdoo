package org.jdoo.tenants;

import java.util.HashMap;

import org.jdoo.exceptions.MissingException;

/**
 * 租户服务
 * 
 * @author lrz
 */
public class TenantService {
    HashMap<String, Tenant> tenants = new HashMap<>();

    static TenantService instance;

    public static TenantService getInstance() {
        if (instance == null) {
            instance = new TenantService();
        }
        return instance;
    }

    public static void setInstance(TenantService svc) {
        instance = svc;
    }

    public static void register(Tenant tenant) {
        getInstance().tenants.put(tenant.getKey(), tenant);
    }

    public static Tenant get(String tenant) {
        Tenant result = getInstance().tenants.get(tenant);
        if (result == null) {
            throw new MissingException(String.format("租户[%s]不存在", tenant));
        }
        return result;
    }

    public static Tenant find(String tenant) {
        return getInstance().tenants.get(tenant);
    }
}
