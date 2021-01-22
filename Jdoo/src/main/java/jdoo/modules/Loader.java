package jdoo.modules;

import java.util.concurrent.ConcurrentHashMap;

public class Loader {
    static ConcurrentHashMap<String, Registry> registries = new ConcurrentHashMap<String, Registry>();

    public static Registry getRegistry(String key) {
        if (!registries.containsKey(key)) {
            synchronized (Registry.class) {
                if (!registries.containsKey(key)) {
                    Registry registry = new Registry();
                    load_modules(registry, key);
                    registries.put(key, registry);
                }
            }
        }
        return registries.get(key);
    }

    public static void load_modules(Registry registry, String key) {
        jdoo.web.__init__.init(registry);
        jdoo.base.__init__.init(registry);
    }
}
