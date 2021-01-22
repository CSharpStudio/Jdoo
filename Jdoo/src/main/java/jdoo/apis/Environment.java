package jdoo.apis;

import java.util.ArrayList;

import jdoo.init;
import jdoo.data.Cursor;
import jdoo.models.Self;
import jdoo.modules.Loader;
import jdoo.modules.Registry;
import jdoo.tools.Dict;
import jdoo.tools.Tuple;

public class Environment {
    static ThreadLocal<Environments> local = new ThreadLocal<Environments>();

    static Environments envs() {
        Environments envs = local.get();
        if (envs == null) {
            envs = new Environments();
            local.set(envs);
        }
        return envs;
    }

    public static Environment create(String key, Registry registry, Cursor cr, String uid, Dict context, boolean su) {
        if (init.SUPERUSER_ID.equals(uid))
            su = true;

        Tuple args = new Tuple(key, cr, uid, context, su);
        Environments envs = envs();
        for (Environment env : envs) {
            if (env.args.equals(args)) {
                return env;
            }
        }

        Environment self = new Environment();
        self.args = args;
        self.cr = cr;
        self.uid = uid;
        self.context = context;
        self.su = su;
        self.registry = registry;
        self.cache = envs.cache;
        self.all = envs;
        envs.add(self);
        return self;
    }

    public static Environment create(String key, Cursor cr, String uid, Dict context, boolean su) {
        Registry registry = Loader.getRegistry(key);
        return create(key, registry, cr, uid, context, su);
    }

    Registry registry;
    Cursor cr;
    Dict context;
    Cache cache;
    boolean su;
    String uid;
    Tuple args;
    Environments all;

    public Environments all() {
        return all;
    }

    public Cursor cr() {
        return cr;
    }

    public Dict context() {
        return context;
    }

    public boolean su() {
        return su;
    }

    public String lang() {
        return (String) context.get("lang");
    }

    public String uid() {
        return uid;
    }

    public Cache cache() {
        return cache;
    }

    Environment() {
    }

    public Self get(String model) {
        ArrayList<String> ids = new ArrayList<String>();
        return registry.get(model).browse(this, ids, ids);
    }
}
