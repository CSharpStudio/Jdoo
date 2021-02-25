package jdoo.addons.base.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import jdoo.apis.api;
import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.RecordSet;
import jdoo.models.d;
import jdoo.models.fields;
import jdoo.util.Kvalues;
import jdoo.util.Pair;
import jdoo.util.Utils;

public class Partner extends Model {

    public Partner() {
        _description = "Contact";
        // _inherit = Arrays.asList("format.address.mixin", "image.mixin");
        _name = "res.partner";
        _order = "display_name";
    }

    static Field name = fields.Char().index(true);
    static Field display_name = fields.Char().compute("_compute_display_name").store(true).index(true);
    static Field date = fields.Date().index(true);
    // static Field title = fields.Many2one("res.partner.title");
    static Field parent_id = fields.Many2one("res.partner").string("Related Company").index(true);
    static Field parent_name = fields.Char().related("parent_id.name").readonly(true).string("Parent name");
    static Field child_ids = fields.One2many("res.partner", "parent_id").string("Contact")
            .domain(d.on("active", "=", true));// # force "active_test" domain to bypass _search() override

    static Field ref = fields.Char().string("Reference").index(true);
    static Field lang = fields.Selection(self -> _lang_get(self)).string("Language").$default(self -> self.env().lang())
            .help("All the emails and documents sent to this contact will be translated in this language.");
    static Field active_lang_count = fields.Integer().compute("_compute_active_lang_count");
    static Field tz = fields.Selection(self -> _tz_get(self)).string("Timezone")
            .$default(self -> self.context().get("tz"))
            .help("When printing documents and exporting/importing data, time values are computed according to this timezone.\n"
                    + "If the timezone is not set, UTC (Coordinated Universal Time) is used.\n"
                    + "Anywhere else, time values are computed according to the time offset of your web client.");

    static Field tz_offset = fields.Char().compute("_compute_tz_offset").string("Timezone offset");
    static Field user_id = fields.Many2one("res.users", "Salesperson")
            .help("The internal user in charge of this contact.");
    static Field vat = fields.Char("Tax ID").help(
            "The Tax Identification Number. Complete it if the contact is subjected to government taxes. Used in some legal statements.");
    static Field same_vat_partner_id = fields.Many2one("res.partner", "Partner with same Tax ID")
            .compute("_compute_same_vat_partner_id").store(false);
    //static Field bank_ids = fields.One2many("res.partner.bank", "partner_id", "Banks");
    static Field website = fields.Char("Website Link");
    static Field comment = fields.Text("Notes");

    static Field category_id = fields.Many2many("res.partner.category").column1("partner_id").column2("category_id")
            .string("Tags").$default(self -> _default_category(self));
    static Field credit_limit = fields.Float("Credit Limit");
    static Field active = fields.Boolean().$default(true);
    static Field employee = fields.Boolean().help("Check this box if this contact is an Employee.");
    static Field function = fields.Char("Job Position");
    static Field type = fields
            .Selection(Arrays.asList(new Pair<>("contact", "Contact"), new Pair<>("invoice", "Invoice Address"),
                    new Pair<>("delivery", "Delivery Address"), new Pair<>("other", "Other Address"),
                    new Pair<>("private", "Private Address")))
            .string("Address Type").$default("contact")
            .help("Invoice & Delivery addresses are used in sales orders. Private addresses are only visible by authorized users.");
    static Field street = fields.Char();
    static Field street2 = fields.Char();
    static Field zip = fields.Char().change_default(true);
    static Field city = fields.Char();

    // static Field state_id = fields.Many2one("res.country.state",
    // "State").ondelete("restrict")
    // .domain(d.on("[('country_id', '=?', country_id)]"));
    // static Field country_id = fields.Many2one("res.country",
    // "Country").ondelete("restrict");
    static Field partner_latitude = fields.Float("Geo Latitude").digits(16, 5);
    static Field partner_longitude = fields.Float("Geo Longitude").digits(16, 5);
    static Field email = fields.Char();
    static Field email_formatted = fields.Char("Formatted Email").compute("_compute_email_formatted")
            .help("Format email address \"Name <email@domain>\"");
    static Field phone = fields.Char();
    static Field mobile = fields.Char();
    static Field is_company = fields.Boolean("Is a Company").$default(false)
            .help("Check if the contact is a company, otherwise it is a person");
    // static Field industry_id = fields.Many2one("res.partner.industry",
    // "Industry");

