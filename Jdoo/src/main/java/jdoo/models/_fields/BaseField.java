package jdoo.models._fields;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import jdoo.models.Field;
import jdoo.models.RecordSet;
import jdoo.util.Pair;

public class BaseField<T extends BaseField<T>> extends Field {

    public T automatic(boolean automatic) {
        setattr(Slots.automatic, automatic);
        return (T) this;
    }

    public T store(boolean store) {
        setattr(Slots.store, store);
        return (T) this;
    }

    public T index(boolean index) {
        setattr(Slots.index, index);
        return (T) this;
    }

    public T copy(boolean copy) {
        setattr(Slots.copy, copy);
        return (T) this;
    }

    public T depends(String[] depends) {
        setattr(Slots.depends, depends);
        return (T) this;
    }

    public T compute(String compute) {
        setattr(Slots.compute, compute);
        return (T) this;
    }

    public T compute_sudo(boolean compute_sudo) {
        setattr(Slots.compute_sudo, compute_sudo);
        return (T) this;
    }

    public T inverse(String inverse) {
        setattr(Slots.inverse, inverse);
        return (T) this;
    }

    public T inverse(Consumer<RecordSet> inverse) {
        setattr(Slots.inverse, inverse);
        return (T) this;
    }

    public T search(String search) {
        setattr(Slots.search, search);
        return (T) this;
    }

    public T search(BiFunction<String, Object, List<Object>> search) {
        setattr(Slots.search, search);
        return (T) this;
    }

    public T related(String related) {
        setattr(Slots.related, Arrays.asList(related.split("\\.")));
        return (T) this;
    }

    public T company_dependent(boolean company_dependent) {
        setattr(Slots.company_dependent, company_dependent);
        return (T) this;
    }

    public T string(String string) {
        setattr(Slots.string, string);
        return (T) this;
    }

    public T help(String help) {
        setattr(Slots.help, help);
        return (T) this;
    }

    public T readonly(boolean readonly) {
        setattr(Slots.readonly, readonly);
        return (T) this;
    }

    public T required(boolean required) {
        setattr(Slots.required, required);
        return (T) this;
    }

    public T states(HashMap<String, Collection<Pair<String, Boolean>>> states) {
        setattr(Slots.states, states);
        return (T) this;
    }

    public T groups(String groups) {
        setattr(Slots.groups, groups);
        return (T) this;
    }

    public T group_operator(String group_operator) {
        setattr(Slots.group_operator, group_operator);
        return (T) this;
    }

    public T $default(Object defaultValue) {
        return $default(self -> defaultValue);
    }

    public T $default(Function<RecordSet, Object> defaultFunc) {
        setattr(Slots.$default, defaultFunc);
        return (T) this;
    }

    public T change_default(boolean change_default) {
        setattr(Slots.change_default, change_default);
        return (T) this;
    }

    public T inherited(boolean inherited) {
        setattr(Slots.inherited, inherited);
        return (T) this;
    }

    public T invisible(boolean invisible){
        return (T) this;
    }
}
