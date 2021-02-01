package jdoo.addons.web.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jdoo.https.Controller;
import jdoo.https.json.JsonRpcException;
import jdoo.https.http;
import jdoo.util.Default;

@org.springframework.stereotype.Controller
public class DataSetController extends Controller {

    @RequestMapping(value = { "/call_kw", "/call_kw/**" }, method = RequestMethod.POST)
    @http.Route(auth = "user", type = "json")
    public Object call_kw(String model, String method, List<?> args, Map<String, ?> kwargs) throws JsonRpcException {
        return env(model).call(method, args.toArray());
    }

    @RequestMapping(value = "/web/dataset/search_read", method = RequestMethod.POST)
    @http.Route(auth = "user", type = "json")
    public Object search_read(String model, List<String> fields, @Default int offset, int limit,
            List<Object> domain, String sort) {
        return env(model).call("web_search_read", domain, fields, offset, limit, sort);
    }
}
