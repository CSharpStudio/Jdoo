package jdoo.addons.web.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jdoo.apis.api;
import jdoo.https.Controller;
import jdoo.https.json.JsonRpcException;
import jdoo.models.Model;
import jdoo.https.http;
import jdoo.util.Default;
import jdoo.util.Kwargs;

@org.springframework.stereotype.Controller
public class DataSetController extends Controller {
    Object _call_kw(String model, String method, List<Object> args, Map<String, Object> kwargs) {
        Model.check_method_name(method);
        return api.call_kw(env(model), method, args, new Kwargs(kwargs));
    }

    @RequestMapping(value = { "/web/dataset/call_kw", "/web/dataset/call_kw/**" }, method = RequestMethod.POST)
    @http.Route(auth = "user", type = "json")
    public Object call_kw(String model, String method, List<Object> args, Map<String, Object> kwargs)
            throws JsonRpcException {
        return _call_kw(model, method, args, kwargs);
    }

    @RequestMapping(value = "/web/dataset/call", method = RequestMethod.POST)
    @http.Route(auth = "user", type = "json")
    public Object call(String model, String method, List<Object> args, Map<String, Object> kwargs)
            throws JsonRpcException {
        return _call_kw(model, method, args, Collections.emptyMap());
    }

    @RequestMapping(value = "/web/dataset/search_read", method = RequestMethod.POST)
    @http.Route(auth = "user", type = "json")
    public Object search_read(String model, List<String> fields, @Default int offset, int limit, List<Object> domain,
            String sort) {
        return env(model).call("web_search_read", domain, fields, offset, limit, sort);
    }
}
