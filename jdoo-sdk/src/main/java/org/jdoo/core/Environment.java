package org.jdoo.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jdoo.Records;
import org.jdoo.data.Cursor;
import org.jdoo.exceptions.MissingException;
import org.jdoo.tenants.Tenant;
import org.jdoo.util.Cache;
import org.jdoo.util.ToUpdate;
import org.jdoo.utils.ArrayUtils;

/**
 * 环境上下文
 * 
 * @author lrz
 */
public class Environment {
    Registry registry;
    Map<String, Object> context;
    Cursor cursor;
    String uid;
    Boolean isAdmin;
    Records user;

    static ThreadLocal<Cache> cache = ThreadLocal.withInitial(() -> new Cache());
    static ThreadLocal<ToUpdate> toupdate = ThreadLocal.withInitial(() -> new ToUpdate());

    public void close() {
        cursor.close();
        cache.remove();
        toupdate.remove();
    }

    public Environment(Registry registry, Cursor cursor, String uid, Map<String, Object> context) {
        this.registry = registry;
        this.cursor = cursor;
        this.uid = uid;
        if (context == null) {
            context = new HashMap<>();
        }
        this.context = context;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Cache getCache() {
        return cache.get();
    }

    public ToUpdate getToUpdate() {
        return toupdate.get();
    }

    public Registry getRegistry() {
        return registry;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public String getUserId() {
        return uid;
    }

    public boolean isAdmin() {
        if (isAdmin == null) {
            isAdmin = (Boolean) get("rbac.security").call("isAdmin", uid);
        }
        return isAdmin;
    }

    public Records getUser() {
        if (user == null) {
            user = get("rbac.user", uid);
        }
        return user;
    }

    public Records findRef(String xmlid) {
        return (Records) get("ir.model.data").call("findRef", xmlid);
    }

    public Records getRef(String xmlid) {
        Records result = findRef(xmlid);
        if (result == null) {
            throw new MissingException(l10n("找不到xmlid：%s", xmlid));
        }
        return result;
    }

    public Records get(String model) {
        MetaModel meta = registry.get(model);
        return meta.browse(this, ArrayUtils.EMPTY_STRING_ARRAY, null);
    }

    public Records get(String model, String... ids) {
        MetaModel meta = registry.get(model);
        return meta.browse(this, ids, null);
    }

    public Records get(String model, Collection<String> ids) {
        MetaModel meta = registry.get(model);
        return meta.browse(this, ids.toArray(ArrayUtils.EMPTY_STRING_ARRAY), null);
    }

    public Object getTenantData(String key) {
        Tenant tenant = registry.getTenant();
        if (tenant != null) {
            return tenant.getData().get(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T getTenantData(String key, Supplier<T> supplier) {
        Tenant tenant = registry.getTenant();
        if (tenant != null) {
            Map<String, Object> data = tenant.getData();
            if (!data.containsKey(key)) {
                T value = supplier.get();
                // TODO 可能有并发问题
                data.put(key, value);
                return value;
            }
            return (T) data.get(key);
        }
        return supplier.get();
    }

    public void setTenantData(String key, Object val) {
        Tenant tenant = registry.getTenant();
        if (tenant != null) {
            tenant.getData().put(key, val);
        }
    }

    public String getLang() {
        return "zh_CN";
    }

    /**
     * 本地化翻译
     * 
     * @param str
     * @return
     */
    public String l10n(String format, Object... args) {
        // TODO 翻译format
        return String.format(format, args);
    }
}
