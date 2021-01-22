package jdoo.models;

import java.util.HashMap;
import java.util.Set;

import jdoo.tools.Func;
import jdoo.tools.Tuple2;

public class BaseField<T extends BaseField<T>> extends Field {

    @SuppressWarnings("unchecked")
    public T automatic(boolean automatic) {
        this.automatic = automatic;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T model(String model) {
        this.model_name = model;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T store(boolean store) {
        this.store = store;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T index(boolean index) {
        this.index = index;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T copy(boolean copy) {
        this.copy = copy;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T depends(String[] depends) {
        this.depends = depends;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T compute(String compute) {
        this.compute = compute;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T compute_sudo(boolean compute_sudo) {
        this.compute_sudo = compute_sudo;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T inverse(String inverse) {
        this.inverse = inverse;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T search(String search) {
        this.search = search;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T related(String related) {
        this.related = related;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T company_dependent(boolean company_dependent) {
        this.company_dependent = company_dependent;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T string(String string) {
        this.string = string;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T help(String help) {
        this.help = help;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T readonly(boolean readonly) {
        this.readonly = readonly;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T required(boolean required) {
        this.required = required;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T states(HashMap<String, Set<Tuple2<String, Boolean>>> states) {
        this.states = states;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T groups(String groups) {
        this.groups = groups;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T group_operator(String group_operator) {
        this.group_operator = group_operator;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T default_(Object defaultValue) {
        this.default_value = defaultValue;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T default_(Func<Object, Self> defaultFunc) {
        this.default_ = defaultFunc;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T change_default(boolean change_default){
        this.change_default = change_default;
        return (T)this;
    }
}