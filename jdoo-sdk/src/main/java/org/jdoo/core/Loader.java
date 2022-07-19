package org.jdoo.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jdoo.Manifest;
import org.jdoo.data.Cursor;
import org.jdoo.data.Database;
import org.jdoo.exceptions.ValueException;
import org.jdoo.util.KvMap;
import org.jdoo.utils.ImportUtils;
import org.jdoo.utils.PathUtils;
import org.springframework.util.ClassUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 加载器
 * 
 * @author lrz
 */
public class Loader {
    static Loader instance;
    private Logger logger = LogManager.getLogger(Loader.class);

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

    public void loadModules(Database db, Registry registry) {
        builder.buildBaseModel(registry);

        try (Cursor cr = db.openCursor()) {
            boolean init = cr.getSqlDialect().tableExists(cr, "ir_module");
            if (!init) {
                init(cr);
            }
            List<KvMap> newModules = loadModuleGraph(cr, registry);
            registry.setupModels(cr);
            registry.initModels(cr, registry.getModels().keySet());

            Environment env = new Environment(registry, cr, Constants.SUPERUSER_ID, Collections.emptyMap());
            for (KvMap m : newModules) {
                String pkg = (String) m.get("package_info");
                loadData(env, pkg, getManifest(pkg));
            }
            env.get("base").flush();
            cr.execute("UPDATE ir_module SET state='installed' WHERE state='installing'");
            cr.commit();
        }
        registry.loaded = true;
    }

    List<KvMap> loadModuleGraph(Cursor cr, Registry registry) {
        List<KvMap> newModules = new ArrayList<>();
        cr.execute("SELECT id,name,package_info,state FROM ir_module WHERE state='installed' or state='installing'");
        List<KvMap> datas = cr.fetchMapAll();
        Map<String, KvMap> nameModules = new HashMap<>(datas.size());
        Map<String, Set<String>> dependency = new HashMap<>(datas.size());
        Set<String> moduleIds = new HashSet<>();
        for (KvMap data : datas) {
            moduleIds.add((String) data.get("id"));
            nameModules.put((String) data.get("name"), data);
            if ("installing".equals(data.get("state"))) {
                newModules.add(data);
            }
        }
        cr.execute(
                "SELECT d.name,m.name as module FROM ir_module_dependency d JOIN ir_module m on d.module_id=m.id WHERE module_id in %s",
                Arrays.asList(moduleIds));
        List<KvMap> deps = cr.fetchMapAll();
        for (KvMap dep : deps) {
            String module = (String) dep.get("module");
            Set<String> set = dependency.get(module);
            if (set == null) {
                set = new HashSet<>();
                dependency.put(module, set);
            }
            set.add((String) dep.get("name"));
        }

        builder.buildModule(registry, getManifest("org.jdoo.base"));
        registry.initModels.add("base");

        for (Entry<String, KvMap> e : nameModules.entrySet()) {
            buildModule(cr, registry, e.getKey(), nameModules, dependency);
        }

        return newModules;
    }

    void buildModule(Cursor cr, Registry registry, String module, Map<String, KvMap> nameModules,
            Map<String, Set<String>> dependency) {
        if (!registry.initModels.contains(module)) {
            Set<String> deps = dependency.get(module);
            if (deps != null) {
                for (String dep : deps) {
                    if (StringUtils.isNotEmpty(dep)) {
                        buildModule(cr, registry, dep, nameModules, dependency);
                    }
                }
            }
        }
        if (!registry.initModels.contains(module)) {
            KvMap data = nameModules.get(module);
            String pkg = (String) data.get("package_info");
            if (StringUtils.isNotBlank(pkg)) {
                builder.buildModule(registry, getManifest(pkg));
                registry.initModels.add(module);
            } else {
                // TODO build without package
            }
        }
    }

    public void loadData(Environment env, String packageName, Manifest manifest) {
        String[] files = manifest.data();
        for (String file : files) {
            if (StringUtils.isNoneBlank(file)) {
                String path = PathUtils.combine(packageName.replaceAll("\\.", "/"), file).toLowerCase();
                ClassLoader loader = ClassUtils.getDefaultClassLoader();
                InputStream input = loader.getResourceAsStream(path);
                if (input != null) {
                    if (path.endsWith(".xml")) {
                        ImportUtils.importXml(input, manifest.name(), env, (m, e) -> {
                            logger.warn(m, e);
                        });
                    } else if (path.endsWith(".csv")) {
                    }
                } else {
                    logger.warn("找不到文件:" + file);
                }
            }
        }
    }

    public void loadData(Environment env, String packageName) {
        Manifest manifest = getManifest(packageName);
        loadData(env, packageName, manifest);
    }

    Package getPackage(String module) {
        try {
            Class<?> clazz = Class.forName(module + ".package-info");
            return clazz.getPackage();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    Manifest getManifest(String packageName) {
        Package pkg = getPackage(packageName);
        if (pkg == null) {
            throw new ValueException("加载模块:[" + packageName + "]失败，请检查package-info");
        }
        Manifest manifest = pkg.getAnnotation(Manifest.class);
        if (manifest == null) {
            throw new ValueException("包[" + packageName + "]未定义Manifest");
        }
        return manifest;
    }

    void init(Cursor cr) {
        Registry registry = new Registry(null);
        builder.buildBaseModel(registry);
        String pkg = "org.jdoo.base";
        Manifest manifest = getManifest(pkg);
        builder.buildModule(registry, manifest);
        registry.setupModels(cr);
        registry.initModels(cr, registry.getModels().keySet());

        Environment env = new Environment(registry, cr, Constants.SUPERUSER_ID, Collections.emptyMap());
        env.get("ir.module").call("updateModules");

        loadData(env, pkg, manifest);
        env.get("base").flush();
    }
}
