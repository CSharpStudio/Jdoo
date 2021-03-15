package jdoo.addons.base.models;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.RecordSet;
import jdoo.models.fields;
import jdoo.util.Pair;
import jdoo.apis.api;

public class Lang extends Model {
    public Lang() {
        _name = "res.lang";
        _description = "Languages";
        _order = "active desc,name";
    }

    static Field name = fields.Char().required(true);
    static Field code = fields.Char().string("Locale Code").required(true)
            .help("This field is used to set/get locales for user");
    static Field active = fields.Boolean();

    @api.model
    public List<Pair<String, String>> get_installed(RecordSet self) {
        RecordSet langs = self.with_context(ctx -> ctx.set("active_test", true)).search(Collections.emptyList());
        return langs.stream().sorted(Comparator.comparing(l -> (String) l.get("code")))
                .map(lang -> new Pair<>((String) lang.get("code"), (String) lang.get("name")))
                .collect(Collectors.toList());
    }
}
