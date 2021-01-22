package jdoo.https.json;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

@JsonSerialize(using = JsonRpcParameterJsonSerializer.class)
@JsonDeserialize(using = JsonRpcParameterJsonDeserializer.class)
public class JsonRpcParameter {
    Map<String, ?> keyValues;
    List<?> listValues;
    boolean isList = true;

    public JsonRpcParameter(Map<String, ?> keyValues) {
        this.keyValues = keyValues;
        isList = false;
    }

    public JsonRpcParameter(List<?> listValues) {
        this.listValues = listValues;
        isList = false;
    }

    public boolean isList() {
        return isList;
    }

    public Map<String, ?> getMap() {
        if (isList)
            throw new Error("list value cannot get map");
        return keyValues;
    }

    public List<?> getList() {
        if (!isList)
            throw new Error("map value cannot get list");
        return listValues;
    }
}

class JsonRpcParameterJsonDeserializer extends JsonDeserializer<JsonRpcParameter> {

    @Override
    public JsonRpcParameter deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.START_ARRAY) {
            ArrayList<?> list = parser.readValueAs(new TypeReference<ArrayList<?>>() {
            });
            return new JsonRpcParameter(list);
        }
        if (token == JsonToken.START_OBJECT) {
            HashMap<String, ?> map = parser.readValueAs(new TypeReference<HashMap<String, ?>>() {
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
        ObjectMapper mapper = new ObjectMapper();
        if (value.isList()) {
            String json = mapper.writeValueAsString(value.getList());
            gen.writeRaw(json.substring(1, json.length() - 2));
        } else {
            String json = mapper.writeValueAsString(value.getMap());
            gen.writeRaw(json.substring(1, json.length() - 2));
        }
    }

}