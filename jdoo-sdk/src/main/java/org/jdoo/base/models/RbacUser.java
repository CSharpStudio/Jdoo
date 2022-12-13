package org.jdoo.base.models;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.jdoo.*;
import org.jdoo.core.Constants;
import org.jdoo.data.Cursor;
import org.jdoo.exceptions.AccessException;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.util.KvMap;
import org.jdoo.util.SecurityCode;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户
 * 
 * @author lrz
 */
@Model.UniqueConstraint(name = "unique_login", fields = "login", message = "登录账号不能重复")
@Model.UniqueConstraint(name = "unique_mobile", fields = "mobile", message = "手机号已存在")
@Model.UniqueConstraint(name = "unique_email", fields = "email", message = "邮箱已存在")
@Model.Meta(name = "rbac.user", label = "用户")
public class RbacUser extends Model {
    static Field name = Field.Char().label("名称").required();
    static Field login = Field.Char().label("账号").help("登录账号").required();
    static Field password = Field.Char().label("密码").help("登录密码").prefetch(false);
    static Field login_time = Field.DateTime().label("最后登录时间").related("log_ids.create_date");
    static Field email = Field.Char().label("邮箱");
    static Field mobile = Field.Char().label("手机号");
    static Field lang = Field.Selection(Selection.method("getLangs")).label("语言")
            .defaultValue(Default.method("getLangDefault"));
    static Field tz = Field.Selection(Selection.method("getTimezone")).label("时区")
            .defaultValue(Default.method("getTimezoneDefault"));
    static Field log_ids = Field.One2many("rbac.user.log", "create_uid");
    static Field active = Field.Boolean().label("是否有效").defaultValue(true);
    static Field role_ids = Field.Many2many("rbac.role", "rbac_role_user", "user", "role");
    static Field company_id = Field.Many2one("res.company").label("主公司").ondelete(DeleteMode.Restrict);
    static Field company_ids = Field.Many2many("res.company", "res_company_users_rel", "user", "company").label("所有公司")
            .help("用户有权访问的所有公司");

    @SuppressWarnings("unchecked")
    public Map<String, String> getLangs(Records rec) {
        return (Map<String, String>) rec.getEnv().get("res.lang").call("getInstalled");
    }

    public String getLangDefault(Records rec) {
        return rec.getEnv().getLang();
    }

    public String getTimezoneDefault(Records rec) {
        return rec.getEnv().getTimezone();
    }

