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
import jdoo.models.RecordSet;
import jdoo.modules.Loader;
import jdoo.modules.Registry;
import jdoo.util.Kvalues;
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

    public static Environment create(Registry registry, Cursor cr, String uid, Kvalues context, boolean su) {
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

    public static Environment create(String tenant, Cursor cr, String uid, Kvalues context, boolean su) {
        Registry registry = Loader.getRegistry(tenant);
        return create(registry, cr, uid, context, su);
    }

    Registry registry;
    Cursor cr;
    Kvalues context;
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

    public Kvalues context() {
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

    public RecordSet get(String model) {
        return registry.get(model).browse(this, Tuple.emptyTuple(), Tuple.emptyTuple());
    }

    public RecordSet records_to_compute(Field field) {
        return get(field.model_name()).browse(all.tocompute(field));
    }

    public boolean is_protected(Field field, RecordSet record) {
        StackMap<Field, Collection<Object>> $protected = all.$protected;
        Collection<Object> ids = $protected.get(field, Tuple.emptyTuple());
        return ids.contains(record.id());
    }

    public RecordSet $protected(Field field) {
        StackMap<Field, Collection<Object>> $protected = all.$protected;
        return get(field.model_name()).browse($protected.get(field, Tuple.emptyTuple()));
    }

    public Protecting protecting(Collection<Field> fields, RecordSet records) {
        StackMap<Field, Collection<Object>> $protected = all.$protected;
        Map<Field, Collection<Object>> map = $protected.pushmap();
        for (Field field : fields) {
            Set<Object> ids_ = new HashSet<>();
            Collection<Object> ids = $protected.get(field, Tuple.emptyTuple());
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

    public void remove_to_compute(Field field, RecordSet records) {
        if (!records.hasId()) {
            return;
        }
        Collection<String> ids = all.tocompute().get(field);
        if (ids == null) {
            return;
        }
        ids.removeAll(records.ids());
        if (ids.isEmpty()) {
            all.tocompute().remove(field);
        }
    }

    private Kvalues lazy_properties = new Kvalues();

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

    public RecordSet user() {
        return lazy_property("user", () -> {
            return create(registry, cr, uid, context, true).get("res.users").browse(uid);
        });
    }

    @SuppressWarnings("unchecked")
    public RecordSet company() {
        return lazy_property("company", () -> {
            if (context.containsKey("allowed_company_ids")) {
                Tuple<Object> company_ids = (Tuple<Object>) context.get("allowed_company_ids");
                Object company_id = company_ids.get(0);
                if (user().get(RecordSet.class, "company_ids").ids().contains(company_id)) {
                    return get("res.company").browse(company_id);
                }
            }
            return user().get(RecordSet.class, "company_id");
        });
    }

    @SuppressWarnings("unchecked")
    public RecordSet companies() {
        return lazy_property("companies", () -> {
            List<Object> allowed_company_ids = new ArrayList<>();
            if (context.containsKey("allowed_company_ids")) {
                Tuple<Object> company_ids = (Tuple<Object>) context.get("allowed_company_ids");
                Collection<?> users_company_ids = user().get(RecordSet.class, "company_ids").ids();
                for (Object company_id : company_ids) {
                    if (users_company_ids.contains(company_id)) {
                        allowed_company_ids.add(company_id);
                    }
                }
            } else {
                allowed_company_ids.addAll(user().get(RecordSet.class, "company_ids").ids());
            }
            return get("res.company").browse(allowed_company_ids);
        });
    }
}
