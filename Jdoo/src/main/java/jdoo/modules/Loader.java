package jdoo.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdoo.data.Cursor;
import jdoo.data.Database;
import jdoo.util.DefaultDict;

public class Loader {
    static Logger _logger = LoggerFactory.getLogger(Loader.class);
    static DefaultDict<String, List<Class<?>>> module_to_models = new DefaultDict<>(ArrayList.class);

    static {
        String[] packages = new String[] { "jdoo.addons.base", "jdoo.addons.web" };
        for (String p : packages) {
            try {
                Class<?> info = Class.forName(p + ".package-info", true,
                        Thread.currentThread().getContextClassLoader());
                Package pkg = info.getPackage();
                Import imp = pkg.getAnnotation(Import.class);
                if (imp != null) {
                    Manifest manifest = pkg.getAnnotation(Manifest.class);
                    for (Class<?> clazz : imp.value()) {
                        module_to_models.get(manifest.name()).add(clazz);
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
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
            if (!Db.is_initialized(cr)) {
                _logger.info("init db");
                Db.initialize(cr);
            }
            load_module_graph(cr, registry);
            registry.loaded = true;
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
