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
        setattr(Slots.automatic, automatic);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T store(boolean store) {
        setattr(Slots.store, store);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T index(boolean index) {
        setattr(Slots.index, index);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T copy(boolean copy) {
        setattr(Slots.copy, copy);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T depends(String[] depends) {
        setattr(Slots.depends, depends);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T compute(String compute) {
        setattr(Slots.compute, compute);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T compute_sudo(boolean compute_sudo) {
        setattr(Slots.compute_sudo, compute_sudo);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T inverse(String inverse) {
        setattr(Slots.inverse, inverse);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T search(String search) {
        setattr(Slots.search, search);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T related(String related) {
        setattr(Slots.related, Arrays.asList(related));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T company_dependent(boolean company_dependent) {
        setattr(Slots.company_dependent, company_dependent);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T string(String string) {
        setattr(Slots.string, string);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T help(String help) {
        setattr(Slots.help, help);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T readonly(boolean readonly) {
        setattr(Slots.readonly, readonly);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T required(boolean required) {
        setattr(Slots.required, required);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T states(HashMap<String, Set<State>> states) {
        setattr(Slots.states, states);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T groups(String groups) {
        setattr(Slots.groups, groups);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T group_operator(String group_operator) {
        setattr(Slots.group_operator, group_operator);
        return (T) this;
    }

    public T $default(Object defaultValue) {
        return $default(self -> defaultValue);
    }

    @SuppressWarnings("unchecked")
    public T $default(Function<RecordSet, Object> defaultFunc) {
        setattr(Slots.$default, defaultFunc);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T change_default(boolean change_default) {
        setattr(Slots.change_default, change_default);
        return (T) this;
    }
}
