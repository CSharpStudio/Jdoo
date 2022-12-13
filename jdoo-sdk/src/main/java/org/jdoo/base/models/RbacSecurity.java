package org.jdoo.base.models;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.jdoo.*;
import org.jdoo.core.Constants;
import org.jdoo.core.Environment;
import org.jdoo.data.Cursor;
import org.jdoo.utils.ObjectUtils;

/**
 * 安全工具类
 */
@Model.Meta(name = "rbac.security", label = "用户")
@Model.Service(remove = "@all")
public class RbacSecurity extends Model {

    public void loginFaild(Records rec, String uid, Map<String, Object> userAgent) {

    }

    public void loginSuccess(Records rec, String uid, Map<String, Object> userAgent) {

    }

    public void beforeLogin(Records rec, String uid, Map<String, Object> userAgent) {

    }

    public boolean isAdmin(Records rec, String uid) {
        Cursor cr = rec.getEnv().getCursor();
        String sql = "SELECT count(1) FROM rbac_role r"
                + " JOIN rbac_role_user ru on r.id=ru.role"
                + " WHERE ru." + cr.quote("user") + "=%s AND r.is_admin=%s AND r.active=%s";
        cr.execute(sql, Arrays.asList(uid, true, true));
        long count = ObjectUtils.toLong(cr.fetchOne()[0]);
        return count > 0;
    }

    /**
     * 判断用户是否有指定模型的指定权限码
     * 
     * @param rec
     * @param uid   用户id
     * @param model 模型名称
     * @param auth  权限码
     * @return
     */
    public boolean hasPermission(Records rec, String uid, String model, String auth) {
        // TODO load from cache
        Environment env = rec.getEnv();
        if ((uid.equals(env.getUserId()) && env.isAdmin()) || isAdmin(rec, uid)) {
            return true;
        }
        if ("read".equals(auth)) {
            String authModel = env.getRegistry().get(model).getAuthModel();
            if (StringUtils.isNotEmpty(authModel)) {
                model = authModel;
            }
        }
        Cursor cr = env.getCursor();
        String sql = "SELECT count(1) FROM rbac_permission p"
                + " JOIN rbac_role_permission rp on p.id=rp.permission"
                + " JOIN rbac_role_user ru on rp.role=ru.role"
                + " JOIN rbac_role r on r.id=ru.role"
                + " WHERE ru.user=%s AND p.model=%s AND p.auth=%s AND r.active=%s";
        cr.execute(sql, Arrays.asList(uid, model, auth, true));
        long count = (Long) cr.fetchOne()[0];
        return count > 0;
    }

    /**
     * 是否有模型的创建权限
     * 
     * @param rec
     * @param model
     * @return
     */
    @Model.ServiceMethod(auth = Constants.ANONYMOUS, label = "是否有模型创建权限")
    public boolean canCreate(Records rec, String model) {
        return hasPermission(rec, rec.getEnv().getUserId(), model, "create");
    }

    /**
     * 权限用户获取指定模型已分配的所有权限码
     * 
     * @param rec
     * @param uid   用户id
     * @param model 模型名称
     * @return
     */
    public List<String> getPermissions(Records rec, String uid, String model) {
        // TODO load from cache
        Environment env = rec.getEnv();
        Cursor cr = env.getCursor();
        if ((uid.equals(env.getUserId()) && env.isAdmin()) || isAdmin(rec, uid)) {
            String sql = "SELECT p.auth FROM rbac_permission p WHERE p.model=%s AND p.active=%s";
            cr.execute(sql, Arrays.asList(model, true));
            List<String> auths = cr.fetchAll().stream().map(row -> (String) row[0]).collect(Collectors.toList());
            return auths;
        }
        String sql = "SELECT p.auth FROM rbac_permission p"
                + " JOIN rbac_role_permission rp on p.id=rp.permission"
                + " JOIN rbac_role_user ru on rp.role=ru.role"
                + " JOIN rbac_role r on r.id=ru.role"
                + " WHERE ru." + cr.quote("user") + "=%s' AND p.model=%s AND p.active=%s AND r.active=%s";
        cr.execute(sql, Arrays.asList(uid, model, true, true));
        List<String> auths = cr.fetchAll().stream().map(row -> (String) row[0]).collect(Collectors.toList());
        return auths;
    }
}
