package jdoo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import jdoo.models.Self;
import jdoo.tools.Dict;
import jdoo.tools.TypeUtils;
import jdoo.https.json.JsonRpcRequest;
import jdoo.https.json.RpcId;
import jdoo.apis.Environment;
import jdoo.data.Cursor;
import jdoo.base.ResUsers;
import jdoo.modules.Registry;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void callMethodWithName() {
        Registry registry = new Registry();
        registry.register(ResUsers.class);
        Cursor cr = new Cursor();
        Environment env = Environment.create("key", registry, cr, "uid", new Dict(), false);
        Self m = env.get("res.users");
        Object n = m.call(String.class, "name_get");

        System.out.println(n);
        assertTrue(true);
    }

    @Test
    public void rpcIdJsonDeserialize() throws JsonParseException, JsonMappingException, IOException {
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
    public void rpcIdJsonSerialize() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcRequest request = new JsonRpcRequest();
        request.setId(new RpcId(1234L));
        String json = mapper.writeValueAsString(request);
        System.out.println(json);
    }

    @Test
    public void rpcParamsJsonDeserialize() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        String listParams = "{\"params\":[1,2.2,\"str\",\"2020-11-03 06:10:54\",{}]}";
        JsonRpcRequest list_req = mapper.readValue(listParams, JsonRpcRequest.class);
        System.out.println(list_req.getParams());

        String mapParams = "{\"params\":{\"lang\":\"zh_CN\",\"uid\":2,\"dt\":\"2020-11-03 06:10:54\",\"allowed_company_ids\":[1]}}";
        JsonRpcRequest map_req = mapper.readValue(mapParams, JsonRpcRequest.class);
        System.out.println(map_req.getParams());
    }

    @Test
    public void parseValues() throws ParseException {
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
