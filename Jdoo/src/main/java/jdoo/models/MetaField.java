package jdoo.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import jdoo.tools.Dict;
import jdoo.tools.Func;
import jdoo.tools.Tuple;
import jdoo.tools.Tuple2;

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

    Tuple2<String, Object> column_type;

    Tuple2<String, Object> column_type() {
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

    protected Func<Object, Self> default_;
    protected Object default_value;

    Object get_default_value(Self self) {
        if (default_ != null)
            return default_.call(self);
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

    protected HashMap<String, Set<Tuple2<String, Boolean>>> states;

    HashMap<String, Set<Tuple2<String, Boolean>>> states() {
        if (states == null)
            states = new HashMap<String, Set<Tuple2<String, Boolean>>>();
        return states;
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
