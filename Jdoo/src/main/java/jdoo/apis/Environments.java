package jdoo.apis;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jdoo.models.Field;
import jdoo.tools.IdValues;
import jdoo.tools.StackMap;
import jdoo.util.DefaultDict;

public class Environments implements Iterable<Environment> {
    Set<WeakReference<Environment>> envs = new HashSet<WeakReference<Environment>>();// weak set of environments
    Cache cache = new Cache();// cache for all records
    StackMap<Field, Collection<Object>> $protected = new StackMap<>();// fields to protect {field: ids, ...}
    Map<Field, Set<String>> tocompute = new DefaultDict<>(HashSet::new);// recomputations
    // {field: ids}
    // updates {model: {id: {field: value}}}
    Map<String, IdValues> towrite = new DefaultDict<>(IdValues.class);

    public Cache cache() {
        return cache;
    }

    public StackMap<Field, Collection<Object>> $protected() {
        return $protected;
    }

    public Map<String, IdValues> towrite() {
        return towrite;
    }

    public Map<Field, Set<String>> tocompute() {
        return tocompute;
    }

    public Collection<String> tocompute(Field field) {
        if (!tocompute.containsKey(field)) {
            return Collections.emptyList();
        }
        return tocompute.get(field);
    }

    public IdValues towrite(String model) {
        if (!towrite.containsKey(model)) {
            IdValues values = new IdValues();
            towrite.put(model, values);
            return values;
        }
        return towrite.get(model);
    }

    public void add(Environment env) {
        envs.add(new WeakReference<Environment>(env));
    }

    @Override
    public Iterator<Environment> iterator() {
        return new EnvironmentsIterator(envs.iterator());
    }

    class EnvironmentsIterator implements Iterator<Environment> {
        Iterator<WeakReference<Environment>> iterator;

        public EnvironmentsIterator(Iterator<WeakReference<Environment>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Environment next() {
            return iterator.next().get();
        }

    }
}
