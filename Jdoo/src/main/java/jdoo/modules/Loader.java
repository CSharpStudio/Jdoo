package jdoo.modules;

import java.util.concurrent.ConcurrentHashMap;

import jdoo.data.Cursor;
import jdoo.data.Database;

public class Loader {
    static ConcurrentHashMap<String, Registry> registries = new ConcurrentHashMap<String, Registry>();

    public static Registry getRegistry(String tenant) {
        if (!registries.containsKey(tenant)) {
            synchronized (Registry.class) {
                if (!registries.containsKey(tenant)) {
                    Database db = new Database("config/dbcp.properties");//db shoud be get from tenant
                    Registry registry = new Registry(tenant);
                    load_modules(db, registry);
                    registries.put(tenant, registry);                    
                }
            }
        }
        return registries.get(tenant);
    }

    public static void load_modules(Database db, Registry registry) {
        jdoo.web.__init__.init(registry);
        jdoo.base.__init__.init(registry);

        try(Cursor cr = db.cursor()){
            registry.setup_models(cr);
        }
    }
}
