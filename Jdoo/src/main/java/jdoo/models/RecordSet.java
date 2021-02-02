package jdoo.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import jdoo.exceptions.JdooException;
import jdoo.exceptions.ModelException;
import jdoo.exceptions.ValueErrorException;
import jdoo.util.Dict;
import jdoo.util.Linq;
import jdoo.util.Tuple;
import jdoo.apis.Environment;

public final class RecordSet implements Iterable<RecordSet> {
    private MetaModel meta;
    private Environment env;
    Tuple<String> ids;
    Tuple<String> prefetchIds;
    static Map<Field, Collection<Field>> _field_computed;

    public Map<Field, Collection<Field>> _field_computed() {
        return _field_computed;
    }

    public RecordSet(MetaModel cls, Environment env) {
        this.meta = cls;
        this.env = env;
    }

    public Environment env() {
        return env;
    }

    public String table() {
        return meta.name().replace('.', '_');
    }

    public Dict context() {
        return env.context();
    }

    public Object context(String key) {
        return env.context().get(key);
    }

    public RecordSet env(String model) {
        return env.get(model);
    }

    public RecordSet browse(Collection<String> ids) {
        return meta.browse(env, ids, ids);
    }

    public RecordSet browse(String id) {
        Tuple<String> ids = Tuple.of(id);
        return meta.browse(env, ids, ids);
    }

    public RecordSet browse() {
        ArrayList<String> ids = new ArrayList<String>();
        return meta.browse(env, ids, ids);
    }

    public Collection<String> ids() {
        return ids;
    }

    public String id() {
        if (hasId())
            return ids.get(0);
        throw new ModelException("Model:" + meta.name() + " ids not set");
    }

    public boolean hasId() {
        return ids != null && ids.size() > 0;
    }

    public void ensure_one() {
        if (ids == null || ids.size() > 1) {
            throw new ValueErrorException(String.format("Expected singleton: %s", this));
        }
    }

    public String name() {
        return meta.name();
    }

    public MetaModel type() {
        return meta;
    }

    public Field getField(String field) {
        return meta.getField(field);
    }

    public boolean hasField(String field) {
        return meta._fields.containsKey(field);
    }

    public Collection<Field> getFields() {
        return meta.getFields();
    }

    public Object get(String field) {
        Field f = meta.getField(field);
        return f.get(this);
    }

    public Object get(Field field) {
        return field.get(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<? extends T> c, String field) {
        return (T) get(field);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<? extends T> c, Field field) {
        return (T) field.get(this);
    }

    public void set(String field, Object value) {
        Field f = meta.getField(field);
        f.set(this, value);
    }

    public void set(Field field, Object value) {
        field.set(this, value);
    }

    private Object[] getArgs(Object[] args) {
        if (args.length > 0 && args[0] instanceof RecordSet) {
            return args;
        }
        Object[] result = new Object[args.length + 1];
        result[0] = this;
        for (int i = 0; i < args.length; i++) {
            result[i + 1] = args[i];
        }
        return result;
    }

    public Object call(String method, Object... args) {
        try {
            return meta.invoke(method, getArgs(args));
        } catch (Exception e) {
            throw new JdooException("call method:'" + method + "' failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T call(Class<? extends T> c, String method, Object... args) {
        return (T) call(method, args);
    }

    @SuppressWarnings("unchecked")
    public <T> T call(TypeReference<T> ref, String method, Object... args) {
        return (T) call(method, args);
    }

    public Object callSuper(String method, Object... args) {
        InvokeResult result = meta.tryInvoke(method, getArgs(args));
        if (result.getSuccess())
            return result.getResult();
        return null;
    }

    @Override
    public Iterator<RecordSet> iterator() {
        return new SelfIterator();
    }

    class SelfIterator implements Iterator<RecordSet> {
        int cusor = 0;

        @Override
        public boolean hasNext() {
            return cusor < ids.size();
        }

        @Override
        public RecordSet next() {
            return browse(ids.get(cusor++));
        }
    }

    @Override
    public String toString() {
        return String.format("%s%s", name(), ids());
    }

    public RecordSet sudo() {
        return this;
    }

    public RecordSet with_context(Dict context) {
        return this;
    }

    public RecordSet _in_cache_without(Field field) {
        RecordSet recs = browse(prefetchIds);
        List<String> ids = new ArrayList<>(ids());
        for (String record_id : env.cache().get_missing_ids(recs.subtract(this), field)) {
            if (record_id.isBlank()) {
                continue;
            }
            ids.add(record_id);
        }
        return browse(ids);
    }

    public RecordSet subtract(RecordSet other) {
        Collection<String> other_ids = new HashSet<String>(other.ids());
        return browse(Collections.list(Linq.where(ids(), id -> !other_ids.contains(id))));
    }

    public RecordSet concat(RecordSet other) {
        HashSet<String> ids = new HashSet<>(ids());
        ids.addAll(other.ids());
        return browse(ids);
    }

    public RecordSet and(RecordSet other) {
        Collection<String> other_ids = new HashSet<String>(other.ids());
        return browse(Collections.list(Linq.where(ids(), id -> other_ids.contains(id))));
    }

    public RecordSet or(RecordSet other) {
        Collection<String> ids = new HashSet<String>(ids());
        ids.addAll(other.ids());
        return browse(ids);
    }
}
