package jdoo.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import jdoo.data.Cursor;
import jdoo.data.Database;
import jdoo.util.DefaultDict;

public class Loader {
    static DefaultDict<String, List<Class<?>>> module_to_models = new DefaultDict<>(ArrayList.class);

    static {
        Package[] pkgs = Package.getPackages();
        for (Package pkg : pkgs) {
            Import imp = pkg.getAnnotation(Import.class);
            if (imp != null) {
                Manifest manifest = pkg.getAnnotation(Manifest.class);
                for (Class<?> clazz : imp.value()) {
                    module_to_models.get(manifest.name()).add(clazz);
                }
            }
        }
    }

    static ConcurrentHashMap<String, Registry> registries = new ConcurrentHashMap<String, Registry>();

    public static Registry getRegistry(String tenant) {
        if (!registries.containsKey(tenant)) {
            synchronized (Registry.class) {
                if (!registries.containsKey(tenant)) {
                    Database db = Database.get(tenant);
                    Registry registry = new Registry(tenant);
                    load_modules(db, registry);
                    registries.put(tenant, registry);
                    return registry;
                }
            }
        }
        return registries.get(tenant);
    }

    public static void load_modules(Database db, Registry registry) {
        // jdoo.web.__init__.init();
        // jdoo.base.__init__.init();

        // for (List<Class<?>> modules : module_to_models.values()) {
        // for (Class<?> clazz : modules) {
        // registry.register(clazz);
        // }
        // }

        try (Cursor cr = db.cursor()) {
            load_module_graph(cr, registry);

            registry.setup_models(cr);
            registry.init_models(cr);
            cr.commit();
        }

    }

    public static void load_module_graph(Cursor cr, Registry registry) {
        List<String> graph = Arrays.asList("base", "web");
        for (String $package : graph) {
            registry.load(cr, $package);
        }
    }
}
