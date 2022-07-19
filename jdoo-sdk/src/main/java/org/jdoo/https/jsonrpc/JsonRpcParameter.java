package org.jdoo.https.jsonrpc;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * json rpc 参数
 * 
 * @author lrz
 */
@JsonSerialize(using = JsonRpcParameterJsonSerializer.class)
@JsonDeserialize(using = JsonRpcParameterJsonDeserializer.class)
public class JsonRpcParameter {
    Map<String, Object> keyValues;
    List<Object> listValues;
    boolean isList = true;

    public JsonRpcParameter(Map<String, Object> keyValues) {
        this.keyValues = keyValues;
        isList = false;
    }

    public JsonRpcParameter(List<Object> listValues) {
        this.listValues = listValues;
        isList = false;
    }

    public boolean isList() {
        return isList;
    }

    public Map<String, Object> getMap() {
        if (isList) {
            throw new Error("list value cannot get map");
        }
        return keyValues;
    }

    public List<Object> getList() {
        if (!isList) {
            throw new Error("map value cannot get list");
        }
        return listValues;
    }
}

class JsonRpcParameterJsonDeserializer extends JsonDeserializer<JsonRpcParameter> {

    @Override
    public JsonRpcParameter deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.START_ARRAY) {
            ArrayList<Object> list = parser.readValueAs(new TypeReference<ArrayList<Object>>() {
            });
            return new JsonRpcParameter(list);
        }
        if (token == JsonToken.START_OBJECT) {
            HashMap<String, Object> map = parser.readValueAs(new TypeReference<HashMap<String, Object>>() {
            });
            return new JsonRpcParameter(map);
        }
        throw new IOException("parames must be array or ditionary");
    }
}

class JsonRpcParameterJsonSerializer extends JsonSerializer<JsonRpcParameter> {

    @Override
    public void serialize(JsonRpcParameter value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value.isList()) {
            gen.writeObject(value.getList());
        } else {
            gen.writeObject(value.getMap());
        }
    }
}