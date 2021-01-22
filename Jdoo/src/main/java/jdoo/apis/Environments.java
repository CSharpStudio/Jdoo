package jdoo.apis;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jdoo.tools.IdValues;

public class Environments implements Iterable<Environment> {
    Set<WeakReference<Environment>> envs = new HashSet<WeakReference<Environment>>();
    Cache cache = new Cache();
    // updates {model: {id: {field: value}}}
    HashMap<String, IdValues> towrite = new HashMap<String, IdValues>();

    public Cache cache() {
        return cache;
    }

    public HashMap<String, IdValues> towrite() {
        return towrite;
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
