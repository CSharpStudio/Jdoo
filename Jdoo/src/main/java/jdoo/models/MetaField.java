package jdoo.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import jdoo.tools.Slot;
import jdoo.util.Dict;
import jdoo.util.Pair;
import jdoo.util.Tuple;

/**
 * Meta attrs for field classes
 */
public abstract class MetaField {
    enum SetupState {
        None, Base, Full
    }

    Map<Slot, Object> _slots = new HashMap<>();
    protected static Map<Slot, Object> default_slots = new HashMap<Slot, Object>() {
        private static final long serialVersionUID = 1L;
        {
            put(automatic, false);
            put(inherited, false);
            put(store, true);
            put(index, false);
            put(manual, false);
            put(copy, true);
            put(recursive, false);
            put(compute_sudo, true);
            put(company_dependent, false);
            put(readonly, false);
            put(required, false);
            put(change_default, false);
            put(prefetch, true);
        }
    };

    public Object get(Slot key) {
        if (!_slots.containsKey(key)) {
            return default_slots.get(key);
        }
        return _slots.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz, Slot key) {
        return (T) get(key);
    }

    public void set(Slot key, Object value) {
        _slots.put(key, value);
    }

    public boolean has(Slot key) {
        return _slots.containsKey(key);
    }

    static int _global_seq = 0;
    /** whether the field is a relational one */
    protected boolean relational = false;
    /** whether the field is translated */
    Boolean translate;
    /** database column type (ident, spec) */
    protected Pair<String, Object> column_type;
    /** placeholder for value in queries */
    String column_format = "%s";
    /** column types that may be cast to this */
    protected Tuple<String> column_cast_from = Tuple.emptyTuple();
    /** absolute ordering of the field */
    int _sequence;
    /** name of the field */
    String name;
    /** name of the model of this field */
    String model_name;
    /** the field's setup state: None, 'base' or 'full' */
    SetupState _setup_done = SetupState.None;

    protected Dict context;

    protected MetaField() {
        _sequence = _global_seq++;
    }

    public String getName() {
        return name;
    }

    Dict context() {
        if (context == null) {
            context = new Dict();
        }
        return context;
    }

    boolean relational() {
        return relational;
    }

    boolean translate() {
        return translate != null && translate;
    }

    public Pair<String, Object> column_type() {
        return column_type;
    }

    String module() {
        return get(String.class, _module);
    }

    boolean automatic() {
        return get(Boolean.class, automatic);
    }

    public boolean inherited() {
        return get(Boolean.class, inherited);
    }

    public Field related_field() {
        return get(Field.class, related_field);
    }

    String inverse() {
        return get(String.class, inverse);
    }

    String compute() {
        return get(String.class, compute);
    }

    String groups() {
        return get(String.class, groups);
    }

    String string() {
        return get(String.class, string);
    }

    @SuppressWarnings("unchecked")
    Function<RecordSet, Object> $default() {
        return (Function<RecordSet, Object>) get(related_field);
    }

    @SuppressWarnings("unchecked")
    Collection<String> depends() {
        return (Collection<String>) get(depends);
    }

    @SuppressWarnings("unchecked")
    public Collection<String> related() {
        return (Collection<String>) get(related);
    }

    public String model_name() {
        return model_name;
    }

    public String comodel_name() {
        return get(String.class, comodel_name);
    }

    public boolean store() {
        return get(Boolean.class, store);
    }

    boolean index() {
        return get(Boolean.class, index);
    }

    boolean manual() {
        return get(Boolean.class, manual);
    }

    boolean copy() {
        return get(Boolean.class, copy);
    }

    @SuppressWarnings("unchecked")
    public Collection<String> depends_context() {
        if (_slots.containsKey(depends_context)) {
            return (Collection<String>) _slots.get(depends_context);
        }
        return Tuple.emptyTuple();
    }

    boolean recursive() {
        return get(Boolean.class, recursive);
    }

