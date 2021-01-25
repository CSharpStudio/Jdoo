package jdoo.apis;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdoo.models.Field;
import jdoo.tools.IdValues;
import jdoo.tools.StackMap;

public class Environments implements Iterable<Environment> {
    Set<WeakReference<Environment>> envs = new HashSet<WeakReference<Environment>>();// weak set of environments
    Cache cache = new Cache();// cache for all records
    StackMap<Field, Collection<String>> $protected = new StackMap<>();// fields to protect {field: ids, ...}
    Map<Field, List<String>> tocompute = new HashMap<Field, List<String>>();// recomputations {field: ids}
    // updates {model: {id: {field: value}}}
    HashMap<String, IdValues> towrite = new HashMap<String, IdValues>();

    public Cache cache() {
        return cache;
    }

    public StackMap<Field, Collection<String>> $protected() {
        return $protected;
    }

    public HashMap<String, IdValues> towrite() {
        return towrite;
    }

    public Map<Field, List<String>> tocompute() {
        return tocompute;
    }

    public List<String> tocompute(Field field) {
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
