package jdoo.https;

import java.io.Closeable;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import jdoo.data.Cursor;
import jdoo.data.Database;
import jdoo.apis.Environment;
import jdoo.util.Kvalues;
import jdoo.https.json.JsonRpcParseException;
import jdoo.https.json.JsonRpcRequest;
import jdoo.models.RecordSet;

public class Controller implements Closeable {
    @Autowired
    protected HttpServletRequest httpServletRequest;
    static ThreadLocal<Cursor> local = new ThreadLocal<Cursor>();

    public Cursor cr() {
        Cursor cr = local.get();
        if (cr == null) {
            cr = new Cursor(Database.get("key"));
            local.set(cr);
        }
        return cr;
    }

    public Controller() {
    }

    public Environment env() {
        return Environment.create("key", cr(), "uid", new Kvalues(), false);
    }

    public RecordSet env(String model) {
        return Environment.create("key", cr(), "uid", new Kvalues(), false).get(model);
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

    @Override
    public void close() {
        cr().close();
        local.remove();
    }
}