    boolean compute_sudo() {
        return get(Boolean.class, compute_sudo);
    }

    boolean company_dependent() {
        return get(Boolean.class, company_dependent);
    }

    public boolean readonly() {
        return get(Boolean.class, readonly);
    }

    public boolean required() {
        return get(Boolean.class, required);
    }

    public boolean change_default() {
        return get(Boolean.class, change_default);
    }

    public boolean deprecated() {
        return get(Boolean.class, deprecated);
    }

    public boolean prefetch() {
        return get(Boolean.class, prefetch);
    }

    /** the field's module name */
    public static final Slot _module = new Slot("_module");
    /** modules that define this field */
    public static final Slot _modules = new Slot("_modules");
    /** whether the field is automatically created ("magic" field) */
    public static final Slot automatic = new Slot("automatic");
    /** whether the field is inherited (_inherits) */
    public static final Slot inherited = new Slot("inherited");
    /** the corresponding inherited field */
    public static final Slot inherited_field = new Slot("inherited_field");
    /** name of the model of values (if relational) */
    public static final Slot comodel_name = new Slot("comodel_name");
    /** whether the field is stored in database */
    public static final Slot store = new Slot("store");
    /** whether the field is indexed in database */
    public static final Slot index = new Slot("index");
    /** whether the field is a custom field */
    public static final Slot manual = new Slot("manual");
    /** whether the field is copied over by BaseModel.copy() */
    public static final Slot copy = new Slot("copy");
    /** collection of field dependencies */
    public static final Slot depends = new Slot("depends");
    /** collection of context key dependencies */
    public static final Slot depends_context = new Slot("depends_context");
    /** whether self depends on itself */
    public static final Slot recursive = new Slot("recursive");
    /** compute(recs) computes field on recs */
    public static final Slot compute = new Slot("compute");
    /** whether field should be recomputed as superuser */
    public static final Slot compute_sudo = new Slot("compute_sudo");
    /** inverse(recs) inverses field on recs */
    public static final Slot inverse = new Slot("inverse");
    /** search(recs, operator, value) searches on self */
    public static final Slot search = new Slot("search");
    /** sequence of field names, for related fields */
    public static final Slot related = new Slot("related");
    /** whether ``self`` is company-dependent (property field) */
    public static final Slot company_dependent = new Slot("company_dependent");
    /** default(recs) returns the default value */
    public static final Slot $default = new Slot("default");
    /** field label */
    public static final Slot string = new Slot("string");
    /** field tooltip */
    public static final Slot help = new Slot("help");
    /** whether the field is readonly */
    public static final Slot readonly = new Slot("readonly");
    /** whether the field is required */
    public static final Slot required = new Slot("required");
    /** set readonly and required depending on state */
    public static final Slot states = new Slot("states");
    /** csv list of group xml ids */
    public static final Slot groups = new Slot("groups");
    /** whether the field may trigger a "user-onchange" */
    public static final Slot change_default = new Slot("change_default");
    /** whether the field is deprecated */
    public static final Slot deprecated = new Slot("deprecated");
    /** corresponding related field */
    public static final Slot related_field = new Slot("related_field");
    /** operator for aggregating values */
    public static final Slot group_operator = new Slot("group_operator");
    /** name of method to expand groups in read_group() */
    public static final Slot group_expand = new Slot("group_expand");
    /** whether the field is prefetched */
    public static final Slot prefetch = new Slot("prefetch");

    public class State {
        private String state;
        private boolean value;

        public State(String state, boolean value) {
            this.state = state;
            this.value = value;
        }

        public String state() {
            return state;
        }

        public boolean value() {
            return value;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaField) {
            MetaField field = (MetaField) obj;
            return field.model_name == model_name && field.name == name;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (model_name + "@" + name).hashCode();
    }

    @Override
    public String toString() {
        return model_name + "." + name;
    }
}