    /**
     * 获取所有时区
     * 
     * @param res
     * @return
     */
    public Map<String, String> getTimezone(Records res) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String tz : TimeZone.getAvailableIDs()) { // Arrays.stream(TimeZone.getAvailableIDs()).sorted().toArray(String[]::new))
            map.put(tz, String.format("%s - (%s)", TimeZone.getTimeZone(tz).getDisplayName(), tz));
        }
        return map;
    }

    /**
     * 账号验证
     * 
     * @param rec
     */
    @Model.Constrains("login")
    public void checkLogin(Records rec) {
        for (Records r : rec) {
            String login = (String) r.get("login");
            if (login.contains("/")) {
                throw new ValidationException(rec.l10n("账号不能包含/"));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> read(Records rec, Collection<String> fields) {
        List<Map<String, Object>> datas = (List<Map<String, Object>>) rec.callSuper(RbacUser.class, "read", fields);
        for (Map<String, Object> data : datas) {
            if (data.containsKey("password")) {
                data.put("password", "********");
            }
        }
        return datas;
    }

    /**
     * 密码登录
     * 
     * @param rec
     * @param login     账号
     * @param password  密码
     * @param remember  是否记住密码，是：7天，否：8小时
     * @param userAgent
     * @return
     */
    public Map<String, Object> login(Records rec, String login, String password, boolean remember,
            Map<String, Object> userAgent) {
        Criteria criteria = Criteria.equal("active", true).and(
                Criteria.equal("login", login).or(Criteria.equal("mobile", login), Criteria.equal("email", login)));
        Records user = rec.find(criteria, 0, 1, null);
        RbacSecurity security = rec.getEnv().get("rbac.security").as(RbacSecurity.class);
        if (!user.any()) {
            security.loginFaild(security.getRecords(), null, userAgent);
            throw new AccessException(rec.l10n("账号[%s]不存在", login), SecurityCode.LOGIN_NOT_FOUND);
        }
        security.beforeLogin(security.getRecords(), user.getId(), userAgent);
        user = user.withUser(user.getId());
        checkCredentials(user, password, userAgent);
        updateLastLogin(user);
        security.loginSuccess(security.getRecords(), user.getId(), userAgent);
        // remember ? 7 days : 8 hours
        String uuid = (String) rec.getEnv().get("rbac.token").call("updateToken", user.getId(), remember ? 10080 : 480);
        KvMap map = new KvMap(3)
                .set("login", user.get("login"))
                .set("name", user.get("name"))
                .set("lang", user.get("lang"))
                .set("tz", user.get("tz"))
                .set("token", uuid)
                .set("id", user.getId());
        return map;
    }

    /**
     * 登出，清理令牌
     * 
     * @param rec
     * @return
     */
    @Model.ServiceMethod(label = "退出登录", auth = Constants.ANONYMOUS)
    public Object logout(Records rec) {
        rec.getEnv().get("rbac.token").call("removeToken", rec.getEnv().getUserId());
        return true;
    }

    /**
     * 读取个人账号信息
     * 
     * @param rec
     * @param fields
     * @return
     */
    @Model.ServiceMethod(label = "读取个人账号信息", auth = Constants.ANONYMOUS, records = false)
    public Map<String, Object> getPersonal(Records rec, Collection<String> fields) {
        return rec.getEnv().getUser().read(fields).get(0);
    }

    /**
     * 更新个人账号信息
     * 
     * @param rec
     * @param values
     * @return
     */
    @Model.ServiceMethod(label = "更新个人账号信息", auth = Constants.ANONYMOUS, records = false)
    public void updatePersonal(Records rec, Map<String, Object> values) {
        rec.getEnv().getUser().update(values);
    }

    /**
     * 更新最后登录时间
     * 
     * @param rec
     */
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

    /**
     * 修改密码
     * 
     * @param rec
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @Model.ServiceMethod(label = "修改密码", records = false, auth = Constants.ANONYMOUS)
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

    /**
     * 重置密码
     * 
     * @param rec
     * @return
     */
    @Model.ServiceMethod(label = "重置密码")
    public Object resetPassword(Records rec) {
        if (!rec.getEnv().isAdmin()) {
            throw new ValidationException("非管理员角色不能重置密码");
        }
        rec.update(new KvMap(1).set("password", "888888"));
        return Action.reload("保存成功");
    }

    @Override
    @SuppressWarnings("unchecked")
    @Model.ServiceMethod(auth = Constants.ANONYMOUS, label = "获取关联模型的数据", doc = "读取关联模型的记录")
    public Map<String, Object> searchRelated(Records rec, String relatedField, Map<String, Object> options) {
        return (Map<String, Object>) rec.callSuper(RbacUser.class, "searchRelated", relatedField, options);
    }

    @Model.ServiceMethod(auth = Constants.ANONYMOUS, label = "获取用户的公司", doc = "获取当前登录用户的公司")
    public List<Map<String, Object>> getUserCompanies(Records rec) {
        Records user = rec.getEnv().getUser();
        Records companies = user.getRel("company_ids");
        if (companies.any()) {
            List<Map<String, Object>> result = companies.read(Arrays.asList("present"));
            Records main = user.getRel("company_id");
            if (!main.any()) {
                user.set("company_id", companies.getIds()[0]);
            }
            String mainId = user.getRel("company_id").getId();
            for (Map<String, Object> row : result) {
                if (mainId.equals(row.get("id"))) {
                    row.put("main", true);
                    break;
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    @Model.ServiceMethod(auth = Constants.ANONYMOUS, label = "更新用户的主公司", doc = "更新当前登录用户的主公司")
    public void updateUserCompany(Records rec, String companyId) {
        Records user = rec.getEnv().getUser();
        Set<String> ids = SetUtils.hashSet(user.getRel("company_ids").getIds());
        if (!ids.contains(companyId)) {
            throw new ValidationException(rec.l10n("用户没有该公司的权限"));
        }
        user.set("company_id", companyId);
    }
}