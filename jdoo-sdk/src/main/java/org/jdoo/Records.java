package org.jdoo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jdoo.core.BaseModel;
import org.jdoo.core.Environment;
import org.jdoo.core.MetaField;
import org.jdoo.core.MetaModel;
import org.jdoo.core.ModelInterceptor;
import org.jdoo.exceptions.ModelException;
import org.jdoo.exceptions.ValueException;
import org.jdoo.utils.ArrayUtils;

import org.apache.tomcat.util.buf.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.sf.cglib.proxy.Enhancer;

/**
 * 记录集
 * 
 * @author lrz
 */
@JsonSerialize(using = RecordsJsonSerializer.class)
public class Records implements Iterable<Records> {
    private String[] ids;
    private Supplier<String[]> prefetchIds;
    private Environment env;
    private MetaModel meta;

    public boolean any() {
        return ids != null && ids.length > 0;
    }

    /** 获取所有id */
    public String[] getIds() {
        return ids;
    }

    /** 获取所有id */
    public Supplier<String[]> getPrefetchIds() {
        return prefetchIds;
    }

    public int size() {
        return ids.length;
    }

    /** 获取id, 当前记录集必需有且只有一条记录 */
    public String getId() {
        if (!any()) {
            return "";
        }
        ensureOne();
        return ids[0];
    }

    public Environment getEnv() {
        return env;
    }

    public MetaModel getMeta() {
        return meta;
    }

    public Records(Environment env, MetaModel meta, String[] ids, Supplier<String[]> prefetchIds) {
        this.env = env;
        this.meta = meta;
        this.ids = ids;
        this.prefetchIds = prefetchIds;
    }

    public Records browse(Collection<String> ids) {
        Records rec = new Records(env, meta, ids.toArray(ArrayUtils.EMPTY_STRING_ARRAY), null);
        return rec;
    }

    public Records browse(String... ids) {
        Records rec = new Records(env, meta, ids, null);
        return rec;
    }

    public Records withPrefetch(Supplier<String[]> prefetchIds) {
        return new Records(env, meta, ids, prefetchIds);
    }

    public void ensureOne() {
        if (ids == null || ids.length != 1) {
            throw new ValueException(String.format("期望单一记录: %s", this));
        }
    }

    public Records withContext(String key, Object value) {
        Map<String, Object> context = new HashMap<>(env.getContext());
        context.put(key, value);
        return withEnv(new Environment(env.getRegistry(), env.getCursor(), env.getUserId(), context));
    }

    public Records withContext(Map<String, ? extends Object> ctx) {
        Map<String, Object> context = new HashMap<>(env.getContext());
        context.putAll(ctx);
        return withEnv(new Environment(env.getRegistry(), env.getCursor(), env.getUserId(), context));
    }

    public Records withNewContext(Map<String, ? extends Object> ctx) {
        Map<String, Object> context = new HashMap<>(ctx);
        return withEnv(new Environment(env.getRegistry(), env.getCursor(), env.getUserId(), context));
    }

    public Records withEnv(Environment env) {
        return getMeta().browse(env, ids, prefetchIds);
    }

    public Records withUser(String uid) {
        return withEnv(new Environment(env.getRegistry(), env.getCursor(), uid, env.getContext()));
    }

    /**
     * 获取字段值
     * 
     * @param field 字段名
     * @return 字段值
     */
    public Object get(String field) {
        MetaField f = getMeta().getField(field);
        return f.get(this);
    }

    /**
     * 获取字段值
     * 
     * @param field
     * @return 字段值
     */
    public Object get(MetaField field) {
        // 不能直接使用field，因为field在setup后已经不是原来的field，需要根据名称重新从元数据中获取field
        return get(field.getName());
    }

    /**
     * 设置字段值
     * 
     * @param field 字段名
     * @param value 要设置的值
     */
    public void set(String field, Object value) {
        MetaField f = getMeta().getField(field);
        f.set(this, value);
    }

    public void set(Field field, Object value) {
        // 不能直接使用field，因为field在setup后已经不是原来的field，需要根据名称重新从元数据中获取field
        set(field.getName(), value);
    }

    @Override
    public String toString() {
        String ids = "[" + StringUtils.join(getIds()) + "]";
        return String.format("%s%s", meta.getName(), ids);
    }

    public Object call(String method, Object... args) {
        try {
            return meta.invoke(method, getArgs(args));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ModelException(String.format("模型[%s]调用方法[%s]失败", meta.getName(), method), e);
        }
    }

    public Object callSuper(Class<?> current, String method, Object... args) {
        try {
            return meta.invokeSupper(current, method, getArgs(args));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ModelException(String.format("模型[%s]调用方法[%s]失败", meta.getName(), method), e);
        }
    }

    /**
     * 本地化翻译
     * 
     * @param str
     * @return
     */
    public String l10n(String format, Object... args) {
        return getEnv().l10n(format, args);
    }

