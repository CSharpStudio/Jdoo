package org.jdoo.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jdoo.Manifest;
import org.jdoo.Records;
import org.jdoo.data.Cursor;
import org.jdoo.data.SqlDialect;
import org.jdoo.exceptions.ModelException;
import org.jdoo.tenants.Tenant;
import org.jdoo.util.KvMap;
import org.jdoo.util.Tuple;
import org.jdoo.util.Tuple4;

/**
 * 模型注册表
 * 
 * @author lrz
 */
public class Registry {
    Map<String, Manifest> modules = new LinkedHashMap<>();
    Map<String, MetaModel> map = new HashMap<>();
    List<Consumer<Environment>> postInit;
    Tenant tenant;
    boolean loaded = false;
    Map<Tuple<String, String>, KvMap> foreignKeys;

    /**
     * 添加外键信息，在所有数据表更新后，再执行外键的更新
     * 
     * @param table1
     * @param column1
     * @param table2
     * @param column2
     * @param ondelete
     * @param model
     * @param module
     * @param force
     */
    public void addForeignKey(String table1, String column1, String table2, String column2, String ondelete,
            String model, String module, boolean force) {
        if (foreignKeys == null) {
            foreignKeys = new HashMap<>();
        }
        Tuple<String, String> key = new Tuple<>(table1, column1);
        KvMap map = new KvMap()
                .set("table2", table2)
                .set("column2", column2)
                .set("ondelete", ondelete)
                .set("model", model)
                .set("module", module);
        if (force) {
            foreignKeys.put(key, map);
        } else {
            foreignKeys.putIfAbsent(key, map);
        }
    }

    /**
     * 注册表是否加载完成
     * 
     * @return
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * 添加初始化操作
     * 
     * @param init
     */
    public void addPostInit(Consumer<Environment> init) {
        if (postInit == null) {
            postInit = new ArrayList<>();
        }
        postInit.add(init);
    }

    /**
     * 获取租户，注册表与租户一一对应，用于隔离每个租户安装的不同应用，
     * 
     * @return
     */
    public Tenant getTenant() {
        return tenant;
    }

    /**
     * 构建注册表实例
     * 
     * @param tenant 注册表对应的租户
     */
    public Registry(Tenant tenant) {
        this.tenant = tenant;
    }

    void put(String model, MetaModel meta) {
        map.put(model, meta);
    }

    /**
     * 获取所有元模型
     * 
     * @return
     */
    public Map<String, MetaModel> getModels() {
        return map;
    }

    /**
     * 获取元模型
     * 
     * @param model 模型名称
     * @return
     */
    public MetaModel get(String model) {
        MetaModel m = map.get(model);
        if (m == null) {
            throw new ModelException("模型[" + model + "]未注册");
        }
        return m;
    }

    /**
     * 判断是否包含元模型
     * 
     * @param model 模型名称
     * @return
     */
    public boolean contains(String model) {
        return map.containsKey(model);
    }

    /**
     * 添加模块
     * 
     * @param module
     * @param manifest
     */
    public void addModule(String module, Manifest manifest) {
        modules.put(module, manifest);
    }

    /**
     * 是否已经加载模块
     * 
     * @param module
     * @return
     */
    public boolean containsModule(String module) {
        return modules.containsKey(module);
    }

    /**
     * 获取所有模块
     */
    public Collection<Manifest> getModules() {
        return modules.values();
    }

    /**
     * 获取模块清单信息
     * 
     * @param module
     * @return
     */
    public Manifest getModule(String module) {
        return modules.get(module);
    }

    void addUserRoot(Environment env) {
        Records root = env.findRef("base.user_root");
        if (root == null) {
            root = env.get("rbac.user").create(new HashMap<String, Object>() {
                {
                    put("id", "__system__");
                    put("login", "__system__");
                    put("name", "系统管理员");
                    put("tz", "Asia/Shanghai");
                    put("lang", "zh_CN");
                    put("active", false);
                }
            });
            env.get("ir.model.data").create(new KvMap()
                    .set("name", "user_root")
                    .set("module", "base")
                    .set("model", "ir.ui.view")
                    .set("res_id", root.getId()));
        }
    }

