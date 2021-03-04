package jdoo.models;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.lang.Nullable;

import jdoo.exceptions.ModelException;
import jdoo.exceptions.ValueErrorException;
import jdoo.tools.Tools;
import jdoo.util.Default;
import jdoo.util.Kvalues;
import jdoo.util.Kwargs;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.apis.Environment;
import jdoo.data.Cursor;

public final class RecordSet implements Iterable<RecordSet> {
    private MetaModel meta;
    private Environment env;
    Tuple<?> ids;
    Tuple<?> prefetchIds;
    static Map<Field, Collection<Field>> _field_computed;
    Kvalues context;

    public Map<Field, Collection<Field>> _field_computed() {
        return _field_computed;
    }

    public RecordSet(MetaModel cls, Environment env) {
        this.meta = cls;
        this.env = env;
    }

    /** the model name */
    public String name() {
        return meta.name();
    }

    /** the model meta */
    public MetaModel type() {
        return meta;
    }

    /** the Cursor */
    public Cursor cr() {
        return env.cr();
    }

    /** the Environment */
    public Environment env() {
        return env;
    }

    /** get RecordSet instance from model name */
    public RecordSet env(String model) {
        return env.get(model);
    }

    public MetaModel pool(String model) {
        return meta.pool.get(model);
    }

    /** SQL table name used by model */
    public String table() {
        return meta.table();
    }

    /** the context from Environment */
    public Kvalues context() {
        return env.context();
    }

    /** get value from context */
    public Object context(String key) {
        return env.context().get(key);
    }

    /**
     * browse([ids]) -> records
     * 
     * @param id single id or Collection ids
     * @return a recordset for the ids provided as parameter in the current
     *         environment.
     */
    public RecordSet browse(Object id) {
        if (id instanceof Collection<?>) {
            return meta.browse(env, (Collection<?>) id, (Collection<?>) id);
        }
        Tuple<Object> ids = new Tuple<>(id);
        return meta.browse(env, ids, ids);
    }

    /**
     * browse() -> records
     * 
     * @return a recordset for the ids provided as parameter in the current
     *         environment.
     */
    public RecordSet browse() {
        return meta.browse(env, Tuple.emptyTuple(), Tuple.emptyTuple());
    }

    /** */
    public Object id() {
        if (hasId())
            return ids.get(0);
        return false;
    }

    /** Return the list of actual record ids corresponding to this. */
    public Collection<?> ids() {
        return ids;
    }

    public Collection<?> prefetch_ids() {
        return prefetchIds;
    }

    /** Test whether this is nonempty. */
    public boolean hasId() {
        return ids != null && ids.size() > 0;
    }

    /** get field by name */
    public Field getField(String field) {
        return meta.getField(field);
    }

    /** get all fields of the model. */
    public Collection<Field> getFields() {
        return meta.getFields();
    }

    /** Test whether has the field. */
    public boolean hasField(String field) {
        return meta._fields.containsKey(field);
    }

    /**
     * Verifies that the current recorset holds a single record. Raises an exception
     * otherwise.
     */
    public void ensure_one() {
        if (ids == null || ids.size() > 1) {
            throw new ValueErrorException(String.format("Expected singleton: %s", this));
        }
    }

    /**
     * Returns a new version of this recordset with superuser mode enabled or
     * disabled, depending on `flag`. The superuser mode does not change the current
     * user, and simply bypasses access rights checks.
     * 
     * <p>
     * .. note::
     * </p>
     * Using ``sudo`` could cause data access to cross the boundaries of record
     * rules, possibly mixing records that are meant to be isolated (e.g. records
     * from different companies in multi-company environments).
     * 
     * It may lead to un-intuitive results in methods which select one record among
     * many - for example getting the default company, or selecting a Bill of
     * Materials.
     * <p>
     * .. note::
     * </p>
     * Because the record rules and access control will have to be re-evaluated, the
     * new recordset will not benefit from the current environment's data cache, so
     * later data access may incur extra delays while re-fetching from the database.
     * The returned recordset has the same prefetch object as ``self``.
     * 
     * @return
     */
    public RecordSet sudo() {
        // TODO
        return this;
    }

    public RecordSet sudo(boolean flag) {
        return this;
    }

    /**
     * with_context([context][, **overrides]) -> records
     * 
     * @param context
     * @return a new version of this recordset attached to an extended context.
     */
    public RecordSet with_context(Kvalues context) {
        // todo
        this.context = context;
        return this;
    }

    public RecordSet with_context(Consumer<Kvalues> func) {
        context = new Kvalues();
        func.accept(context);
        return this;
    }

    public RecordSet with_user(Object uid) {
        return this;
    }