    private Object[] getArgs(Object[] args) {
        if (args.length > 0 && args[0] instanceof Records) {
            return args;
        }
        Object[] result = new Object[args.length + 1];
        result[0] = this;
        for (int i = 0; i < args.length; i++) {
            result[i + 1] = args[i];
        }
        return result;
    }

    public Records create(Map<String, ? extends Object> values) {
        return (Records) call("create", this, values);
    }

    public Records createBatch(List<? extends Map<String, ? extends Object>> valuesList) {
        return (Records) call("createBatch", this, valuesList);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> read(List<String> fields) {
        return (List<Map<String, Object>>) call("read", this, fields);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> search(List<String> fields, Criteria criteria, Integer offset,
            Integer limit, String order) {
        return (List<Map<String, Object>>) call("search", this, fields, criteria, offset, limit, order);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> search(List<String> fields, Criteria criteria) {
        return (List<Map<String, Object>>) call("search", this, fields, criteria, 0, 0, null);
    }

    public Records find(Criteria criteria, Integer offset, Integer limit, String order) {
        return (Records) call("find", this, criteria, offset, limit, order);
    }

    public Records find(Criteria criteria) {
        return (Records) call("find", this, criteria, 0, 0, null);
    }

    public long count(Criteria criteria) {
        return (Long) call("count", this, criteria);
    }

    public void load(Map<String, ? extends Object> values) {
        call("load", this, values);
    }

    public void update(Map<String, ? extends Object> values) {
        call("update", this, values);
    }

    public void flush() {
        call("flush", this);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getPresent() {
        return (List<Object[]>) call("getPresent", this);
    }

    public void delete() {
        call("delete", this);
    }

    public Criteria criteria(String criteria) {
        return Criteria.parse(criteria);
    }

    public Criteria criteria(String field, String op, Object value) {
        return Criteria.binary(field, op, value);
    }

    public Records exists() {
        return (Records) call("exists");
    }

    public Stream<Records> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public Records filter(Predicate<Records> predicate) {
        String[] ids = stream().filter(predicate).map(p -> p.getId()).toArray(String[]::new);
        return getMeta().browse(getEnv(), ids, prefetchIds);
    }

    @Override
    public Iterator<Records> iterator() {
        return new RecordsIterator();
    }

    public final int PREFETCH_MAX = 1000;

    class RecordsIterator implements Iterator<Records> {
        int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor < ids.length;
        }

        @Override
        public Records next() {
            if (prefetchIds != null) {
                return getMeta().browse(getEnv(), new String[] { ids[cursor++] }, prefetchIds);
            }
            if (ids.length > PREFETCH_MAX) {
                int idx = cursor / PREFETCH_MAX;
                int from = idx * PREFETCH_MAX;
                return getMeta().browse(getEnv(), new String[] { ids[cursor++] },
                        () -> Arrays.copyOfRange(ids, from, from + PREFETCH_MAX));
            }
            return getMeta().browse(getEnv(), new String[] { ids[cursor++] }, () -> ids);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseModel> T as(Class<? extends T> clazz) {
        // T m = null;
        // try {
        // //m = (T) clazz.getConstructor().newInstance();
        // } catch (InstantiationException | IllegalAccessException |
        // IllegalArgumentException
        // | InvocationTargetException | NoSuchMethodException | SecurityException e) {
        // e.printStackTrace();
        // throw new Error("error");
        // }
        Enhancer e = new Enhancer();
        e.setSuperclass(clazz);
        e.setCallback(ModelInterceptor.INTERCEPTOR);
        T m = (T) e.create();
        m.setRecords(this);
        return m;
    }

    public <T extends BaseModel> Iterable<T> of(Class<? extends T> c) {
        return new ModelIterable<T>(c);
    }

    class ModelIterable<T extends BaseModel> implements Iterable<T> {
        Class<? extends T> clazz;

        public ModelIterable(Class<? extends T> c) {
            clazz = c;
        }

        @Override
        public Iterator<T> iterator() {
            return new ModelIterator<T>(clazz);
        }

    }

    class ModelIterator<T extends BaseModel> implements Iterator<T> {
        int cusor = 0;
        Class<? extends T> clazz;

        public ModelIterator(Class<? extends T> c) {
            clazz = c;
        }

        @Override
        public boolean hasNext() {
            return cusor < ids.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            Records rec = browse(ids[cusor++]);
            // T m = null;
            // try {
            // m = (T) clazz.getConstructor().newInstance();
            // } catch (InstantiationException | IllegalAccessException |
            // IllegalArgumentException
            // | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // e.printStackTrace();
            // throw new Error("error");
            // }
            Enhancer e = new Enhancer();
            e.setSuperclass(clazz);
            e.setCallback(ModelInterceptor.INTERCEPTOR);
            T m = (T) e.create();
            m.setRecords(rec);
            return m;
        }
    }
}

class RecordsJsonSerializer extends JsonSerializer<Records> {

    @Override
    public void serialize(Records value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeObject(value.getIds());
    }
}