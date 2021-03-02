package jdoo.addons.web.models;

import java.util.List;

import jdoo.models.Model;
import jdoo.models.RecordSet;

public class Base extends Model {
    public Base() {
        _inherit = "base";
    }

    public Object web_search_read(RecordSet self, List<Object> domain, List<String> fields, int offset, int limit,
            String order) {
        // self.search_read(domain, fields, offset, limit, order);
        return null;
    }
}
