package jdoo.apis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import jdoo.init;
import jdoo.data.Cursor;
import jdoo.models.Field;
import jdoo.models.Self;
import jdoo.modules.Loader;
import jdoo.modules.Registry;
import jdoo.util.Dict;
import jdoo.tools.StackMap;
import jdoo.util.Tuple;

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

    public void clear() {
        cache.invalidate();
        all.tocompute.clear();
        all.towrite.clear();
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

    private Dict lazy_properties = new Dict();

    @SuppressWarnings("unchecked")
    <T> T lazy_property(String p, Supplier<T> func) {
        if (lazy_properties.containsKey(p)) {
            return (T) lazy_properties.get(p);
        }
        T obj = func.get();
        lazy_properties.put(p, obj);
        return obj;
    }

    public void reset_lazy_property() {
        lazy_properties.clear();
    }

    public Self user() {
        return lazy_property("user", () -> {
            return create(registry, cr, uid, context, true).get("res.users").browse(uid);
        });
    }

    @SuppressWarnings("unchecked")
    public Self company() {
        return lazy_property("company", () -> {
            if (context.containsKey("allowed_company_ids")) {
                Tuple<String> company_ids = (Tuple<String>) context.get("allowed_company_ids");
                String company_id = company_ids.get(0);
                if (user().get(Self.class, "company_ids").ids().contains(company_id)) {
                    return get("res.company").browse(company_id);
                }
            }
            return user().get(Self.class, "company_id");
        });
    }

    @SuppressWarnings("unchecked")
    public Self companies() {
        return lazy_property("companies", () -> {
            List<String> allowed_company_ids = new ArrayList<>();
            if (context.containsKey("allowed_company_ids")) {
                Tuple<String> company_ids = (Tuple<String>) context.get("allowed_company_ids");
                Collection<String> users_company_ids = user().get(Self.class, "company_ids").ids();
                for (String company_id : company_ids) {
                    if (users_company_ids.contains(company_id)) {
                        allowed_company_ids.add(company_id);
                    }
                }
            } else {
                allowed_company_ids.addAll(user().get(Self.class, "company_ids").ids());
            }
            return get("res.company").browse(allowed_company_ids);
        });
    }
}
