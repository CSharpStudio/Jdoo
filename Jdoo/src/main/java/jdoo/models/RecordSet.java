package jdoo.models;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.lang.Nullable;

import jdoo.exceptions.ValueErrorException;
import jdoo.tools.Tools;
import jdoo.util.Default;
import jdoo.util.Kvalues;
import jdoo.util.Kwargs;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.apis.Environment;
import jdoo.data.Cursor;

/**
 * Every model instance is a "recordset", i.e., an ordered collection of records
 * of the model. Recordsets are returned by methods like :meth:`~.browse`,
 * :meth:`~.search`, or field accesses. Records have no explicit representation:
 * a record is represented as a recordset of one record.
 */
public final class RecordSet implements Iterable<RecordSet> {
    private MetaModel meta;
    private Environment env;
    private Kvalues _lazy_properties;
    Tuple<Object> _ids;
    Tuple<Object> _prefetch_ids;
    static Map<Field, Collection<Field>> _field_computed;

    public Map<Field, Collection<Field>> _field_computed() {
        return _field_computed;
    }

    public RecordSet(MetaModel cls, Environment env) {
        this.meta = cls;
        this.env = env;
    }

    <T> T lazy_property(String p, Supplier<T> func) {
        if (_lazy_properties == null) {
            _lazy_properties = new Kvalues();
        }
        if (_lazy_properties.containsKey(p)) {
            return (T) _lazy_properties.get(p);
        }
        T obj = func.get();
        _lazy_properties.put(p, obj);
        return obj;
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
        if (id instanceof Collection) {
            return meta.browse(env, (Collection<Object>) id, (Collection<Object>) id);
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
            return _ids.get(0);
        return false;
    }

    /** Return the list of actual record ids corresponding to this. */
    public List<?> ids() {
        return origin_ids(_ids);
    }

    /** Return the list of record _ids corresponding to this. */
    public List<?> _ids() {
        return _ids;
    }

    public List<?> prefetch_ids() {
        return _prefetch_ids;
    }

    /** Test whether this is nonempty. */
    public boolean hasId() {
        return _ids != null && _ids.size() > 0;
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
        if (_ids == null || _ids.size() > 1) {
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
     * Returns a new version of this recordset attached to the provided environment
     * <p>
     * .. warning:: The new environment will not benefit from the current
     * environment's data cache, so later data access may incur extra delays while
     * re-fetching from the database. The returned recordset has the same prefetch
     * object as ``self``.
     * </p>
     * 
     * @param env
     * @return
     */
    public RecordSet with_env(Environment env) {
        return meta.browse(env, _ids, _prefetch_ids);
    }

    /**
     * with_context([context][, **overrides]) -> records
     * 
     * <pre>
     *current context is {'key1': True} 
     *r2 = records.with_context(new Kvalues(k->k.set("key2",true));
     *-> r2._context is {'key2': True}
     * </pre>
     * 
     * @param context
     * @return a new version of this recordset attached to an extended context.
     */
    public RecordSet with_context(Kvalues context) {
        return with_env(env.create(new Kwargs(k -> k.set("context", context))));
    }

    /**
     * with_context([context][, **overrides]) -> records
     * 
     * <pre>
     *current context is {'key1': True} 
     *r2 = records.with_context(k->k.set("key2",true));
     *-> r2._context is {'key1': True, 'key2': True}
     * </pre>
     * 
     * @param func
     * @return a new version of this recordset attached to an extended context.
     */
    public RecordSet with_context(Consumer<Kvalues> func) {
        Kvalues context = new Kvalues(env.context());
        func.accept(context);
        return with_env(env.create(new Kwargs(k -> k.set("context", context))));
    }

    /**
     * with_context([context][, **overrides]) -> records
     * 
     * <pre>
     *current context is {'key1': True} 
     *r2 = records.with_context(k->k.set("key2",true));
     *-> r2._context is {'key1': True, 'key2': True}
     * </pre>
     * 
     * @param func
     * @return a new version of this recordset attached to an extended context.
     */
    public RecordSet with_context(Supplier<Map<String, Object>> func) {
        Kvalues context = new Kvalues(env.context());
        context.putAll(func.get());
        return with_env(env.create(new Kwargs(k -> k.set("context", context))));
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
    public RecordSet with_prefetch(@Nullable Collection<Object> prefetch_ids) {
        if (prefetch_ids == null) {
            prefetch_ids = _ids;
        }
        return type().browse(env, _ids, prefetch_ids);
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
        Field f = meta.getField(field.getName());
        return f.get(this);
    }

    /** read the field of the first record in {@code this}. */
    public <T> T get(Class<? extends T> c, String field) {
        return (T) get(field);
    }

    /** read the field of the first record in {@code this}. */
    public <T> T get(Class<? extends T> c, Field field) {
        Field f = meta.getField(field.getName());
        return (T) f.get(this);
    }

    /** Assign the field to value in this record . */
    public void set(String field, Object value) {
        Field f = meta.getField(field);
        f.set(this, value);
    }

    /** Assign the field to {@code value} in this record . */
    public void set(Field field, Object value) {
        Field f = meta.getField(field.getName());
        f.set(this, value);
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
    public <T> T call(Class<? extends T> c, String method, Object... args) {
        return (T) call(method, args);
    }

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
        return _ids.size();
    }

    /** get the record from this {@code RecordSet} */
    public RecordSet get(int index) {
        return browse(_ids.get(index));
    }

    /** subset of this {@code RecordSet} */
    public RecordSet get(int from, int to) {
        return browse(new Tuple<>(_ids.toArray(from, to)));
    }

    class SelfIterator implements Iterator<RecordSet> {
        int cusor = 0;

        @Override
        public boolean hasNext() {
            return cusor < _ids.size();
        }

        @Override
        public RecordSet next() {
            return browse(_ids.get(cusor++));
        }
    }

    @Override
    public String toString() {
        return String.format("%s%s", name(), _ids());
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
        return new HashSet<>(_ids).equals(new HashSet<>(other._ids));
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

    public Map<String, Object> _convert_to_record(Map<String, Object> values) {
        Map<String, Object> map = new HashMap<>();
        for (Entry<String, Object> e : values.entrySet()) {
            map.put(e.getKey(), getField(e.getKey()).convert_to_record(e.getValue(), this));
        }
        return map;
    }

    public Map<String, Object> _convert_to_write(Map<String, Object> values) {
        Map<String, Object> result = new HashMap<>();
        for (Entry<String, Object> e : values.entrySet()) {
            String name = e.getKey();
            Object value = e.getValue();
            if (hasField(name)) {
                Field field = getField(name);
                value = field.convert_to_write(value, this);
                if (!(value instanceof NewId)) {
                    result.put(name, value);
                }
            }
        }
        return result;
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

    /**
     * Select the records in ``self`` such that ``func(rec)`` is true, and return
     * them as a recordset.
     * 
     * @param func
     * @return
     */
    public RecordSet filtered(String func) {
        if ("id".equals(func)) {
            return filtered(rec -> Tools.hasId(rec.id()));
        } else {
            return filtered(rec -> Tools.hasValue(rec.get(func)));
        }
    }

    /**
     * Select the records in ``self`` such that ``func(rec)`` is true, and return
     * them as a recordset.
     * 
     * @param func
     * @return
     */
    public RecordSet filtered(Predicate<RecordSet> func) {
        return browse(StreamSupport.stream(this.spliterator(), false).filter(rec -> func.test(rec)).map(rec -> rec.id())
                .collect(Collectors.toList()));
    }

    public RecordSet filtered_domain(List<Object> domain) {
        // todo
        return this;
    }

    /** Return the actual records corresponding to ``self``. */
    public RecordSet origin() {
        List<Object> ids = origin_ids(this._ids);
        List<Object> prefetch_ids = origin_ids(this._prefetch_ids);
        return meta.browse(env, ids, prefetch_ids);
    }

    List<Object> origin_ids(List<Object> ids) {
        List<Object> ids_ = new ArrayList<>();
        for (Object id : ids) {
            if (id instanceof NewId) {
                ids_.add(((NewId) id).origin());
            } else if (!Boolean.FALSE.equals(id)) {
                ids_.add(id);
            }
        }
        return ids_;
    }

    public Map<String, Object> _cache() {
        return lazy_property("_cache", () -> {
            return new RecordCache(this);
        });
    }

    // =================================================================================
    // model methods
    //

    public RecordSet $new(Map<String, Object> values) {
        RecordSet record = browse(new NewId(null, null));
        record.call("_update_cache", values, false);
        return record;
    }

    public RecordSet $new(Map<String, Object> values, @Nullable Object origin, @Nullable Object ref) {
        if (origin instanceof RecordSet) {
            origin = ((RecordSet) origin).id();
        }
        RecordSet record = browse(new NewId(origin, ref));
        record.call("_update_cache", values, false);
        return record;
    }

    /**
     * Creates new records for the model.
     * 
     * The new records are initialized using the values from the list of dicts
     * ``values``, and if necessary those from :meth:`~.default_get`.
     * 
     * @param values values for the model's fields, as a list of dictionaries::
     *               <p>
     *               [{'field_name': field_value, ...}, ...]
     *               </p>
     *               For backward compatibility, ``vals_list`` may be a dictionary.
     *               It is treated as a singleton list ``[vals]``, and a single
     *               record is returned.
     */
    public RecordSet create(Object values) {
        return call(RecordSet.class, "create", this, values);
    }

    /** Update the records in ``self`` with ``values``. */
    public void update(Map<String, Object> values) {
        for (RecordSet record : this) {
            for (Entry<String, Object> entry : values.entrySet()) {
                record.set(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * name_create(name) -> record
     * 
     * Create a new record by calling :meth:`create` with only one value provided:
     * the display name of the new record.
     * 
     * The new record will be initialized with any default values applicable to this
     * model, or provided through the context. The usual behavior of :meth:`create`
     * applies.
     * 
     * @param name display name of the record to create
     * @return
     */
    public Pair<Object, String> name_create(String name) {
        return (Pair<Object, String>) call("name_create", this, name);
    }

    /**
     * Returns a textual representation for the records in ``self``. By default this
     * is the value of the ``display_name`` field.
     * 
     * @return list of pairs ``(id, text_repr)`` for each records
     */
    public List<Pair<Object, String>> name_get() {
        return (List<Pair<Object, String>>) call("name_get", this);
    }

    /**
     * Return default values for the fields in ``fields_list``. Default values are
     * determined by the context, user defaults, and the model itself.
     * 
     * @param fields_list a list of field names
     * @return a dictionary mapping each field name to its corresponding default
     *         value, if it has one.
     */
    public Kvalues default_get(Collection<String> fields_list) {
        return call(Kvalues.class, "default_get", this, fields_list);
    }

    /**
     * Updates all records in the current set with the provided values.
     * 
     * @param vals fields to update and the value to set on them e.g::
     *             <p>
     *             {'foo': 1, 'bar': "Qux"}
     *             </p>
     *             will set the field ``foo`` to ``1`` and the field ``bar`` to
     *             ``"Qux"`` if those are valid (otherwise it will trigger an
     *             error).
     * @return
     * @exception AccessErrorException   if user has no write rights on the
     *                                   requested object if user tries to bypass
     *                                   access rules for write on the requested
     *                                   object
     * @exception ValidateErrorException if user tries to enter invalid value for a
     *                                   field that is not in selection
     * @exception UserErrorException     if a loop would be created in a hierarchy
     *                                   of objects a result of the operation (such
     *                                   as setting an object as its own parent)
     */
    public boolean write(Map<String, Object> vals) {
        return call(Boolean.class, "write", this, vals);
    }

    /**
     * Process all the pending recomputations and flush the pending updates to the
     * database.
     */
    public void flush() {
        call("flush", this, null, null);
    }

    /**
     * Reads the requested fields for the records in ``self``, low-level/RPC method.
     * In Python code, prefer :meth:`~.browse`.
     * 
     * @param fields list of field names to return (default is all fields)
     * @return a list of dictionaries mapping field names to their values, with one
     *         dictionary per record
     * @exception AccessErrorException: if user has no read rights on some of the
     *                                  given records
     */
    public List<Kvalues> read(Collection<String> fields) {
        return (List<Kvalues>) call("read", this, fields);
    }

    /**
     * Returns the subset of records in ``self`` that exist, and marks deleted
     * records as such in cache. It can be used as a test on records:: <blockquote>
     * 
     * <pre>
     *if(record.exists().hasId()){
     *  ...
     *}
     * </pre>
     * 
     * </blockquote> By convention, new records are returned as existing.
     */
    public RecordSet exists() {
        return call(RecordSet.class, "exists", this);
    }

    /**
     * Search for records that have a display name matching the given ``name``
     * pattern when compared with the given ``operator``, while also matching the
     * optional search domain (``args``).
     * 
     * This is used for example to provide suggestions based on a partial value for
     * a relational field. Sometimes be seen as the inverse function of
     * :meth:`~.name_get`, but it is not guaranteed to be.
     * 
     * This method is equivalent to calling :meth:`~.search` with a search domain
     * based on ``display_name`` and then :meth:`~.name_get` on the result of the
     * search.
     * 
     * @param name     the name pattern to match
     * @param args     optional search domain (see :meth:`~.search` for syntax),
     *                 specifying further restrictions
     * @param operator domain operator for matching ``name``, such as ``'like'`` or
     *                 ``'='``.
     * @param limit    optional max number of records to return
     * @return list of pairs ``(id, text_repr)`` for all matching records.
     */
    public List<Pair<Object, String>> name_search(String name, List<Object> args, @Default("ilike") String operator,
            @Default("100") Integer limit) {
        return (List<Pair<Object, String>>) call("name_search", this, name, args, operator, limit);
    }

    /**
     * Search for records that have a display name matching the given ``name``
     * pattern when compared with the given ``operator``, while also matching the
     * optional search domain (``args``).
     * 
     * This is used for example to provide suggestions based on a partial value for
     * a relational field. Sometimes be seen as the inverse function of
     * :meth:`~.name_get`, but it is not guaranteed to be.
     * 
     * This method is equivalent to calling :meth:`~.search` with a search domain
     * based on ``display_name`` and then :meth:`~.name_get` on the result of the
     * search.
     * 
     * @param name the name pattern to match
     * @return list of pairs ``(id, text_repr)`` for all matching records.
     */
    public List<Pair<Object, String>> name_search(String name) {
        return (List<Pair<Object, String>>) call("name_search", this, name);
    }

    /**
     * Search for records that have a display name matching the given ``name``
     * pattern when compared with the given ``operator``, while also matching the
     * optional search domain (``args``).
     * 
     * This is used for example to provide suggestions based on a partial value for
     * a relational field. Sometimes be seen as the inverse function of
     * :meth:`~.name_get`, but it is not guaranteed to be.
     * 
     * This method is equivalent to calling :meth:`~.search` with a search domain
     * based on ``display_name`` and then :meth:`~.name_get` on the result of the
     * search.
     * 
     * @param name the name pattern to match
     * @param args optional search domain (see :meth:`~.search` for syntax),
     *             specifying further restrictions
     * @return list of pairs ``(id, text_repr)`` for all matching records.
     */
    public List<Pair<Object, String>> name_search(String name, List<Object> args) {
        return (List<Pair<Object, String>>) call("name_search", this, name, args);
    }

    /**
     * Searches for records based on the ``args`` :ref:`search domain
     * <reference/orm/domains>`.
     * 
     * @param args :ref:`A search domain <reference/orm/domains>`. Use an empty list
     *             to match all records.
     * 
     * @return at most ``limit`` records matching the search criteria
     * @exception AccessErrorException: * if user tries to bypass access rules for
     *                                  read on the requested object.
     */
    public RecordSet search(List<Object> args) {
        return (RecordSet) call("search", this, args, 0, null, null, false);
    }

    /**
     * Searches for records based on the ``args`` :ref:`search domain
     * <reference/orm/domains>`.
     * 
     * @param args   :ref:`A search domain <reference/orm/domains>`. Use an empty
     *               list to match all records.
     * @param offset number of results to ignore (default: 0)
     * @param limit  maximum number of records to return (default: all)
     * 
     * @return at most ``limit`` records matching the search criteria
     * @exception AccessErrorException: * if user tries to bypass access rules for
     *                                  read on the requested object.
     */
    public RecordSet search(List<Object> args, int offset, Integer limit) {
        return (RecordSet) call("search", this, args, offset, limit, null, false);
    }

    /**
     * Searches for records based on the ``args`` :ref:`search domain
     * <reference/orm/domains>`.
     * 
     * @param args   :ref:`A search domain <reference/orm/domains>`. Use an empty
     *               list to match all records.
     * @param offset number of results to ignore (default: 0)
     * @param limit  maximum number of records to return (default: all)
     * @param order  sort string
     * 
     * @return at most ``limit`` records matching the search criteria
     * @exception AccessErrorException: * if user tries to bypass access rules for
     *                                  read on the requested object.
     */
    public RecordSet search(List<Object> args, int offset, Integer limit, String order) {
        return (RecordSet) call("search", this, args, offset, limit, order, false);
    }

    /**
     * Searches for records based on the ``args`` :ref:`search domain
     * <reference/orm/domains>`.
     * 
     * @param args   :ref:`A search domain <reference/orm/domains>`. Use an empty
     *               list to match all records.
     * @param offset number of results to ignore (default: 0)
     * @param limit  maximum number of records to return (default: all)
     * @param order  sort string
     * @param count  if True, only counts and returns the number of matching records
     *               (default: false)
     * 
     * @return at most ``limit`` records matching the search criteria
     * @exception AccessErrorException: * if user tries to bypass access rules for
     *                                  read on the requested object.
     */
    public Object search(List<Object> args, int offset, Integer limit, String order, boolean count) {
        return call("search", this, args, offset, limit, order, count);
    }

    /**
     * Searches for records based on the ``args`` :ref:`search domain
     * <reference/orm/domains>`.
     * 
     * @param args   :ref:`A search domain <reference/orm/domains>`. Use an empty
     *               list to match all records.
     * @param kwargs dictionary args of offset, limit, order, count
     * 
     * @return at most ``limit`` records matching the search criteria
     * @exception AccessErrorException: * if user tries to bypass access rules for
     *                                  read on the requested object.
     */
    public Object search(List<Object> args, Kwargs kwargs) {
        return call("search", this, new Tuple<>(args), kwargs);
    }

    /**
     * Returns the number of records in the current model matching :ref:`the
     * provided domain <reference/orm/domains>`.
     * 
     * @param args
     * @return
     */
    public long search_count(List<Object> args) {
        return (long) call("search_count", this, args);
    }

    public void unlink() {
        call("unlink", this);
    }
}

class RecordCache implements Map<String, Object> {

    RecordSet _record;

    public RecordCache(RecordSet record) {
        assert record.size() == 1 : "Unexpected RecordCache(%s)".formatted(record);
        _record = record;
    }

    @Override
    public int size() {
        return _record.env().cache().get_fields(_record).size();
    }

    @Override
    public boolean isEmpty() {
        return size() > 0;
    }

    @Override
    public boolean containsKey(Object key) {
        Field field = _record.getField((String) key);
        return _record.env().cache().contains(_record, field);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(Object key) {
        Field field = _record.getField((String) key);
        return _record.env().cache().get(_record, field);
    }

    @Override
    public Object put(String key, Object value) {
        Field field = _record.getField((String) key);
        _record.env().cache().set(_record, field, value);
        return value;
    }

    @Override
    public Object remove(Object key) {
        Field field = _record.getField((String) key);
        return _record.env().cache().remove(_record, field);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        for (Entry<? extends String, ? extends Object> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        for (Field field : _record.getFields()) {
            _record.env().cache().remove(_record, field);
        }
    }

    @Override
    public Set<String> keySet() {
        return _record.env().cache().get_fields(_record).stream().map(f -> f.getName()).collect(Collectors.toSet());
    }

    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }

}