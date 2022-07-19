package org.jdoo.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdoo.data.Cursor;
import org.jdoo.exceptions.ModelException;
import org.jdoo.tenants.Tenant;

/**
 * 模型注册表
 * 
 * @author lrz
 */
public class Registry {
    Map<String, MetaModel> map = new HashMap<>();
    Set<String> initModels = new HashSet<>();
    Tenant tenant;
    boolean loaded = false;
    
    public Tenant getTenant() {
        return tenant;
    }

    public Registry(Tenant tenant) {
        this.tenant = tenant;
    }

    void put(String model, MetaModel meta) {
        map.put(model, meta);
    }

    public Map<String, MetaModel> getModels() {
        return map;
    }

    public MetaModel get(String model) {
        MetaModel m = map.get(model);
        if (m == null) {
            throw new ModelException("模型[" + model + "]未注册");
        }
        return m;
    }

    public boolean contains(String model) {
        return map.containsKey(model);
    }

    public void initModels(Cursor cr, Collection<String> models) {
        Environment env = new Environment(this, cr, Constants.SUPERUSER_ID, new HashMap<String, Object>(0));
        for (String model : models) {
            MetaModel m = get(model);
            m.autoInit(env);
        }
        env.get("ir.model").call("reflectModels", models);
        env.get("ir.model.field").call("reflectFields", models);
    }

    public void setupModels(Cursor cr) {
        Environment env = new Environment(this, cr, Constants.SUPERUSER_ID, new HashMap<String, Object>(0));
        // TODO reset

        // TODO add manual models
        if (!initModels.isEmpty()) {
            env.get("ir.model").call("addManualModels");
        }

        Collection<MetaModel> models = getModels().values();
        for (MetaModel model : models) {
            model.setupBase(env);
        }

        for (MetaModel model : models) {
            model.setupFields(env);
        }

        for (MetaModel model : models) {
            model.setupComplete(env);
        }
    }
}
