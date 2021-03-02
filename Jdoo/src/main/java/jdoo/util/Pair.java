package jdoo.util;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.springframework.util.ObjectUtils;

@JsonDeserialize(using = PairJsonDeserializer.class)
public class Pair<P0, P1> extends AbstractCollection<Object> {
    private P0 first;
    private P1 second;

    public Pair(P0 first, P1 second) {
        this.first = first;
        this.second = second;
    }

    public P0 first() {
        return first;
    }

    public P1 second() {
        return second;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair<?, ?> other = (Pair<?, ?>) obj;
            return ObjectUtils.nullSafeEquals(first, other.first) && ObjectUtils.nullSafeEquals(second, other.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (first == null ? 0 : first.hashCode());
        hash = 31 * hash + (second == null ? 0 : second.hashCode());
        return hash;
    }

    @Override
    public String toString() {
        return String.format("[%s,%s]", getString(first), getString(second));
    }

    String getString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + obj + "\"";
        }
        return obj.toString();
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<Object>() {
            int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < 2;
            }

            @Override
            public Object next() {
                if (cursor++ == 0) {
                    return first;
                } else {
                    return second;
                }
            }
        };
    }

    @Override
    public int size() {
        return 2;
    }
}

class PairJsonDeserializer extends JsonDeserializer<Pair<?, ?>> {

    @Override
    public Pair<?, ?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        List<Object> list = parser.readValueAs(new TypeReference<List<Object>>() {
        });
        if (list.size() != 2) {
            throw new JsonParseException(parser, "pair must be two elements");
        }
        return new Pair<>(list.get(0), list.get(1));
    }
}