package org.jdoo.base.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jdoo.*;
import org.jdoo.core.BaseService;
import org.jdoo.core.Constants;
import org.jdoo.core.Environment;
import org.jdoo.core.MetaField;
import org.jdoo.core.MetaModel;
import org.jdoo.core.Registry;
import org.jdoo.data.Cursor;

/**
 * 权限码，包含服务、字段、自定的权限码
 * 
 * @author lrz
 */
@Model.Meta(name = "rbac.permission", label = "权限码")
@Model.UniqueConstraint(name = "unique_model_auth_type", fields = { "auth", "model", "type" })
public class RbacPermission extends Model {
    static Field name = Field.Char().label("名称").required();
    static Field auth = Field.Char().label("权限码/字段").required();
    static Field model = Field.Char().label("模型").required();
    static Field role_ids = Field.Many2many("rbac.role", "rbac_role_permission", "permission", "role");
    static Field type = Field.Selection().label("类型").selection(new HashMap<String, String>() {
        {
            put("service", "服务");
            put("field", "字段");
        }
    }).defaultValue("service").required();
    static Field origin = Field.Selection().label("来源").selection(new HashMap<String, String>() {
        {
            put("base", "内置");
            put("manual", "自定义");
        }
    }).defaultValue("manual").readonly();
    static Field active = Field.Boolean().label("是否有效").defaultValue(true);

    /**
     * 更新所有模型的权限码清单
     * 
     * @param rec
     * @return
     */
    @ServiceMethod(label = "更新", records = false)
    public Object refresh(Records rec) {
        Environment env = rec.getEnv();
        Cursor cr = env.getCursor();

        String sql = "SELECT id, auth, model, type FROM rbac_permission WHERE origin='base'";
        cr.execute(sql);
        List<Map<String, Object>> datas = cr.fetchMapAll();
        Map<String, List<Map<String, Object>>> modelPermission = new HashMap<>();
        for (Map<String, Object> data : datas) {
            String model = (String) data.get("model");
            List<Map<String, Object>> lines = modelPermission.get(model);
            if (lines == null) {
                lines = new ArrayList<>();
                modelPermission.put(model, lines);
            }
            lines.add(data);
        }

        sql = "UPDATE rbac_permission SET origin='deleted' WHERE origin='base'";
        cr.execute(sql);

        for (MetaModel m : env.getRegistry().getModels().values()) {
            Collection<BaseService> svcs = m.getService().values();
            if (m.isAbstract()) {
                continue;
            }
            List<Map<String, Object>> data = modelPermission.getOrDefault(m.getName(), new ArrayList<>());
            Map<String, Map<String, Object>> services = new HashMap<>();
            Map<String, Map<String, Object>> fields = new HashMap<>();
            for (Map<String, Object> map : data) {
                String auth = (String) map.get("auth");
                String type = (String) map.get("type");
                if ("service".equals(type)) {
                    services.put(auth, map);
                } else {
                    fields.put(auth, map);
                }
            }
            for (MetaField field : m.getFields().values()) {
                if (field.isAuth()) {
                    String fname = field.getName();
                    Map<String, Object> map = fields.get(fname);
                    String label = field.getLabel();
                    if (StringUtils.isEmpty(label)) {
                        label = fname;
                    }
                    Map<String, Object> values = new HashMap<>();
                    values.put("name", label);
                    values.put("auth", fname);
                    values.put("model", m.getName());
                    values.put("type", "field");
                    values.put("origin", "base");
                    if (map == null) {
                        create(rec, values);
                    } else {
                        update(rec.browse((String) map.get("id")), values);
                    }
                }
            }
            Set<String> auths = new HashSet<>();
            for (BaseService svc : svcs) {
                String auth = svc.getAuth();
                if (StringUtils.isEmpty(auth)) {
                    auth = svc.getName();
                }
                if (auth.equals(svc.getName())) {
                    auths.add(auth);
                }
            }
            for (BaseService svc : svcs) {
                String auth = svc.getAuth();
                if (Constants.ANONYMOUS.equals(auth)) {
                    continue;
                }
                if (StringUtils.isEmpty(auth)) {
                    auth = svc.getName();
                }
                if (!auth.equals(svc.getName()) && auths.contains(auth)) {
                    continue;
                }
                if (!"read".equals(auth) || StringUtils.isEmpty(m.getAuthModel())) {
                    Map<String, Object> map = services.get(auth);
                    String label = svc.getLabel();
                    if (StringUtils.isEmpty(label)) {
                        label = auth;
                    }
                    Map<String, Object> values = new HashMap<>();
                    values.put("name", label);
                    values.put("auth", auth);
                    values.put("model", m.getName());
                    values.put("type", "service");
                    values.put("origin", "base");
                    if (map == null) {
                        create(rec, values);
                    } else {
                        update(rec.browse((String) map.get("id")), values);
                    }
                }
            }
        }
        flush(rec);
        sql = "DELETE FROM rbac_role_permission WHERE permission in (SELECT id FROM rbac_permission WHERE origin='deleted')";
        cr.execute(sql);
        sql = "DELETE FROM rbac_permission WHERE origin='deleted'";
        cr.execute(sql);
        return Action.reload("更新成功");
    }