    /**
     * 初始化模型。auto=true的模型自动更新数据库表
     * 
     * @param cr     数据库游标
     * @param models 需要初始化的模型列表
     * @param init   是否初始化数据，第一次创建数据库时，创建根用户数据并更新模块信息
     */
    public Environment initModels(Environment env, Collection<String> models, String module, boolean init) {
        for (String model : models) {
            MetaModel m = get(model);
            m.autoInit(env);
        }
        if (init) {
            addUserRoot(env);
            env.get("ir.module").call("updateModules");
        }
        env.get("ir.model").call("reflectModels", models, module);
        env.get("ir.model.field").call("reflectFields", models, module);
        // env.get("ir.model.field.selection").call("reflectFields", models);
        env.get("ir.model.constraint").call("reflectModels", models, module);

        if (postInit != null) {
            for (Consumer<Environment> c : postInit) {
                c.accept(env);
            }
        }
        checkForeignKeys(env);
        for (Manifest manifest : modules.values()) {
            if (StringUtils.isNotEmpty(manifest.postInit())) {
                String[] parts = manifest.postInit().split("::");
                if (parts.length > 1) {
                    env.get(parts[0]).call(parts[1]);
                }
            }
        }

        if (foreignKeys != null) {
            foreignKeys.clear();
            foreignKeys = null;
        }
        if (postInit != null) {
            postInit.clear();
            postInit = null;
        }
        return env;
    }

    void checkForeignKeys(Environment env) {
        if (foreignKeys == null || foreignKeys.size() == 0) {
            return;
        }
        Cursor cr = env.getCursor();
        Collection<String> tables = foreignKeys.keySet().stream().map(t -> t.getItem1()).collect(Collectors.toSet());
        SqlDialect sd = cr.getSqlDialect();
        List<Object[]> rows = sd.getForeignKeys(cr, tables);
        Map<Tuple<String, String>, Tuple4<String, String, String, String>> existing = new HashMap<>();
        for (Object[] row : rows) {
            existing.put(new Tuple<>((String) row[1], (String) row[2]),
                    new Tuple4<>((String) row[0], (String) row[3], (String) row[4], (String) row[5]));
        }
        for (Entry<Tuple<String, String>, KvMap> kv : foreignKeys.entrySet()) {
            String table1 = kv.getKey().getItem1();
            String column1 = kv.getKey().getItem2();
            String table2 = (String) kv.getValue().get("table2");
            String column2 = (String) kv.getValue().get("column2");
            String ondelete = (String) kv.getValue().get("ondelete");
            String model = (String) kv.getValue().get("model");
            String module = (String) kv.getValue().get("module");
            Tuple4<String, String, String, String> spec = existing.get(kv.getKey());
            if (spec == null) {
                String fk = sd.addForeignKey(cr, table1, column1, table2, column2, ondelete);
                env.get("ir.model.constraint").call("reflectConstraint", fk, "f", ondelete, model, model, module);
            } else if (!table2.equals(spec.getItem2()) || !column2.equals(spec.getItem3())
                    || !ondelete.equals(spec.getItem4())) {
                sd.dropConstraint(cr, table1, spec.getItem1());
                String fk = sd.addForeignKey(cr, table1, column1, table2, column2, ondelete);
                env.get("ir.model.constraint").call("reflectConstraint", fk, "f", ondelete, model, model, module);
            }
        }
    }

    /**
     * 设置模型，在初始化前完成模型的设置
     * 
     * @param cr
     */
    public void setupModels(Cursor cr) {
        Environment env = new Environment(this, cr, Constants.SYSTEM_USER, new HashMap<String, Object>(0));
        // TODO reset

        // TODO add manual models
        if (!modules.isEmpty()) {
            env.get("ir.model").call("addManualModels");
        }

        Collection<MetaModel> models = getModels().values();
        for (MetaModel model : models) {
            model.setupBase(env);
        }

        for (MetaModel model : models) {
            model.setupFields();
        }

        for (MetaModel model : models) {
            model.setupComplete(env);
        }
    }
}
