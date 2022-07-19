package org.jdoo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdoo.data.BinaryOp;
import org.jdoo.exceptions.ModelException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 过滤查询
 * 
 * @author lrz
 */
@JsonDeserialize(using = CriteriaJsonDeserializer.class)
public class Criteria extends ArrayList<Object> {

    public Criteria() {

    }

    public Criteria(BinaryOp bo) {
        add(bo);
    }

    public static Criteria binary(String field, String op, Object value) {
        return new Criteria(new BinaryOp(field, op, value));
    }

    public static Criteria equal(String field, Object value) {
        return new Criteria(new BinaryOp(field, "=", value));
    }

    public static Criteria notEqual(String field, Object value) {
        return new Criteria(new BinaryOp(field, "!=", value));
    }

    public static Criteria greater(String field, Object value) {
        return new Criteria(new BinaryOp(field, ">", value));
    }

    public static Criteria greaterOrEqual(String field, Object value) {
        return new Criteria(new BinaryOp(field, ">=", value));
    }

    public static Criteria less(String field, Object value) {
        return new Criteria(new BinaryOp(field, "<", value));
    }

    public static Criteria lessOrEqual(String field, Object value) {
        return new Criteria(new BinaryOp(field, "<=", value));
    }

    public static Criteria like(String field, Object value) {
        return new Criteria(new BinaryOp(field, "like", value));
    }

    public static Criteria notLike(String field, Object value) {
        return new Criteria(new BinaryOp(field, "!like", value));
    }

    public static Criteria ilike(String field, Object value) {
        return new Criteria(new BinaryOp(field, "ilike", value));
    }

    public static Criteria in(String field, Object value) {
        return new Criteria(new BinaryOp(field, "in", value));
    }

    public static Criteria notIn(String field, Object value) {
        return new Criteria(new BinaryOp(field, "!in", value));
    }

    public static Criteria childOf(String field, Object value) {
        return new Criteria(new BinaryOp(field, "child_of", value));
    }

    public static Criteria parentOf(String field, Object value) {
        return new Criteria(new BinaryOp(field, "parent_of", value));
    }

    public Criteria and(String field, String op, Object value) {
        if (size() > 0) {
            add(0, "&");
        }
        add(new BinaryOp(field, op, value));
        return this;
    }

    public Criteria and(Criteria... criterias) {
        for (Criteria criteria : criterias) {
            if (size() > 0) {
                add(0, "&");
            }
            addAll(criteria);
        }
        return this;
    }

    public Criteria or(String field, String op, Object value) {
        if (size() > 0) {
            add(0, "|");
        }
        add(new BinaryOp(field, op, value));
        return this;
    }

    public Criteria or(Criteria... criterias) {
        for (Criteria criteria : criterias) {
            if (size() > 0) {
                add(0, "|");
            }
            addAll(criteria);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public static Criteria parse(List<Object> list) {
        Criteria criteria = new Criteria();
        for (Object obj : list) {
            if (obj instanceof String) {
                criteria.add(obj);
            } else if (obj instanceof Map<?, ?>) {
                Map<String, Object> map = (Map<String, Object>) obj;
                BinaryOp binaryOp = new BinaryOp((String) map.get("field"), (String) map.get("op"), map.get("value"));
                criteria.add(binaryOp);
            } else if (obj instanceof List<?>) {
                List<Object> args = (List<Object>) obj;
                if (args.size() == 3) {
                    BinaryOp binaryOp = new BinaryOp((String) args.get(0), (String) args.get(1),
                            args.get(2));
                    criteria.add(binaryOp);
                }
            }
        }
        return criteria;
    }

    public static Criteria parse(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Criteria.class);
        } catch (Exception e) {
            throw new ModelException(String.format("无法把[%s]解析为Criteria", json));
        }
    }
}

class CriteriaJsonDeserializer extends JsonDeserializer<Criteria> {

    @Override
    public Criteria deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        ArrayList<Object> list = null;
        if (token == JsonToken.START_ARRAY) {
            list = parser.readValueAs(new TypeReference<ArrayList<?>>() {
            });
        } else if (token == JsonToken.START_OBJECT) {
            HashMap<String, ?> map = parser.readValueAs(new TypeReference<HashMap<String, ?>>() {
            });
            list = new ArrayList<>();
            list.add(map);
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return Criteria.parse(list);
    }
}