    /**
     * with_prefetch([prefetch_ids]) -> records
     * 
     * @param prefetch_ids
     * @return a new version of this recordset that uses the given prefetch ids, or
     *         this's ids if not given.
     */
    public RecordSet with_prefetch(@Nullable Collection<?> prefetch_ids) {
        if (prefetch_ids == null) {
            prefetch_ids = ids;
        }
        return type().browse(env, ids, prefetch_ids);
    }

    // =================================================================================
    // get/set field value
    //

    /** read the field of the first record in {@code this}. */
    public Object get(String field) {
        Field f = meta.getField(field);
        return f.get(this);
    }

    /** read the field of the first record in {@code this}. */
    public Object get(Field field) {
        return field.get(this);
    }

    /** read the field of the first record in {@code this}. */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<? extends T> c, String field) {
        return (T) get(field);
    }

    /** read the field of the first record in {@code this}. */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<? extends T> c, Field field) {
        return (T) field.get(this);
    }

    /** Assign the field to value in this record . */
    public void set(String field, Object value) {
        Field f = meta.getField(field);
        f.set(this, value);
    }

    /** Assign the field to {@code value} in this record . */
    public void set(Field field, Object value) {
        field.set(this, value);
    }

    // =================================================================================
    // call methods
    //

    public Object call(String method, Collection<?> args, Kwargs kwargs) {
        Object[] array = args == null ? new Object[0] : args.toArray();
        return meta.invoke(method, getArgs(array), kwargs);
    }

    public Object call(String method, Kwargs kwargs) {
        return meta.invoke(method, new Object[] { this }, kwargs);
    }

    public Object call(String method, Object... args) {
        Kwargs kwargs = args.length > 0 && args[args.length - 1] instanceof Kwargs ? (Kwargs) args[args.length - 1]
                : null;
        return meta.invoke(method, getArgs(args), kwargs);
    }

    /** call method define in {@code Model}. */
    @SuppressWarnings("unchecked")
    public <T> T call(Class<? extends T> c, String method, Object... args) {
        return (T) call(method, args);
    }

    @SuppressWarnings("unchecked")
    public <T> T call(TypeReference<T> ref, String method, Object... args) {
        return (T) call(method, args);
    }

    private Object[] getArgs(Object[] args) {
        if (args.length == 0) {
            return new Object[] { this };
        } else if (args[0] instanceof RecordSet) {
            if (!(args[args.length - 1] instanceof Kwargs)) {
                return args;
            } else {
                Object[] result = new Object[args.length - 1];
                System.arraycopy(args, 0, result, 0, result.length);
                return result;
            }
        } else {
            int length = args[args.length - 1] instanceof Kwargs ? args.length - 1 : args.length;
            Object[] result = new Object[length + 1];
            result[0] = this;
            System.arraycopy(args, 0, result, 1, length);
            return result;
        }
    }

    // =================================================================================
    // Iterable
    //

    @Override
    public Iterator<RecordSet> iterator() {
        return new SelfIterator();
    }

    public Stream<RecordSet> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /** the size of this {@code RecordSet} */
    public int size() {
        return ids.size();
    }

    /** get the record from this {@code RecordSet} */
    public RecordSet get(int index) {
        return browse(ids.get(index));
    }

    /** subset of this {@code RecordSet} */
    public RecordSet get(int from, int to) {
        return browse(new Tuple<>(ids.toArray(from, to)));
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RecordSet)) {
            return false;
        }
        RecordSet other = (RecordSet) obj;
        if (!name().equals(other.name()) || size() != other.size()) {
            return false;
        }
        for (Object id : other.ids()) {
            if (!ids.contains(id)) {
                return false;
            }
        }
        return true;
    }

    // =================================================================================
    // +/- and or union
    //

    /**
     * Return the recordset of all the records in ``self`` that are not in
     * {@code other}. Note that recordset order is preserved.
     * 
     * @param other
     * @return
     */
    public RecordSet subtract(RecordSet other) {
        Collection<Object> other_ids = new HashSet<>(other.ids());
        return browse(ids().stream().filter(id -> !other_ids.contains(id)).collect(Collectors.toList()));
    }

    /**
     * Return the concatenation of ``self`` with all the arguments (in linear time
     * complexity).
     * 
     * @param other
     * @return
     */
    public RecordSet concat(RecordSet other) {
        HashSet<Object> ids = new HashSet<>(ids());
        ids.addAll(other.ids());
        return browse(ids);
    }

    /**
     * Return the intersection of two recordsets. Note that first occurrence order
     * is preserved.
     * 
     * @param other
     * @return
     */
    public RecordSet and(RecordSet other) {
        Collection<Object> other_ids = new HashSet<>(other.ids());
        return browse(ids().stream().filter(id -> other_ids.contains(id)).collect(Collectors.toList()));
    }

    /**
     * Return the union of two recordsets. Note that first occurrence order is
     * preserved.
     * 
     * @param other
     * @return
     */
    public RecordSet or(RecordSet other) {
        return union(other);
    }

    /**
     * Return the union of ``self`` with all the arguments (in linear time
     * complexity, with first occurrence order preserved).
     * 
     * @param others
     * @return
     */
    public RecordSet union(RecordSet... others) {
        Collection<Object> ids = new HashSet<>(ids());
        for (RecordSet other : others) {
            ids.addAll(other.ids());
        }
        return browse(ids);
    }

    /**
     * Return the union of ``self`` with all the arguments (in linear time
     * complexity, with first occurrence order preserved).
     * 
     * @param others
     * @return
     */
    public RecordSet union(Collection<RecordSet> others) {
        Collection<Object> ids = new HashSet<>(ids());
        for (RecordSet other : others) {
            ids.addAll(other.ids());
        }
        return browse(ids);
    }

    Object _mapped_func(Function<RecordSet, Object> func) {
        if (hasId()) {
            List<Object> vals = StreamSupport.stream(this.spliterator(), false).map(rec -> func.apply(rec))
                    .collect(Collectors.toList());
            if (vals.get(0) instanceof RecordSet) {
                return ((RecordSet) vals.get(0))
                        .union(vals.stream().map(v -> (RecordSet) v).collect(Collectors.toList()));
            }
            return vals;
        } else {
            Object vals = func.apply(this);
            if (vals instanceof RecordSet) {
                return vals;
            } else {
                return Collections.emptyList();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Object mapped(Object func) {
        if (func == null) {
            return this;
        }
        if (func instanceof String) {
            RecordSet recs = this;
            for (String name : ((String) func).split("\\.")) {
                recs = (RecordSet) _mapped_func(rec -> rec.get(name));
            }
            return recs;
        } else {
            return _mapped_func((Function<RecordSet, Object>) func);
        }
    }

    public RecordSet filtered(String func) {
        if ("id".equals(func)) {
            return filtered(rec -> Tools.hasId(rec.id()));
        } else {
            return filtered(rec -> Tools.hasValue(rec.get(func)));
        }
    }

    public RecordSet filtered(Predicate<RecordSet> func) {
        return browse(StreamSupport.stream(this.spliterator(), false).filter(rec -> func.test(rec)).map(rec -> rec.id())
                .collect(Collectors.toList()));
    }

    // =================================================================================
    // model methods
    //

    public RecordSet create(Object values) {
        return call(RecordSet.class, "create", this, values);
    }

    @SuppressWarnings("unchecked")
    public Pair<Object, Object> name_create(String name) {
        return (Pair<Object, Object>) call("name_create", this, name);
    }

    @SuppressWarnings("unchecked")
    public List<Pair<Object, Object>> name_get() {
        return (List<Pair<Object, Object>>) call("name_get", this);
    }

    public Kvalues default_get(Collection<String> fields_list) {
        return call(Kvalues.class, "default_get", this, fields_list);
    }

    public boolean write(Map<String, Object> vals) {
        return call(Boolean.class, "write", this, vals);
    }

    public void flush() {
        call("flush", this, null, null);
    }

    @SuppressWarnings("unchecked")
    public List<Kvalues> read(Collection<String> fields) {
        return (List<Kvalues>) call("read", this, fields);
    }

    public RecordSet exists() {
        return call(RecordSet.class, "exists", this);
    }

    @SuppressWarnings("unchecked")
    public List<Pair<Object, Object>> name_search(String name, List<Object> args, @Default("ilike") String operator,
            @Default("100") Integer limit) {
        return (List<Pair<Object, Object>>) call("name_search", name, args, operator, limit);
    }

    @SuppressWarnings("unchecked")
    public List<Pair<Object, Object>> name_search(String name) {
        return (List<Pair<Object, Object>>) call("name_search", name);
    }

    @SuppressWarnings("unchecked")
    public List<Pair<Object, Object>> name_search(String name, List<Object> args) {
        return (List<Pair<Object, Object>>) call("name_search", name, args);
    }

    public RecordSet search(List<Object> args) {
        return (RecordSet) call("search", args, 0, null, null, false);
    }

    public RecordSet search(List<Object> args, int offset, Integer limit) {
        return (RecordSet) call("search", args, offset, limit, null, false);
    }

    public RecordSet search(List<Object> args, int offset, Integer limit, String order) {
        return (RecordSet) call("search", args, offset, limit, order, false);
    }

    public Object search(List<Object> args, int offset, Integer limit, String order, boolean count) {
        return call("search", args, offset, limit, order, count);
    }
}
