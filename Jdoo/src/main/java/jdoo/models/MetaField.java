package jdoo.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

import jdoo.util.Dict;
import jdoo.util.Pair;
import jdoo.util.Tuple;

public abstract class MetaField {
    private String name;
    private MetaModel meta;

    public void setMeta(MetaModel meta) {
        this.meta = meta;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaField) {
            MetaField field = (MetaField) obj;
            return field.meta.getName() == meta.getName() && field.name == name;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (meta.getName() + "@" + name).hashCode();
    }

    protected Dict context;

    Dict context() {
        if (context == null) {
            context = new Dict();
        }
        return context;
    }

    protected String column_format = "%s";

    protected boolean relational = false;

    boolean relational() {
        return relational;
    }

    protected Boolean translate;

    boolean translate() {
        return translate != null && translate;
    }

    Tuple<String> column_cast_from;

    Tuple<String> column_cast_from() {
        if (column_cast_from == null)
            column_cast_from = Tuple.emptyTuple();
        return column_cast_from;
    }

    Pair<String, Object> column_type;

    Pair<String, Object> column_type() {
        return column_type;
    }

    protected String _module;

    String module() {
        return _module;
    }

    protected Boolean automatic;

    boolean automatic() {
        return automatic != null && automatic;
    }

    protected boolean inherited;

    boolean inherited() {
        return inherited;
    }

    public String model_name() {
        return meta.getName();
    }

    protected String comodel_name;

    protected Boolean store;

    boolean store() {
        return store == null || store;// default is true
    }

    protected Boolean index;

    boolean index() {
        return index != null && index;
    }

    protected boolean menual;

    boolean menual() {
        return menual;
    }

    protected Boolean copy;

    boolean copy() {
        return copy == null || copy;// default is true
    }

    protected String[] depends;

    String[] depends() {
        if (depends == null)
            depends = new String[0];
        return depends;
    }

    public void set_depends(String[] depends) {
        this.depends = depends;
    }

    protected Set<String> depends_context;

    public Set<String> depends_context() {
        if (depends_context == null)
            depends_context = Collections.emptySet();
        return depends_context;
    }

    protected Boolean recursive;

    boolean recursive() {
        return recursive != null && recursive;
    }

    protected String compute;

    protected Boolean compute_sudo;

    boolean compute_sudo() {
        return compute_sudo == null || compute_sudo;// default is true
    }

    protected String inverse;

    protected String search;

    protected String related;

    protected Boolean company_dependent;

    boolean company_dependent() {
        return company_dependent != null && company_dependent;
    }

    protected Function<RecordSet, Object> default_;
    protected Object default_value;

    Object get_default_value(RecordSet self) {
        if (default_ != null)
            return default_.apply(self);
        return default_value;
    }

    protected String string;

    protected String help;

    protected Boolean readonly;

    boolean readonly() {
        return readonly != null && readonly;
    }

    protected Boolean required;

    boolean required() {
        return required != null && required;
    }

    protected HashMap<String, Set<State>> states;

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

    protected String groups;

    protected Boolean change_default;

    boolean change_default() {
        return change_default != null && change_default;
    }

    protected Boolean deprecated;

    boolean deprecated() {
        return deprecated != null && deprecated;
    }

    protected Field related_field;

    protected String group_operator;

    protected String group_expand;

    protected boolean prefetch = true;

    boolean prefetch() {
        return prefetch;
    }

}
