package jdoo.modules;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import jdoo.models.MetaModel;
import jdoo.models.RecordSet;
import jdoo.util.Kvalues;
import jdoo.init;
import jdoo.apis.Environment;
import jdoo.data.Cursor;
import jdoo.data.Database;
import jdoo.exceptions.JdooException;
import jdoo.exceptions.ModelException;

public class Registry {
    HashMap<String, MetaModel> map = new HashMap<String, MetaModel>();
    String tenant;
    boolean ready;// whether everything is set up
    boolean loaded;// whether all modules are loaded
    Database db;
    static ConcurrentHashMap<String, Registry> registries = new ConcurrentHashMap<>();

    public static Registry getRegistry(String tenant) {
        if (!registries.containsKey(tenant)) {
            synchronized (Registry.class) {
                if (!registries.containsKey(tenant)) {
                    Registry registry = new Registry(tenant);
                    registries.put(tenant, registry);
                    Loader.load_modules(registry.db, registry);
                    return registry;
                }
            }
        }
        return registries.get(tenant);
    }

    public Registry(String tenant) {
        this.tenant = tenant;
        db = Database.get(tenant);
    }

    public String tenant() {
        return tenant;
    }

    public boolean ready() {
        return ready;
    }

    public boolean loaded() {
        return loaded;
    }

    public MetaModel get(String model) {
        MetaModel m = map.get(model);
        if (m == null)
            throw new ModelException("model:'" + model + "' is not registered");
        return m;
    }

    public void put(String name, MetaModel model) {
        map.put(name, model);
    }

    public boolean contains(String model) {
        return map.containsKey(model);
    }

    public void setup_models(Cursor cr) {
        Environment env = Environment.create(cr, init.SUPERUSER_ID, new Kvalues(), true);

        List<RecordSet> models = new ArrayList<>();
        for (MetaModel m : map.values()) {
            RecordSet self = env.get(m.name());
            models.add(self);
            self.call("_prepare_setup");
        }
        for (RecordSet self : models) {
            self.call("_setup_base");
        }
        for (RecordSet self : models) {
            self.call("_setup_fields");
        }
        for (RecordSet self : models) {
            self.call("_setup_complete");
        }
    }

    public void init_models(Cursor cr) {
        Environment env = Environment.create(cr, init.SUPERUSER_ID, new Kvalues(), true);
        List<RecordSet> models = new ArrayList<>();
        for (MetaModel m : map.values()) {
            RecordSet self = env.get(m.name());
            models.add(self);
            self.call("_auto_init");
            self.call("init");
        }
        env.get("base").call("flush");
    }

    public void load(Cursor cr, String module) {
        for (Class<?> clazz : Loader.module_to_models.get(module)) {
            if (Modifier.isAbstract(clazz.getModifiers()))
                throw new ModelException("Model:" + clazz.getName() + " cannot be abstract class");
            try {
                MetaModel._build_model(clazz, module, this, cr);
            } catch (Exception e) {
                throw new JdooException("load module " + module + " class " + clazz.getName() + " failed", e);
            }
        }
    }

    public void post_init() {

    }
}
