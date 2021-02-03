package jdoo.models._fields;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

import jdoo.models.Field;
import jdoo.models.RecordSet;

public class BaseField<T extends BaseField<T>> extends Field {

    @SuppressWarnings("unchecked")
    public T automatic(boolean automatic) {
        set(Field.automatic, automatic);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T store(boolean store) {
        set(Field.store, store);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T index(boolean index) {
        set(Field.index, index);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T copy(boolean copy) {
        set(Field.copy, copy);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T depends(String[] depends) {
        set(Field.depends, depends);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T compute(String compute) {
        set(Field.compute, compute);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T compute_sudo(boolean compute_sudo) {
        set(Field.compute_sudo, compute_sudo);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T inverse(String inverse) {
        set(Field.inverse, inverse);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T search(String search) {
        set(Field.search, search);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T related(String related) {
        set(Field.related, Arrays.asList(related));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T company_dependent(boolean company_dependent) {
        set(Field.company_dependent, company_dependent);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T string(String string) {
        set(Field.string, string);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T help(String help) {
        set(Field.help, help);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T readonly(boolean readonly) {
        set(Field.readonly, readonly);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T required(boolean required) {
        set(Field.required, required);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T states(HashMap<String, Set<State>> states) {
        set(Field.states, states);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T groups(String groups) {
        set(Field.groups, groups);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T group_operator(String group_operator) {
        set(Field.group_operator, group_operator);
        return (T) this;
    }

    public T $default(Object defaultValue) {
        return $default(self -> defaultValue);
    }

    @SuppressWarnings("unchecked")
    public T $default(Function<RecordSet, Object> defaultFunc) {
        set(Field.$default, defaultFunc);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T change_default(boolean change_default) {
        set(Field.change_default, change_default);
        return (T) this;
    }
}
