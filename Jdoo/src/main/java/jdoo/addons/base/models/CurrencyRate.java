package jdoo.addons.base.models;

import java.util.Arrays;
import java.util.List;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.RecordSet;
import jdoo.models.fields;
import jdoo.models._fields.DateField;
import jdoo.util.Pair;
import jdoo.util.Tuple;

public class CurrencyRate extends Model {
    public CurrencyRate() {
        _name = "res.currency.rate";
        _description = "Currency Rate";
        _order = "name desc";

        _sql_constraints = Arrays.asList(
                new Tuple<>("unique_name_per_day", "unique (name,currency_id,company_id)",
                        "Only one currency rate per day allowed!"),
                new Tuple<>("currency_rate_check", "CHECK (rate>0)", "The currency rate must be strictly positive."));
    }

    static Field name = fields.Date().string("Date").required(true).index(true).$default(self -> DateField.today());
    static Field rate = fields.Float().digits(0).$default(1.0)
            .help("The rate of the currency to the currency of rate 1");

    static Field currency_id = fields.Many2one("res.currency").string("Currency").readonly(true);
    static Field company_id = fields.Many2one("res.company").string("Company").$default(self -> self.env().company());

    @Override
    protected List<Pair<Object, String>> _name_search(RecordSet self, String name, List<Object> args, String operator,
            Integer limit, String name_get_uid) {
        // TODO Auto-generated method stub
        return super._name_search(self, name, args, operator, limit, name_get_uid);
    }
}