    // # company_type is only an interface field, do not use it in business logic
    static Field company_type = fields.Selection().string("Company Type")
            .selection(Arrays.asList(new Pair<>("person", "Individual"), new Pair<>("company", "Company")))
            .compute("_compute_company_type").inverse("_write_company_type");
    // static Field company_id = fields.Many2one("res.company",
    // "Company").index(true);
    static Field color = fields.Integer("Color Index").$default(0);
    static Field user_ids = fields.One2many("res.users", "partner_id", "Users").auto_join(true);
    static Field partner_share = fields.Boolean("Share Partner").compute("_compute_partner_share").store(true).help(
            "Either customer (not a user), either shared user. Indicated the current partner is a customer without "
                    + "access or with a limited access created for sharing data.");
    static Field contact_address = fields.Char().compute("_compute_contact_address").string("Complete Address");

    // # technical field used for managing commercial fields
    static Field commercial_partner_id = fields.Many2one("res.partner").compute("_compute_commercial_partner")
            .string("Commercial Entity").store(true).index(true);
    static Field commercial_company_name = fields.Char("Company Name Entity")
            .compute("_compute_commercial_company_name").store(true);
    static Field company_name = fields.Char("Company Name");

    // # hack to allow using plain browse record in qweb views, and used in
    // ir.qweb.field.contact
    static Field self = fields.Many2one("res.partner").compute("_compute_get_ids");

    @api.depends({ "is_company", "name", "parent_id.name", "type", "company_name" })
    @api.depends_context({ "show_address", "show_address_only", "show_email", "html_format", "show_vat" })
    public void _compute_display_name(RecordSet self) {
        Kvalues diff = new Kvalues(k->k.set("show_address", null).set("show_address_only", null).set("show_email", null)
                .set("html_format", null).set("show_vat", null));
        Map<Object, Object> names = Utils.toMap(self.with_context(diff).name_get());
        for (RecordSet partner : self)
            partner.set(display_name, names.get(partner.id()));
    }

    public void _compute_active_lang_count(RecordSet self) {
        int lang_count = self.env("res.lang").call(List.class, "get_installed").size();
        for (RecordSet partner : self) {
            partner.set(active_lang_count, lang_count);
        }
    }

    @api.depends("tz")
    public void _compute_tz_offset(RecordSet self) {

    }

    @api.depends({ "user_ids.share", "user_ids.active" })
    public void _compute_partner_share(RecordSet self) {

    }

    @api.depends("vat")
    public void _compute_same_vat_partner_id(RecordSet self) {

    }

    @api.depends("self->_display_address_depends(self)")
    public void _compute_contact_address(RecordSet self) {

    }

    @api.depends({ "is_company", "parent_id.commercial_partner_id" })
    public void _compute_commercial_partner(RecordSet self) {

    }

    @api.depends({ "company_name", "parent_id.is_company", "commercial_partner_id.name" })
    public void _compute_commercial_company_name(RecordSet self) {

    }

    @api.depends({ "name", "email" })
    public void _compute_email_formatted(RecordSet self) {

    }

    @api.depends("is_company")
    public void _compute_company_type(RecordSet self) {

    }

    public void _compute_get_ids(RecordSet self) {

    }

    static List<String> _tz_get(RecordSet self) {
        return new ArrayList<String>();
    }

    static List<String> _lang_get(RecordSet self) {
        return self.env("res.lang").call(new TypeReference<List<String>>() {
        }, "get_installed");
    }

    static RecordSet _default_category(RecordSet self) {
        return self.env("res.partner.category").browse((String) self.context().get("category_id"));
    }
}
