package jdoo.models;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

import jdoo.util.Dict;
import jdoo.util.Pair;
import jdoo.util.Tuple;

/**
 * Meta attrs for field classes
 */
public abstract class MetaField {
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

    /** the field's module name */
    String _module;
    /** modules that define this field */
    String _modules;
    /** absolute ordering of the field */
    int _sequence;

    /** whether the field is automatically created ("magic" field) */
    protected Boolean automatic;
    /** whether the field is inherited (_inherits) */
    boolean inherited;
    /** the corresponding inherited field */
    Field inherited_field;

    /** name of the field */
    String name;
    /** name of the model of this field */
    String model_name;
    /** name of the model of values (if relational) */
    protected String comodel_name;

    /** whether the field is stored in database */
    protected Boolean store;
    /** whether the field is indexed in database */
    protected Boolean index;
    /** whether the field is a custom field */
    protected boolean menual;
    /** whether the field is copied over by BaseModel.copy() */
    protected Boolean copy;
    /** collection of field dependencies */
    protected String[] depends;
    /** collection of context key dependencies */
    protected Set<String> depends_context;
    /** whether self depends on itself */
    protected Boolean recursive;
    /** compute(recs) computes field on recs */
    protected String compute;
    /** whether field should be recomputed as superuser */
    protected Boolean compute_sudo;
    /** inverse(recs) inverses field on recs */
    protected String inverse;
    /** search(recs, operator, value) searches on self */
    protected String search;
    /** sequence of field names, for related fields */
    protected Collection<String> related;
    /** whether ``self`` is company-dependent (property field) */
    protected Boolean company_dependent;
    /** default(recs) returns the default value */
    protected Function<RecordSet, Object> $default;

    /** field label */
    protected String string;
    /** field tooltip */
    protected String help;
    /** whether the field is readonly */
    protected Boolean readonly;
    /** whether the field is required */
    protected Boolean required;
    /** set readonly and required depending on state */
    protected HashMap<String, Set<State>> states;
    /** csv list of group xml ids */
    protected String groups;
    /** whether the field may trigger a "user-onchange" */
    protected Boolean change_default;
    /** whether the field is deprecated */
    protected Boolean deprecated;

    /** corresponding related field */
    protected Field related_field;
    /** operator for aggregating values */
    protected String group_operator;
    /** name of method to expand groups in read_group() */
    protected String group_expand;
    /** whether the field is prefetched */
    protected boolean prefetch = true;
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
        return _module;
    }

    boolean automatic() {
        return automatic != null && automatic;
    }

    boolean inherited() {
        return inherited;
    }

    public String model_name() {
        return model_name;
    }

    boolean store() {
        return store == null || store;// default is true
    }

    boolean index() {
        return index != null && index;
    }

    boolean menual() {
        return menual;
    }

    boolean copy() {
        return copy == null || copy;// default is true
    }

    String[] depends() {
        if (depends == null)
            depends = new String[0];
        return depends;
    }

    public void set_depends(String[] depends) {
        this.depends = depends;
    }

    public Set<String> depends_context() {
        if (depends_context == null)
            depends_context = Collections.emptySet();
        return depends_context;
    }

    boolean recursive() {
        return recursive != null && recursive;
    }

    boolean compute_sudo() {
        return compute_sudo == null || compute_sudo;// default is true
    }

    boolean company_dependent() {
        return company_dependent != null && company_dependent;
    }

    boolean readonly() {
        return readonly != null && readonly;
    }

    boolean required() {
        return required != null && required;
    }

    HashMap<String, Set<State>> states() {
        if (states == null)
            states = new HashMap<String, Set<State>>();
        return states;
    }

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

    boolean change_default() {
        return change_default != null && change_default;
    }

    boolean deprecated() {
        return deprecated != null && deprecated;
    }

    boolean prefetch() {
        return prefetch;
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
}
