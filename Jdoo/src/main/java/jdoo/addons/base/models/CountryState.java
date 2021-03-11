package jdoo.addons.base.models;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.RecordSet;
import jdoo.models.d;
import jdoo.models.fields;
import jdoo.osv.Expression;
import jdoo.tools.LazyNameGet;
import jdoo.util.Kwargs;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.util.Utils;

public class CountryState extends Model {
    public CountryState() {
        _description = "Country state";
        _name = "res.country.state";
        _order = "code";

        _sql_constraints = Arrays.asList(new Tuple<>("name_code_uniq", "unique(country_id, code)",
                "The code of the state must be unique by country !"));
    }

    static Field country_id = fields.Many2one("res.country").string("Country").required(true);
    static Field name = fields.Char().string("State Name").required(true)
            .help("Administrative divisions of a country. E.g. Fed. State, Departement, Canton");
    static Field code = fields.Char().string("State Code").help("The state code.").required(true);

    @Override
    protected List<Pair<Object, String>> _name_search(RecordSet self, String name, List<Object> args, String operator,
            Integer limit, String name_get_uid) {
        if (args == null) {
            args = new ArrayList<>();
        } else {
            args = new ArrayList<>(args);
        }
        if (Utils.bool(self.env().context().get("country_id"))) {
            args = Expression.AND(args, d.on("country_id", "=", self.env().context().get("country_id")));
        }
        List<Object> first_domain;
        List<Object> domain;
        if ("ilike".equals(operator) && !Utils.bool(name)) {
            first_domain = Collections.emptyList();
            domain = Collections.emptyList();
        } else {
            first_domain = d.on("code", "=ilike", name);
            domain = d.on("name", operator, name);
        }
        Collection<Object> first_state_ids = first_domain.isEmpty() ? Collections.emptyList()
                : (Collection<Object>) self.call("_search", Expression.AND(first_domain, args),
                        new Kwargs().set("limit", limit).set("access_rights_uid", name_get_uid));
        List<Object> state_ids = new ArrayList<>(first_state_ids);
        for (Object state_id : (Collection<Object>) self.call("_search", Expression.AND(domain, args),
                new Kwargs().set("limit", limit).set("access_rights_uid", name_get_uid))) {
            if (!first_state_ids.contains(state_id)) {
                state_ids.add(state_id);
            }
        }
        return new LazyNameGet(self.browse(state_ids).with_user(name_get_uid));
    }

    @Override
    public List<Pair<Object, String>> name_get(RecordSet self) {
        List<Pair<Object, String>> result = new ArrayList<>();
        for (RecordSet record : self) {
            result.add(new Pair<>(record.id(),
                    MessageFormat.format("{0} ({1})", record.get(name), record.get("country_id.code"))));
        }
        return result;
    }
}
