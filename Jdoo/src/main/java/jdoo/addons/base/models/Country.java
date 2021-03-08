package jdoo.addons.base.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.RecordSet;
import jdoo.models.d;
import jdoo.models.fields;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.util.Utils;
import jdoo.apis.api;

public class Country extends Model {
    public Country() {
        _name = "res.country";
        _description = "Country";
        _order = "name";
        _sql_constraints = Arrays.asList(
                new Tuple<>("name_uniq", "unique (name)", "The name of the country must be unique !"),
                new Tuple<>("code_uniq", "unique (code)", "The code of the country must be unique !"));
    }

    static Field name = fields.Char().string("Country Name").required(true).translate(true)
            .help("The full name of the country.");
    static Field code = fields.Char().string("Country Code").size(2)
            .help("The ISO country code in two chars. \nYou can use this field for quick search.");
    static Field address_format = fields.Text().string("Layout in Reports")
            .help("Display format to use for addresses belonging to this country.\n\n" + //
                    "You can use python-style string pattern with all the fields of the address " + //
                    "(for example, use \"%(street)s\" to display the field \"street\") plus" + //
                    "\n%(state_name)s: the name of the state" + //
                    "\n%(state_code)s: the code of the state" + //
                    "\n%(country_name)s: the name of the country" + //
                    "\n%(country_code)s: the code of the country")
            .$default("%(street)s\n%(street2)s\n%(city)s %(state_code)s %(zip)s\n%(country_name)s");
    // todo
    // static Field address_view_id =
    // fields.Many2one().comodel_name("ir.ui.view").string("Input View")
    // .domain(d.on("model", "=", "res.partner").on("type", "=", "form"))
    // .help("Use this field if you want to replace the usual way to encode a
    // complete address. " + //
    // "Note that the address_format field is used to modify the way to display
    // addresses " + //
    // "(in reports for example), while this field is used to modify the input form
    // for " + //
    // "addresses.");

    static Field currency_id = fields.Many2one("res.currency").string("Currency");
    static Field image = fields.Binary().attachment(true);
    static Field phone_code = fields.Integer().string("Country Calling Code");
    static Field country_group_ids = fields.Many2many("res.country.group", "res_country_res_country_group_rel",
            "res_country_id", "res_country_group_id").string("Country Groups");
    static Field state_ids = fields.One2many("res.country.state", "country_id").string("States");

    static Field name_position = fields
            .Selection(Arrays.asList(new Pair<>("before", "Before Address"), new Pair<>("after", "After Address")))
            .string("Customer Name Position").$default("before")
            .help("Determines where the customer/company name should be placed, i.e. after or before the address.");
    static Field vat_label = fields.Char().string("Vat Label").translate(true)
            .help("Use this field if you want to change vat label.");

    @Override
    @api.model_create_multi
    public RecordSet create(RecordSet self, Object values) {
        List<Map<String, Object>> vals_list = new ArrayList<Map<String, Object>>();
        if (values instanceof Map<?, ?>) {
            vals_list.add((Map<String, Object>) values);
        } else if (values instanceof Collection<?>) {
            vals_list.addAll((Collection<Map<String, Object>>) values);
        }
        for (Map<String, Object> vals : vals_list) {
            if (Utils.bool(vals.get("code"))) {
                vals.put("code", vals.get("code").toString().toUpperCase());
            }
        }
        return super.create(self, values);
    }

    @Override
    public boolean write(RecordSet self, Map<String, Object> vals) {
        if (Utils.bool(vals.get("code"))) {
            vals.put("code", vals.get("code").toString().toUpperCase());
        }
        return super.write(self, vals);
    }

    public Collection<String> get_address_fields(RecordSet self) {
        self.ensure_one();
        return Pattern.compile("\\((.+?)\\)").matcher(self.get(String.class, address_format)).results()
                .map(r -> r.group()).collect(Collectors.toList());
    }
}
