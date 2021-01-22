package jdoo.web.models;

import java.util.List;

import jdoo.models.Model;
import jdoo.models.Self;

public class Base extends Model {
    public Base() {
        _inherit = "base";
    }

    public Object web_search_read(Self self, List<Object> domain, List<String> fields, int offset, int limit,
            String order) {
        return null;
    }
}
