package jdoo.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import jdoo.exceptions.JdooException;
import jdoo.exceptions.ModelException;
import jdoo.tools.Dict;
import jdoo.apis.Environment;

public final class Self implements Iterable<Self> {
    private MetaModel meta;
    private Environment env;
    List<String> ids;
    List<String> prefetchIds;

    public Self(MetaModel cls, Environment env) {
        this.meta = cls;
        this.env = env;
    }

    public Environment env() {
        return env;
    }

    String table() {
        return meta.getName().replace('.', '_');
    }

    public Dict context() {
        return env.context();
    }

    public Object context(String key) {
        return env.context().get(key);
    }

    public Self env(String model) {
        return env.get(model);
    }

    public Self browse(List<String> ids) {
        return meta.browse(env, ids, ids);
    }

    public Self browse(String id) {
        ArrayList<String> ids = new ArrayList<String>();
        ids.add(id);
        return meta.browse(env, ids, ids);
    }

    public Self browse() {
        ArrayList<String> ids = new ArrayList<String>();
        return meta.browse(env, ids, ids);
    }

    public List<String> ids() {
        return ids;
    }

    public String id() {
        if (hasId())
            return ids.get(0);
        throw new ModelException("Model:" + meta.getName() + " ids not set");
    }

    public boolean hasId() {
        return ids != null && !ids.isEmpty();
    }

    public String getName() {
        return meta.getName();
    }

    public MetaModel getMeta() {
        return meta;
    }

    public Field getField(String field) {
        return meta.getField(field);
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
        if (args.length > 0 && args[0] instanceof Self) {
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
    public <T> T call(TypeReference<?> ref, String method, Object... args) {
        return (T) call(method, args);
    }

    public Object callSuper(String method, Object... args) {
        InvokeResult result = meta.tryInvoke(method, getArgs(args));
        if (result.getSuccess())
            return result.getResult();
        return null;
    }

    @Override
    public Iterator<Self> iterator() {
        return new SelfIterator();
    }

    class SelfIterator implements Iterator<Self> {
        int cusor = 0;

        @Override
        public boolean hasNext() {
            return cusor < ids.size();
        }

        @Override
        public Self next() {
            return browse(ids.get(cusor++));
        }
    }
}
