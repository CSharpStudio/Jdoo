package jdoo.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import jdoo.tools.Slot;
import jdoo.util.Kvalues;
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

    public Object getattr(Slot key) {
        if (!_slots.containsKey(key)) {
            return key.value();
        }
        return _slots.get(key);
    }

    public <T> T getattr(Class<T> clazz, Slot key) {
        return (T) getattr(key);
    }

    public void setattr(Slot key, Object value) {
        _slots.put(key, value);
    }

    public boolean hasattr(Slot key) {
        return _slots.containsKey(key);
    }

    static int _global_seq = 0;
    /** whether the field is a relational one */
    protected boolean relational = false;
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

    protected MetaField() {
        _sequence = _global_seq++;
    }

    public String getName() {
        return name;
    }

    protected Kvalues _context() {
        return Kvalues.empty();
    }

    boolean _relational() {
        return relational;
    }

    public boolean _translate() {
        return false;
    }

    public String column_format() {
        return column_format;
    }

    public String model_name() {
        return model_name;
    }

    public Pair<String, Object> column_type() {
        return column_type;
    }

    /** get slot _module */
    String _module() {
        return getattr(String.class, Slots._module);
    }

    /** get slot automatic */
    boolean _automatic() {
        return getattr(Boolean.class, Slots.automatic);
    }

    /** get slot inherited */
    public boolean _inherited() {
        return getattr(Boolean.class, Slots.inherited);
    }

    /** get slot related_field */
    public Field _related_field() {
        return getattr(Field.class, Slots.related_field);
    }

    /** has slot inverse */
    boolean _inverse() {
        return hasattr(Slots.inverse);
    }

    /** has slot search */
    public boolean _search() {
        return hasattr(Slots.search);
    }

    /** get slot compute */
    String _compute() {
        return getattr(String.class, Slots.compute);
    }

    /** get slot groups */
    String _groups() {
        return getattr(String.class, Slots.groups);
    }

    /** get slot string */
    String _string() {
        return getattr(String.class, Slots.string);
    }

    /** get slot default */
    Function<RecordSet, Object> _default() {
        return (Function<RecordSet, Object>) getattr(Slots.$default);
    }

    /** get slot depends */
    Collection<String> _depends() {
        return (Collection<String>) getattr(Slots.depends);
    }

    /** get slot related */
    public Collection<String> _related() {
        return (Collection<String>) getattr(Slots.related);
    }

    /** get slot comodel_name */
    public String _comodel_name() {
        return getattr(String.class, Slots.comodel_name);
    }

    /** get slot store */
    public boolean _store() {
        return getattr(Boolean.class, Slots.store);
    }

    /** get slot index */
    boolean _index() {
        return getattr(Boolean.class, Slots.index);
    }

    /** get slot manual */
    boolean _manual() {
        return getattr(Boolean.class, Slots.manual);
    }

    /** get slot copy */
    boolean _copy() {
        return getattr(Boolean.class, Slots.copy);
    }

    /** get slot depends_context */
    public Collection<String> _depends_context() {
        if (_slots.containsKey(Slots.depends_context)) {
            return (Collection<String>) _slots.get(Slots.depends_context);
        }
        return Tuple.emptyTuple();
    }

    /** get slot recursive */
    boolean _recursive() {
        return getattr(Boolean.class, Slots.recursive);
    }

    /** get slot compute_sudo */
    boolean _compute_sudo() {
        return getattr(Boolean.class, Slots.compute_sudo);
    }

    /** get slot company_dependent */
    boolean _company_dependent() {
        return getattr(Boolean.class, Slots.company_dependent);
    }

    /** get slot readonly */
    public boolean _readonly() {
        return getattr(Boolean.class, Slots.readonly);
    }

    /** get slot required */
    public boolean _required() {
        return getattr(Boolean.class, Slots.required);
    }

    /** get slot change_default */
    public boolean _change_default() {
        return getattr(Boolean.class, Slots.change_default);
    }

    /** get slot deprecated */
    public boolean _deprecated() {
        return getattr(Boolean.class, Slots.deprecated);
    }

    /** get slot prefetch */
    public boolean _prefetch() {
        return getattr(Boolean.class, Slots.prefetch);
    }

    public static class Slots {
        /** the field's module name */
        public static final Slot _module = new Slot("_module", null);
        /** modules that define this field */
        public static final Slot _modules = new Slot("_modules", null);
        /** whether the field is automatically created ("magic" field) */
        public static final Slot automatic = new Slot("automatic", false);
        /** whether the field is inherited (_inherits) */
        public static final Slot inherited = new Slot("inherited", false);
        /** the corresponding inherited field */
        public static final Slot inherited_field = new Slot("inherited_field", null);
        /** name of the model of values (if relational) */
        public static final Slot comodel_name = new Slot("comodel_name", null);
        /** whether the field is stored in database */
        public static final Slot store = new Slot("store", true);
        /** whether the field is indexed in database */
        public static final Slot index = new Slot("index", false);
        /** whether the field is a custom field */
        public static final Slot manual = new Slot("manual", false);
        /** whether the field is copied over by BaseModel.copy() */
        public static final Slot copy = new Slot("copy", true);
        /** collection of field dependencies */
        public static final Slot depends = new Slot("depends", null);
        /** collection of context key dependencies */
        public static final Slot depends_context = new Slot("depends_context", null);
        /** whether self depends on itself */
        public static final Slot recursive = new Slot("recursive", false);
        /** compute(recs) computes field on recs */
        public static final Slot compute = new Slot("compute", null);
        /** whether field should be recomputed as superuser */
        public static final Slot compute_sudo = new Slot("compute_sudo", true);
        /** inverse(recs) inverses field on recs */
        public static final Slot inverse = new Slot("inverse", null);
        /** search(recs, operator, value) searches on self */
        public static final Slot search = new Slot("search", null);
        /** sequence of field names, for related fields */
        public static final Slot related = new Slot("related", null);
        /** whether ``self`` is company-dependent (property field) */
        public static final Slot company_dependent = new Slot("company_dependent", false);
        /** default(recs) returns the default value */
        public static final Slot $default = new Slot("default", null);
        /** field label */
        public static final Slot string = new Slot("string", null);
        /** field tooltip */
        public static final Slot help = new Slot("help", null);
        /** whether the field is readonly */
        public static final Slot readonly = new Slot("readonly", false);
        /** whether the field is required */
        public static final Slot required = new Slot("required", false);
        /** set readonly and required depending on state */
        public static final Slot states = new Slot("states", null);
        /** csv list of group xml ids */
        public static final Slot groups = new Slot("groups", null);
        /** whether the field may trigger a "user-onchange" */
        public static final Slot change_default = new Slot("change_default", false);
        /** whether the field is deprecated */
        public static final Slot deprecated = new Slot("deprecated", false);
        /** corresponding related field */
        public static final Slot related_field = new Slot("related_field", null);
        /** operator for aggregating values */
        public static final Slot group_operator = new Slot("group_operator", null);
        /** name of method to expand groups in read_group() */
        public static final Slot group_expand = new Slot("group_expand", null);
        /** whether the field is prefetched */
        public static final Slot prefetch = new Slot("prefetch", true);
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
