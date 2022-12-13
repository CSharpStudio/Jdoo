package org.jdoo.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.collections4.SetUtils;

import org.jdoo.Records;
import org.jdoo.data.Cursor;
import org.jdoo.exceptions.AccessException;
import org.jdoo.exceptions.MissingException;
import org.jdoo.tenants.Tenant;
import org.jdoo.util.Cache;
import org.jdoo.util.ToUpdate;
import org.jdoo.utils.ArrayUtils;
import org.jdoo.utils.StringUtils;

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
    Records company;
    Records companies;

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

    /**
     * 用户当前的公司。
     * 如果没指定company_ids上下文，默认使用用户的主公司。
     *
     * @exception AccessException 上下文中company_ids的公司无效或者没有权限
     * @return 当前公司，默认是用户的主公司
     */
    @SuppressWarnings("unchecked")
    public Records getCompany() {
        if (company == null) {
            List<String> company_ids = (List<String>) context.getOrDefault("company_ids", Collections.emptyList());
            if (company_ids.size() > 0) {
                Set<String> user_company_ids = SetUtils.hashSet(getUser().getRel("company_ids").getIds());
                for (String cid : company_ids) {
                    if (!user_company_ids.contains(cid)) {
                        throw new AccessException("访问未授权或无效的公司");
                    }
                }
                company = get("res.company", company_ids.get(0));
            } else {
                company = getUser().getRel("company_id");
            }
        }
        return company;
    }

    /**
     * 用户访问的公司集。
     * 如果没指定company_ids上下文，默认使用用户的所有公司。
     *
     * @exception AccessException 上下文中company_ids的公司无效或者没有权限
     * @return
     */
    public Records getCompanies() {
        if (companies == null) {
            String[] company_ids = ((String) context.getOrDefault("company_ids", "")).split(",");
            if (company_ids.length > 0) {
                Set<String> user_company_ids = SetUtils.hashSet(getUser().getRel("company_ids").getIds());
                for (String cid : company_ids) {
                    if (!user_company_ids.contains(cid)) {
                        throw new AccessException("访问未授权或无效的公司");
                    }
                }
                companies = get("res.company", company_ids);
            } else {
                companies = getUser().getRel("company_ids");
            }
        }
        return companies;
    }

    /**
     * 查找xmlid关联的数据集
     *
     * @param xmlid
     * @return
     */
    public Records findRef(String xmlid) {
        return (Records) get("ir.model.data").call("findRef", xmlid);
    }

    /**
     * 获取xmlid关联的数据集
     *
     * @param xmlid
     * @exception MissingException 找不到xmlid的数据集
     * @return
     */
    public Records getRef(String xmlid) {
        Records result = findRef(xmlid);
        if (result == null) {
            throw new MissingException(l10n("找不到xmlid：%s", xmlid));
        }
        return result;
    }

    /**
     * 获取模型的空数据集
     *
     * @param model
     * @return
     */
    public Records get(String model) {
        MetaModel meta = registry.get(model);
        return meta.browse(this, ArrayUtils.EMPTY_STRING_ARRAY, null);
    }

    /**
     * 获取模型指定id的数据集
     *
     * @param model
     * @param ids
     * @return
     */
    public Records get(String model, String... ids) {
        MetaModel meta = registry.get(model);
        return meta.browse(this, ids, null);
    }

    /**
     * 获取模型指定id的数据集
     *
     * @param model
     * @param ids
     * @return
     */
    public Records get(String model, Collection<String> ids) {
        MetaModel meta = registry.get(model);
        return meta.browse(this, ids.toArray(ArrayUtils.EMPTY_STRING_ARRAY), null);
    }

    /**
     * 获取租户数据
     *
     * @param key
     * @return
     */
    public Object getTenantData(String key) {
        Tenant tenant = registry.getTenant();
        if (tenant != null) {
            return tenant.getData().get(key);
        }
        return null;
    }

    /**
     * 获取租户数据，不存在时插入初始值
     *
     * @param <T>
     * @param key
     * @param supplier
     * @return
     */
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

    /**
     * 设置租户数据
     *
     * @param key
     * @param val
     */
    public void setTenantData(String key, Object val) {
        Tenant tenant = registry.getTenant();
        if (tenant != null) {
            tenant.getData().put(key, val);
        }
    }

    /**
     * 获取当前语言代码
     *
     * @return
     */
    public String getLang() {
        String lang = (String) context.get("lang");
        if (StringUtils.isEmpty(lang)) {
            lang = (String) getUser().get("lang");
        }
        if (StringUtils.isEmpty(lang)) {
            lang = "zh_CN";
        }
        return lang;
    }

    /**
     * 获取时区
     * 
     * @return
     */
    public String getTimezone() {
        String tz = (String) context.get("tz");
        if (StringUtils.isEmpty(tz)) {
            tz = (String) getUser().get("tz");
        }
        if (StringUtils.isEmpty(tz)) {
            tz = "UTC";
        }
        return tz;
    }

    public boolean isDebug() {
        return "true".equals(context.get("debug"));
    }

    /**
     * 本地化翻译
     *
     * @param str
     * @return
     */
    public String l10n(String format, Object... args) {
        String lang = getLang();
        Map<String, String> data = getTenantData("lang@" + lang, () -> {
            Map<String, String> kv = new HashMap<>();
            Cursor cr = getCursor();
            String sql = "SELECT a.name,a.value FROM res_localization a JOIN res_lang b on a.lang_id=b.id WHERE b.iso_code=%s";
            cr.execute(sql, Arrays.asList(lang));
            for (Object[] row : cr.fetchAll()) {
                kv.put((String) row[0], (String) row[1]);
            }
            return kv;
        });
        String value = data.getOrDefault(format, format);
        return String.format(value, args);
    }
}
