package jdoo.addons.base.models;

import jdoo.models.Kwargs;
import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.RecordSet;
import jdoo.models.d;
import jdoo.models.fields;

public class ResBank extends Model {

    public ResBank() {
        _name = "res.bank";
        _description = "Bank";
        _order = "name";
    }

    public static Field name = fields.Char().required(true);
    public static Field street = fields.Char();
    public static Field street2 = fields.Char();
    public static Field zip = fields.Char();
    public static Field city = fields.Char();
    // public static Field state = fields.Many2one("res.country.state", "Fed. State")
    //         .domain(d.on("[('country_id', '=?', country)]"));
    //public static Field country = fields.Many2one("res.country");
    public static Field email = fields.Char();
    public static Field phone = fields.Char();
    public static Field active = fields.Boolean().$default(true);
    public static Field bic = fields.Char("Bank Identifier Code").index(true).help("Sometimes called BIC or Swift.");

    // public List<Object[]> name_get(Self self) {
    // ArrayList<Object[]> result = new ArrayList<Object[]>();
    // for (Self bank : self) {
    // String n = bank.get(String.class, name);
    // String bic_ = bank.get(String.class, bic);
    // if (StringUtils.isNotBlank(bic_)) {
    // n += " - " + bic_;
    // }
    // result.add(new Object[] { bank.id(), n });
    // }
    // return result;
    // }

    public Object _name_search(RecordSet self, Kwargs args) {

        return null;
    }
}
