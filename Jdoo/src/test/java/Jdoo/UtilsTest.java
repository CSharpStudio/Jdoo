package jdoo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.util.TypeUtils;
import jdoo.https.json.JsonRpcRequest;
import jdoo.https.json.RpcId;

/**
 * Unit test for simple App.
 */
public class UtilsTest {
    @Test
    public void test_tuple() {
        Tuple<?> a = Tuple.fromCollection(Arrays.asList(1, 2));
        Tuple<?> b = new Tuple<>(1, 2);
        assertEquals(2, a.size());
        assertEquals(b, a);
    }

    @Test
    public void test_pair_json_deserialize() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[1,\"str\"]";
        Pair<Integer, String> pair = mapper.readValue(json, new TypeReference<Pair<Integer, String>>() {
        });
        System.out.println(pair);
        assertEquals(1, (int) pair.first());
        assertEquals("str", pair.second());
    }

    @Test
    public void test_pair_json_serialize() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new Pair<>(1, "str"));
        System.out.println(json);
        assertEquals("[1,\"str\"]", json);
    }

    @Test
    public void test_tuple_json_deserialize() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[1,\"str\"]";
        Tuple<Object> tuple = mapper.readValue(json, new TypeReference<Tuple<Object>>() {
        });
        System.out.println(tuple);
        assertEquals(1, tuple.get(0));
        assertEquals("str", tuple.get(1));
    }

    @Test
    public void test_tuple_json_serialize() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new Tuple<>(1, "str"));
        System.out.println(json);
        assertEquals("[1,\"str\"]", json);
    }

    @Test
    public void test_rpcid_json_deserialize() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        String strId = "{\"id\":\"x\"}";
        JsonRpcRequest str_req = mapper.readValue(strId, JsonRpcRequest.class);
        System.out.println(str_req.getId().getId());

        String longId = "{\"id\":123}";
        JsonRpcRequest long_req = mapper.readValue(longId, JsonRpcRequest.class);
        System.out.println(long_req.getId().getId());

        try {

            String doubleId = "{\"id\":1.2}";
            JsonRpcRequest double_req = mapper.readValue(doubleId, JsonRpcRequest.class);
            System.out.println(double_req.getId().getId());
            assertTrue(false);
        } catch (IOException e) {
        }

        try {

            String objId = "{\"id\":{}}";
            JsonRpcRequest obj_req = mapper.readValue(objId, JsonRpcRequest.class);
            System.out.println(obj_req.getId().getId());
            assertTrue(false);
        } catch (IOException e) {
        }
    }

    @Test
    public void test_rpcid_json_serialize() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcRequest request = new JsonRpcRequest();
        request.setId(new RpcId(1234L));
        String json = mapper.writeValueAsString(request);
        System.out.println(json);
    }

    @Test
    public void test_rpcparams_json_deserialize() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        String listParams = "{\"params\":[1,2.2,\"str\",\"2020-11-03 06:10:54\",{}]}";
        JsonRpcRequest list_req = mapper.readValue(listParams, JsonRpcRequest.class);
        System.out.println(list_req.getParams());

        String mapParams = "{\"params\":{\"lang\":\"zh_CN\",\"uid\":2,\"dt\":\"2020-11-03 06:10:54\",\"allowed_company_ids\":[1]}}";
        JsonRpcRequest map_req = mapper.readValue(mapParams, JsonRpcRequest.class);
        System.out.println(map_req.getParams());
    }

    @Test
    public void test_typeutils_parse_values() throws ParseException {
        int v1 = TypeUtils.parse("1", int.class);
        assertEquals(1, v1);
        int v2 = TypeUtils.parse("1", Integer.class);
        assertEquals(1, v2);

        boolean v3 = TypeUtils.parse("true", boolean.class);
        assertTrue(v3);
        boolean v4 = TypeUtils.parse("true", Boolean.class);
        assertTrue(v4);

        float v5 = TypeUtils.parse("1.2", float.class);
        assertEquals(1.2, v5, 0.001);
        float v6 = TypeUtils.parse("1.2", Float.class);
        assertEquals(1.2, v6, 0.001);

        Date v7 = TypeUtils.parse("2021-1-4 19:36:50", Date.class);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-1-4 19:36:50"), v7);

        Integer[] v8 = TypeUtils.parse("[1,2,3]", Integer[].class);
        assertEquals(3, v8.length);
    }
}