    /**
     * 根据当前用户加载指定模型的已授权权限码
     * 
     * @param rec
     * @param model
     * @return
     */
    public List<String> loadModelAuths(Records rec, String model) {
        Cursor cr = rec.getEnv().getCursor();
        String sql = "SELECT distinct auth FROM rbac_permission p"
                + " JOIN rbac_role_permission r ON p.id = r.permission"
                + " JOIN rbac_role_user u ON r.role=u.role"
                + " WHERE p.model=%s AND u." + cr.quote("user") + "=%s AND p.type='service' AND p.active=%s";
        cr.execute(sql, Arrays.asList(model, rec.getEnv().getUserId(), true));
        return cr.fetchAll().stream().map(o -> (String) o[0]).collect(Collectors.toList());
    }

    /**
     * 初始化模型需要授权的字段
     * 
     * @param rec
     */
    public void initModelAuthFields(Records rec) {
        Cursor cr = rec.getEnv().getCursor();
        String sql = "SELECT auth,model FROM rbac_permission p WHERE p.active=%s AND p.type='field'";
        cr.execute(sql, Arrays.asList(true));
        Map<String, List<String>> map = new HashMap<>();
        for (Object[] row : cr.fetchAll()) {
            String model = (String) row[1];
            List<String> fields = (List<String>) map.get(model);
            if (fields == null) {
                fields = new ArrayList<>();
                map.put(model, fields);
            }
            fields.add((String) row[0]);
        }
        Registry reg = rec.getEnv().getRegistry();
        for (Entry<String, List<String>> row : map.entrySet()) {
            String model = row.getKey();
            if (reg.contains(model)) {
                reg.get(model).setAuthFields(row.getValue());
            }
        }
    }

    /**
     * 加载当前用户没有权限访问的模型的字段
     * 
     * @param rec
     * @param model
     * @return
     */
    public Set<String> loadModelDenyFields(Records rec, String model) {
        Set<String> result = rec.getEnv().isAdmin() ? Collections.emptySet()
                : new HashSet<String>(rec.getEnv().getRegistry().get(model).getAuthFields());
        if (result.size() > 0) {
            Cursor cr = rec.getEnv().getCursor();
            String sql = "SELECT auth FROM rbac_permission p"
                    + " JOIN rbac_role_permission rp ON rp.permission=p.id"
                    + " JOIN rbac_role_user ru ON ru.role=rp.role"
                    + " JOIN rbac_role r ON r.id=ru.role"
                    + " WHERE p.type='field' AND ru." + cr.quote("user") + "=%s AND p.active=%s AND r.active=%s";
            cr.execute(sql, Arrays.asList(rec.getEnv().getUserId(), true, true));
            List<String> allow = cr.fetchAll().stream().map(o -> (String) o[0]).collect(Collectors.toList());
            result.removeAll(allow);
        }
        return result;
    }

    /**
     * 加载有read权限的模型
     * 
     * @param rec
     * @return
     */
    public List<String> loadAllowReadModels(Records rec) {
        Cursor cr = rec.getEnv().getCursor();
        String sql = "SELECT DISTINCT model FROM rbac_permission p"
                + " JOIN rbac_role_permission rp on p.id = rp.permission"
                + " JOIN rbac_role_user u on rp.role=u.role"
                + " JOIN rbac_role r on rp.role=r.id"
                + " WHERE u." + cr.quote("user") + "=%s AND r.active=%s AND p.auth='read' AND p.active=%s";
        cr.execute(sql, Arrays.asList(rec.getEnv().getUserId(), true, true));
        return cr.fetchAll().stream().map(o -> (String) o[0]).collect(Collectors.toList());
    }
}
