package jdoo.apis;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdoo.init;
import jdoo.data.Cursor;
import jdoo.models.Field;
import jdoo.models.Self;
import jdoo.modules.Loader;
import jdoo.modules.Registry;
import jdoo.tools.Dict;
import jdoo.tools.StackMap;
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

    public static Environment create(Registry registry, Cursor cr, String uid, Dict context, boolean su) {
        if (init.SUPERUSER_ID.equals(uid))
            su = true;

        Tuple<Object> args = new Tuple<>(registry.tenant(), cr, uid, context, su);
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

    public static Environment create(String tenant, Cursor cr, String uid, Dict context, boolean su) {
        Registry registry = Loader.getRegistry(tenant);
        return create(registry, cr, uid, context, su);
    }

    Registry registry;
    Cursor cr;
    Dict context;
    Cache cache;
    boolean su;
    String uid;
    Tuple<Object> args;
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
        return registry.get(model).browse(this, Tuple.emptyTuple(), Tuple.emptyTuple());
    }

    public Self records_to_compute(Field field) {
        return get(field.model_name()).browse(all.tocompute(field));
    }

    public boolean is_protected(Field field, Self record) {
        StackMap<Field, Collection<String>> $protected = all.$protected;
        Collection<String> ids = $protected.get(field, Tuple.emptyTuple());
        return ids.contains(record.id());
    }

    public Self $protected(Field field) {
        StackMap<Field, Collection<String>> $protected = all.$protected;
        return get(field.model_name()).browse($protected.get(field, Tuple.emptyTuple()));
    }

    public Protecting protecting(Collection<Field> fields, Self records) {
        StackMap<Field, Collection<String>> $protected = all.$protected;
        Map<Field, Collection<String>> map = $protected.pushmap();
        for (Field field : fields) {
            Set<String> ids_ = new HashSet<String>();
            Collection<String> ids = $protected.get(field, Tuple.emptyTuple());
            ids_.addAll(ids);
            ids_.addAll(records.ids());
            map.put(field, ids_);
        }
        return new Protecting();
    }

    public class Protecting implements AutoCloseable {
        public void close() {
            all.$protected.popmap();
        }
    }

    public void remove_to_compute(Field field, Self records) {
        if (!records.hasId()) {
            return;
        }
        List<String> ids = all.tocompute().get(field);
        if (ids == null) {
            return;
        }
        ids.removeAll(records.ids());
        if (ids.isEmpty()) {
            all.tocompute().remove(field);
        }
    }
}
