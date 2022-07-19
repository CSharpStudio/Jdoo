package org.jdoo.base.models;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdoo.*;
import org.jdoo.data.Cursor;
import org.jdoo.exceptions.AccessException;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.util.KvMap;
import org.jdoo.util.SecurityCode;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户
 * 
 * @author lrz
 */
@Model.UniqueConstraint(name = "unique_login", fields = "login", message = "登录账号不能重复")
@Model.UniqueConstraint(name = "unique_mobile", fields = "mobile", message = "手机号已存在")
@Model.UniqueConstraint(name = "unique_email", fields = "email", message = "邮箱已存在")
@Model.Meta(name = "rbac.user", description = "用户")
public class User extends Model {
    static Field name = Field.Char().label("名称").help("姓名");
    static Field login = Field.Char().label("账号").help("登录账号").required();
    static Field password = Field.Char().label("密码").help("登录密码").prefetch(false);
    static Field login_time = Field.DateTime().label("最后登录时间").related("log_ids.create_date");
    static Field email = Field.Char().label("邮箱");
    static Field mobile = Field.Char().label("手机号");
    static Field log_ids = Field.One2many("rbac.user.log", "create_uid");
    static Field active = Field.Boolean();
    static Field role_ids = Field.Many2many("rbac.role", "rbac_role_user", "user", "role");

    /** 名称 */
    public String getName() {
        return (String) get(name);
    }

    /** 名称 */
    public void setName(String value) {
        set(name, value);
    }

    /** 账号 */
    public String getLogin() {
        return (String) get(login);
    }

    /** 账号 */
    public void setLogin(String value) {
        set(login, value);
    }

    /** 密码 */
    public String getPassword() {
        return (String) get(password);
    }

    /** 密码 */
    public void setPassword(String value) {
        set(password, value);
    }

    /** 最后登录时间 */
    public Timestamp getLoginTime() {
        return (Timestamp) get(login_time);
    }

    /** 邮箱 */
    public String getEmail() {
        return (String) get(email);
    }

    /** 邮箱 */
    public void setEmail(String value) {
        set(email, value);
    }

    /** 手机号 */
    public String getMobile() {
        return (String) get(mobile);
    }

    /** 手机号 */
    public void setMobile(String value) {
        set(mobile, value);
    }

    @Model.Constrains("login")
    public void checkLogin(Records rec) {
        for (User u : rec.of(User.class)) {
            String login = u.getLogin();
            if (login.contains("/")) {
                throw new ValidationException(rec.l10n("账号不能包含/"));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> read(Records rec, List<String> fields) {
        List<Map<String, Object>> datas = (List<Map<String, Object>>) rec.callSuper(User.class, "read", fields);
        for (Map<String, Object> data : datas) {
            if (data.containsKey("password")) {
                data.put("password", "********");
            }
        }
        return datas;
    }

    public Map<String, Object> login(Records rec, String login, String password, boolean remember,
            Map<String, Object> userAgent) {
        Records user = rec.find(
                Criteria.equal("login", login).or(Criteria.equal("mobile", login), Criteria.equal("email", login)), 0, 1,
                rec.getMeta().getOrder());
        Security security = rec.getEnv().get("rbac.security").as(Security.class);
        if (!user.any()) {
            security.loginFaild(security.getRecords(), null, userAgent);
            throw new AccessException(rec.l10n("账号[%s]不存在", login), SecurityCode.LOGIN_NOT_FOUND);
        }
        security.beforeLogin(security.getRecords(), rec.getId(), userAgent);
        user = user.withUser(user.getId());
        checkCredentials(user, password, userAgent);
        updateLastLogin(user);
        security.loginSuccess(security.getRecords(), rec.getId(), userAgent);
        // remember ? 7 days : 8 hours
        String uuid = (String) rec.getEnv().get("rbac.token").call("updateToken", user.getId(), remember ? 10080 : 480);
        KvMap map = new KvMap(3)
                .set("login", user.get("login"))
                .set("name", user.get("name"))
                .set("token", uuid)
                .set("id", user.getId());
        return map;
    }

    public void updateLastLogin(Records rec) {
        rec.getEnv().get("rbac.user.log").create(new HashMap<>(0));
    }

    void checkCredentials(Records rec, String password, Map<String, Object> userAgent) {
        Cursor cr = rec.getEnv().getCursor();
        cr.execute("SELECT COALESCE(password, '') FROM rbac_user WHERE id=%s", Arrays.asList(rec.getId()));
        String hashed = (String) cr.fetchOne()[0];
        byte[] bytes = Base64.getDecoder().decode(password);
        String pwd = org.springframework.util.DigestUtils.md5DigestAsHex(bytes);
        if (!hashed.equals(pwd)) {
            if (!hashed.equals(new String(bytes, Charset.forName("utf-8")))) {
                rec.getEnv().get("rbac.security").call("loginFaild", rec.getId(), userAgent);
                throw new AccessException(rec.l10n("密码错误"), SecurityCode.PASSWORD_ERROR);
            } else {
                setEncryptedPassword(rec, bytes);
            }
        }
    }

    void setEncryptedPassword(Records rec, byte[] password) {
        String pwd = org.springframework.util.DigestUtils.md5DigestAsHex(password);
        Cursor cr = rec.getEnv().getCursor();
        cr.execute("UPDATE rbac_user SET password=%s WHERE id=%s", Arrays.asList(pwd, rec.getId()));
        rec.getEnv().getCache().remove(rec, rec.getMeta().getField("password"));
    }

    @Model.ServiceMethod(label = "修改密码")
    public Object changePassword(Records rec, @Doc(doc = "旧密码") String oldPassword,
            @Doc(doc = "新密码") String newPassword) {
        if (StringUtils.isBlank(newPassword)) {
            throw new ValidationException(rec.l10n("设置空密码不符合安全要求"));
        }
        Records user = rec.getEnv().getUser();
        checkCredentials(user, oldPassword, Collections.emptyMap());
        String pwd = org.springframework.util.DigestUtils
                .md5DigestAsHex(Base64.getDecoder().decode(newPassword));
        user.update(new KvMap(1).set("password", pwd));
        return Action.reload("保存成功");
    }

    @Model.ServiceMethod(label = "重置密码")
    public Object resetPassword(Records rec) {
        rec.update(new KvMap(1).set("password", "888888"));
        return Action.reload("保存成功");
    }
}