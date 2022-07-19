package org.jdoo.base.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.jdoo.core.MetaModel;
import org.jdoo.data.Cursor;
import org.jdoo.util.KvMap;

/**
 * 角色
 * 
 * @author lrz
 */
@Model.Meta(name = "rbac.permission", description = "权限")
public class Permission extends Model {
    static Field name = Field.Char().label("名称");
    static Field auth = Field.Char().label("权限");
    static Field model = Field.Char().label("模型");
    static Field role_ids = Field.Many2many("rbac.role", "rbac_role_permission", "permission", "role");
    static Field active = Field.Boolean();

    @ServiceMethod(label = "更新", records = false)
    public Object refresh(Records rec) {
        Environment env = rec.getEnv();
        Cursor cr = env.getCursor();

        String sql = "SELECT id, auth, model FROM rbac_permission WHERE active=%s";
        cr.execute(sql, Arrays.asList(true));
        List<KvMap> datas = cr.fetchMapAll();
        Map<String, List<KvMap>> modelPermission = new HashMap<>();
        for (KvMap data : datas) {
            String model = (String) data.get("model");
            List<KvMap> lines = modelPermission.get(model);
            if (lines == null) {
                lines = new ArrayList<>();
                modelPermission.put(model, lines);
            }
            lines.add(data);
        }

        sql = "UPDATE rbac_permission SET active=%s WHERE active!=%s";
        cr.execute(sql, Arrays.asList(false, false));

        for (MetaModel m : env.getRegistry().getModels().values()) {
            Collection<BaseService> svcs = m.getService().values();
            if (svcs.size() == 0 || m.isAbstract()) {
                continue;
            }
            List<KvMap> data = modelPermission.getOrDefault(m.getName(), new ArrayList<>());
            Map<String, KvMap> permission = new HashMap<>();
            for (KvMap d : data) {
                String auth = (String) d.get("auth");
                permission.put(auth, d);
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
                KvMap d = permission.get(auth);
                String label = svc.getLabel();
                if (StringUtils.isEmpty(label)) {
                    label = auth;
                }
                Map<String, Object> values = new HashMap<>();
                values.put("name", label);
                values.put("auth", auth);
                values.put("model", m.getName());
                values.put("active", true);
                if (d == null) {
                    create(rec, values);
                } else {
                    update(rec.browse((String) d.get("id")), values);
                }
            }
        }
        flush(rec);
        sql = "DELETE FROM rbac_role_permission WHERE permission in (SELECT id FROM rbac_permission WHERE active=%s)";
        cr.execute(sql, Arrays.asList(false));
        sql = "DELETE FROM rbac_permission WHERE active=%s";
        cr.execute(sql, Arrays.asList(false));
        return Action.reload("更新成功");
    }

    public List<String> loadModelAuths(Records rec, String model) {
        Cursor cr = rec.getEnv().getCursor();
        String sql = "SELECT distinct auth FROM jdoo.rbac_permission p"
                + " join rbac_role_permission r on p.id = r.permission"
                + " join rbac_role_user u on r.role=u.role"
                + " where p.model=%s and u.user=%s";
        cr.execute(sql, Arrays.asList(model, rec.getEnv().getUserId()));
        return cr.fetchAll().stream().map(o -> (String) o[0]).collect(Collectors.toList());
    }

    public List<String> loadAuthModels(Records rec) {
        Cursor cr = rec.getEnv().getCursor();
        String sql = "SELECT DISTINCT model FROM rbac_permission p"
                + " JOIN rbac_role_permission r on p.id = r.permission"
                + " JOIN rbac_role_user u on r.role=u.role"
                + " WHERE u.user=%s";
        cr.execute(sql, Arrays.asList(rec.getEnv().getUserId()));
        return cr.fetchAll().stream().map(o -> (String) o[0]).collect(Collectors.toList());
    }

    @ServiceMethod(label = "加载模型权限清单", records = false)
    public List<Object> loadModelPermission(Records rec) {
        List<Object> result = new ArrayList<>();

        Records menus = rec.getEnv().get("ir.ui.menu").find(new Criteria(), 0, 0, null);
        Records permissions = rec.find(new Criteria(), 0, 0, null);

        Set<String> models = new HashSet<>();
        for (Records p : permissions) {
            models.add((String) p.get("model"));
        }
        Map<String, List<KvMap>> apps = new LinkedHashMap<>();
        for (Records menu : menus) {
            if (!((Records) menu.get("parent_id")).any()) {
                apps.put((String) menu.get("name"), new ArrayList<>());
            }
        }
        for (Records menu : menus) {
            String model = (String) menu.get("model");
            if (StringUtils.isNotEmpty(model) && models.remove(model)) {
                Records ps = permissions.filter(p -> model.equals(p.get("model")));
                Records root = getRoot(menu);
                String app = (String) root.get("name");
                List<KvMap> appModels = apps.get(app);
                KvMap kv = new KvMap();
                appModels.add(kv);
                kv.set("menu", menu.get("name"));
                kv.set("model", menu.get("model"));
                List<Object> permissionList = new ArrayList<>();
                for (Records r : ps) {
                    KvMap p = new KvMap();
                    p.set("name", r.get("name"));
                    p.set("auth", r.get("auth"));
                    p.set("id", r.getId());
                    p.set("role_ids", ((Records) r.get("role_ids")).getIds());
                    permissionList.add(p);
                }
                kv.set("permissions", permissionList);
            }
        }
        List<KvMap> nomenu = new ArrayList<>();
        Records ms = rec.getEnv().get("ir.model").find(Criteria.in("model", models), 0, 0, null);
        for (String model : models) {
            Records ps = permissions.filter(p -> model.equals(p.get("model")));
            KvMap kv = new KvMap();
            kv.set("menu", ms.filter(p -> model.equals(p.get("model"))).get("name"));
            kv.set("model", model);
            nomenu.add(kv);
            List<Object> permissionList = new ArrayList<>();
            for (Records r : ps) {
                KvMap p = new KvMap();
                p.set("name", r.get("name"));
                p.set("auth", r.get("auth"));
                p.set("id", r.getId());
                p.set("role_ids", ((Records) r.get("role_ids")).getIds());
                permissionList.add(p);
            }
            kv.set("permissions", permissionList);
        }
        for (Entry<String, List<KvMap>> app : apps.entrySet()) {
            result.add(new KvMap().set("app", app.getKey()).set("models", app.getValue()));
        }
        if (nomenu.size() > 0) {
            result.add(new KvMap().set("app", "-").set("models", nomenu));
        }

        return result;
    }

    Records getRoot(Records menu) {
        Records parent = (Records) menu.get("parent_id");
        if (parent.any()) {
            return getRoot(parent);
        }
        return menu;
    }
}
