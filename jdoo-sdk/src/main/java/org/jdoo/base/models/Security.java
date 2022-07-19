package org.jdoo.base.models;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdoo.*;
import org.jdoo.core.Environment;
import org.jdoo.data.Cursor;

/**
 * 安全工具类
 */
@Model.Meta(name = "rbac.security", description = "用户")
public class Security extends Model {

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
                + " WHERE ru.user=%s AND r.is_admin=%s";
        cr.execute(sql, Arrays.asList(uid, true));
        long count = (Long) cr.fetchOne()[0];
        return count > 0;
    }

    public boolean hasPermission(Records rec, String uid, String model, String auth) {
        // TODO load from cache
        Environment env = rec.getEnv();
        if ((uid.equals(env.getUserId()) && env.isAdmin()) || isAdmin(rec, uid)) {
            return true;
        }
        Cursor cr = env.getCursor();
        String sql = "SELECT count(1) FROM rbac_permission p"
                + " JOIN rbac_role_permission rp on p.id=rp.permission"
                + " JOIN rbac_role_user ru on rp.role=ru.role"
                + " WHERE ru.user=%s AND p.model=%s AND p.auth=%s";
        cr.execute(sql, Arrays.asList(uid, model, auth));
        long count = (Long) cr.fetchOne()[0];
        return count > 0;
    }

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
                + " WHERE ru.user=%s' AND p.model=%s AND p.active=%s";
        cr.execute(sql, Arrays.asList(uid, model, true));
        List<String> auths = cr.fetchAll().stream().map(row -> (String) row[0]).collect(Collectors.toList());
        return auths;
    }
}
