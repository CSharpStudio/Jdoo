package jdoo.https;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import jdoo.data.Cursor;
import jdoo.apis.Environment;
import jdoo.tools.Dict;
import jdoo.https.json.JsonRpcParseException;
import jdoo.https.json.JsonRpcRequest;
import jdoo.models.Self;

public class Controller {
    @Autowired
    protected HttpServletRequest httpServletRequest;

    public Controller() {
    }

    public Environment env(){
        Cursor cr = new Cursor();
        return Environment.create("key", cr, "uid", new Dict(), false);
    }

    public Self env(String model){
        Cursor cr = new Cursor();
        return Environment.create("key", cr, "uid", new Dict(), false).get(model);
    }

    protected JsonRpcRequest getRequest(String data) throws JsonRpcParseException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonRpcRequest result = mapper.readValue(data, JsonRpcRequest.class);
            httpServletRequest.setAttribute("jsonrpc_id", result.getId());
            return result;
        } catch (IOException e) {
            throw new JsonRpcParseException(e);
        }
    }
}
