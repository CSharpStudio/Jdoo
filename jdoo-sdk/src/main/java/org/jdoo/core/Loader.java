package org.jdoo.core;

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

import org.jdoo.Manifest;
import org.jdoo.Records;
import org.jdoo.data.Cursor;
import org.jdoo.data.Database;
import org.jdoo.utils.ManifestUtils;
import org.jdoo.utils.ObjectUtils;
import org.jdoo.utils.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * 加载器
 * 
 * @author lrz
 */
public class Loader {
    static Loader instance;
    private Logger logger = LoggerFactory.getLogger(Loader.class);

    public static void setLoader(Loader loader) {
        instance = loader;
    }

    public static Loader getLoader() {
        if (instance == null) {
            instance = new Loader();
        }
        return instance;
    }

    ModelBuilder builder = ModelBuilder.getBuilder();

    @SuppressWarnings("unchecked")
    public void loadModules(Database db, Registry registry) {
        builder.buildBaseModel(registry);

        try (Cursor cr = db.openCursor()) {
            boolean init = cr.getSqlDialect().tableExists(cr, "ir_module");
            if (!init) {
                init(cr);
            }
            // 强制更新
            boolean forceUpdate = ObjectUtils.toBoolean(PropertiesUtils.getProperty("forceUpdate"));
            if (forceUpdate) {
                cr.execute("UPDATE ir_module SET state='installing' WHERE state='installed'");
            }

            // load installed and installing modules
            List<Map<String, Object>> newModules = loadModuleGraph(cr, registry);
            registry.setupModels(cr);
            Environment env = new Environment(registry, cr, Constants.SYSTEM_USER, Collections.emptyMap());
            Records http = env.get("ir.http");
            // load data of new modules
            for (Map<String, Object> m : newModules) {
                Collection<String> models = (Collection<String>) m.get("models");
                registry.initModels(env, models, (String) m.get("name"), false);
                String pkg = (String) m.get("package_info");
                env.get("ir.model.data").call("loadData", pkg);
            }
            if (newModules.size() > 0) {
                env.get("rbac.permission").call("refresh");
            }
            cr.execute("UPDATE ir_module SET state='installed' WHERE state='installing'");
            // remove modules
            cr.execute("SELECT id,name,package_info FROM ir_module WHERE state ='removing'");
            List<Map<String, Object>> rows = cr.fetchMapAll();
            if (rows.size() > 0) {
                // List<String> toRemoveModules = rows.stream().map(m -> (String) m.get("name"))
                // .collect(Collectors.toList());
                // env.get("ir.model.data").call("removeData", toRemoveModules);
                rows.stream().map(m -> (String) m.get("package_info")).forEach(pkg -> {
                    Manifest manifest = ManifestUtils.getManifest(pkg);
                    http.call("unregisterControllers", manifest);
                });
                cr.execute("UPDATE ir_module SET state='installable' WHERE state='removing'");
            }
            env.get("rbac.permission").call("initModelAuthFields");
            env.get("base").flush();
            registry.getModules().forEach(manifest -> {
                http.call("registerControllers", manifest);
            });
            cr.commit();
        }
        registry.loaded = true;
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> loadModuleGraph(Cursor cr, Registry registry) {
        cr.execute("SELECT id,name,package_info,state FROM ir_module WHERE state in ('installed', 'installing')");
        List<Map<String, Object>> datas = cr.fetchMapAll();
        Map<String, Map<String, Object>> nameModules = new HashMap<>(datas.size());
        Set<String> moduleIds = new HashSet<>();
        for (Map<String, Object> data : datas) {
            data.put("deps", new HashSet<String>());
            moduleIds.add((String) data.get("id"));
            nameModules.put((String) data.get("name"), data);
        }
        cr.execute(
                "SELECT d.name,m.name as module FROM ir_module_dependency d JOIN ir_module m on d.module_id=m.id WHERE module_id in %s",
                Arrays.asList(moduleIds));
        List<Map<String, Object>> deps = cr.fetchMapAll();
        for (Map<String, Object> dep : deps) {
            String moduleName = (String) dep.get("module");
            Map<String, Object> module = nameModules.get(moduleName);
            if (module != null) {
                Set<String> set = (Set<String>) module.get("deps");
                set.add((String) dep.get("name"));
            }
        }
        Manifest manifest = ManifestUtils.getManifest("org.jdoo.base");
        builder.buildModule(registry, manifest);
        registry.modules.put("base", manifest);

        List<Map<String, Object>> newModules = new ArrayList<>();
        for (Entry<String, Map<String, Object>> e : nameModules.entrySet()) {
            newModules.addAll(buildModule(cr, registry, e.getKey(), nameModules));
        }
        return newModules;
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> buildModule(Cursor cr, Registry registry, String module,
            Map<String, Map<String, Object>> nameModules) {
        List<Map<String, Object>> newModules = new ArrayList<>();
        if (!registry.containsModule(module)) {
            Map<String, Object> data = nameModules.get(module);
            if (data == null) {
                logger.error(String.format("模块%s未安装", module));
            } else {
                Set<String> deps = (Set<String>) data.get("deps");
                for (String dep : deps) {
                    if (StringUtils.isNotEmpty(dep)) {
                        newModules.addAll(buildModule(cr, registry, dep, nameModules));
                    }
                }
                if ("installing".equals(data.get("state"))) {
                    newModules.add(data);
                }
                String pkg = (String) data.get("package_info");
                if (StringUtils.isNotBlank(pkg)) {
                    Manifest manifest = ManifestUtils.getManifest(pkg);
                    data.put("models", builder.buildModule(registry, manifest));
                    registry.addModule(module, manifest);
                } else {
                    // TODO build without package
                }
            }
        }
        return newModules;
    }

    void init(Cursor cr) {
        Registry registry = new Registry(null);
        builder.buildBaseModel(registry);
        String pkg = "org.jdoo.base";
        Manifest manifest = ManifestUtils.getManifest(pkg);
        builder.buildModule(registry, manifest);
        registry.setupModels(cr);
        Environment env = new Environment(registry, cr, "__system__", Collections.emptyMap());
        registry.initModels(env, registry.getModels().keySet(), "base", true);

        env.get("ir.model.data").call("loadData", pkg);
        env.get("rbac.permission").call("refresh");
        env.get("base").flush();
    }
}
