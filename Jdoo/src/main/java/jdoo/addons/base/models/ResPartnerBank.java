package jdoo.addons.base.models;

import java.util.Arrays;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.d;
import jdoo.models.fields;
import jdoo.util.Tuple;

public class ResPartnerBank extends Model {
    public ResPartnerBank() {
        _name = "res.partner.bank";
        _rec_name = "acc_number";
        _description = "Bank Accounts";
        _order = "sequence, id";

        _sql_constraints = Arrays.asList(new Tuple<>("unique_number", "unique(sanitized_acc_number, company_id)",
                "Account Number must be unique"));
    }

    static Field acc_type = fields.Selection()
            .selection(x -> x.env("res.partner.bank").call("get_supported_account_types")).compute("_compute_acc_type")
            .string("Type").help("Bank account type: Normal or IBAN. Inferred from the bank account number.");
    static Field acc_number = fields.Char("Account Number").required(true);
    static Field sanitized_acc_number = fields.Char().compute("_compute_sanitized_acc_number")
            .string("Sanitized Account Number").readonly(true).store(true);
    static Field acc_holder_name = fields.Char().string("Account Holder Name")
            .help("Account holder name, in case it is different than the name of the Account Holder");
    static Field partner_id = fields.Many2one("res.partner", "Account Holder").ondelete("cascade").index(true)
            .domain(d.or().on("is_company", "=", true).on("parent_id", "=", false)).required(true);
    static Field bank_id = fields.Many2one("res.bank").string("Bank");
    static Field bank_name = fields.Char().related("bank_id.name").readonly(false);
    static Field bank_bic = fields.Char().related("bank_id.bic").readonly(false);
    static Field sequence = fields.Integer().$default(10);
    static Field currency_id = fields.Many2one("res.currency").string("Currency");
    static Field company_id = fields.Many2one("res.company", "Company").$default(self -> self.env().company())
            .ondelete("cascade");
    static Field qr_code_valid = fields.Boolean().string("Has all required arguments")
            .compute("_validate_qr_code_arguments");
}
