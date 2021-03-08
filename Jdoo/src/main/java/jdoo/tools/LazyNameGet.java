package jdoo.tools;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jdoo.models.RecordSet;
import jdoo.util.Pair;

public class LazyNameGet extends AbstractList<Pair<Object, String>> {
    RecordSet recs;
    Map<Object, String> map;

    Map<Object, String> getMap() {
        if (map == null) {
            List<Pair<Object, String>> list = recs.name_get();
            map = list.stream().collect(Collectors.toMap(p -> p.first(), p -> p.second()));
        }
        return map;
    }

    public LazyNameGet(RecordSet recs) {
        this.recs = recs;
    }

    @Override
    public Pair<Object, String> get(int index) {
        Object id = recs.ids().get(index);
        return new LazyPair(id, k -> getMap().get(k));
    }

    @Override
    public int size() {
        return recs.size();
    }

    class LazyPair extends Pair<Object, String> {
        String second;
        Function<Object, String> func;

        public LazyPair(Object first, Function<Object, String> func) {
            super(first, null);
            this.func = func;
        }

        @Override
        public String second() {
            if (second == null) {
                second = func.apply(first());
            }
            return second;
        }
    }
}
