package jdoo.addons.base.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jdoo.exceptions.UserErrorException;
import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.RecordSet;
import jdoo.models.fields;
import jdoo.util.Dict;
import jdoo.util.Kvalues;
import jdoo.util.Tuple;
import jdoo.util.Utils;
import jdoo.apis.api;

/**
 * User class. A res.users record models an OpenERP user and is different from
 * an employee.
 * 
 * res.users class now inherits from res.partner. The partner model is used to
 * store the data related to the partner: lang, name, address, avatar, ... The
 * user model is now dedicated to technical data.
 */
public class ResUsers extends Model {
    // User can write on a few of his own fields (but not his groups for example)
    static List<String> SELF_WRITEABLE_FIELDS = Arrays.asList("signature", "action_id", "company_id", "email", "name",
            "image_1920", "lang", "tz");
    // User can read a few of his own fields
    static List<String> SELF_READABLE_FIELDS = Arrays.asList("signature", "company_id", "login", "email", "name",
            "image_1920", "image_1024", "image_512", "image_256", "image_128", "lang", "tz", "tz_offset", "groups_id",
            "partner_id", "__last_update", "action_id");

    public ResUsers() {
        _name = "res.users";
        _description = "Users";
        _inherits = new Dict<>(d -> d.put("res.partner", "partner_id"));
        _order = "name, login";
        _sql_constraints = Arrays
                .asList(new Tuple<>("login_key", "UNIQUE (login)", "You can not have two users with the same login !"));
    }

    static Field partner_id = fields.Many2one("res.partner").required(true).ondelete("restrict").auto_join(true)
            .string("Related Partner").help("Partner-related data of the user");
    static Field login = fields.Char().required(true).help("Used to log into the system");
    static Field password = fields.Char().compute("_compute_password").inverse("_set_password")// .invisible(true)
            .copy(false).help("Keep empty if you don\"t want the user to be able to connect on the system.");
    static Field new_password = fields.Char().string("Set Password").compute("_compute_password")
            .inverse("_set_new_password")
            .help("Specify a value only when creating a user or if you\"re changing the user\"s password, otherwise leave empty. After a change of password, the user has to login again.");
    static Field signature = fields.Html().string("Email Signature");
    static Field active = fields.Boolean().$default(true);
    static Field active_partner = fields.Boolean().related("partner_id.active").readonly(true)
            .string("Partner is Active");

    // static Field action_id = fields.Many2one("ir.actions.actions").string("Home Action").help(
    //         "If specified, this action will be opened at log on for this user, in addition to the standard menu.");
    static Field groups_id = fields.Many2many("res.groups", "res_groups_users_rel", "uid", "gid").string("Groups")
            .$default(ResUsers::_default_groups);
    static Field log_ids = fields.One2many("res.users.log", "create_uid").string("User log entries");
    static Field login_date = fields.Datetime().related("log_ids.create_date").string("Latest authentication")
            .readonly(false);
    static Field share = fields.Boolean().compute("_compute_share").compute_sudo(true).string("Share User").store(true)
            .help("External user with limited access, created only for the purpose of sharing data.");
    static Field companies_count = fields.Integer().compute("_compute_companies_count").string("Number of Companies")
            .$default(ResUsers::_companies_count);
    static Field tz_offset = fields.Char().compute("_compute_tz_offset").string("Timezone offset").invisible(true);

    static Field company_id = fields.Many2one("res.company").string("Company").required(true)
            .$default(self -> self.env().company().id()).help("The default company for this user.")
            .context(new Kvalues().set("user_preference", true));
    static Field company_ids = fields.Many2many("res.company", "res_company_users_rel", "user_id", "cid")
            .string("Companies").$default(self -> self.env().company().ids());

    static Field name = fields.Char().related("partner_id.name").inherited(true).readonly(false);
    static Field email = fields.Char().related("partner_id.email").inherited(true).readonly(false);

    static Object _default_groups(RecordSet self) {
        // todo
        return "";
    }

    static Object _companies_count(RecordSet self) {
        return self.env("res.company").sudo().search_count(Collections.emptyList());
    }

    protected void _compute_password(RecordSet self) {
        for (RecordSet user : self) {
            user.set(password, "");
            user.set(new_password, "");
        }
    }

    protected void _set_new_password(RecordSet self) {
        for (RecordSet user : self) {
            if (!Utils.bool(user.get(new_password))) {
                continue;
            }
            if (user == self.env().user()) {
                throw new UserErrorException(Utils.l18i(
                        "Please use the change password wizard (in User Preferences or User menu) to change your own password."));
            } else {
                user.set(password, user.get(new_password));
            }
        }
    }

    @api.depends("groups_id")
    protected void _compute_share(RecordSet self) {
        for (RecordSet user : self) {
            user.set(share, !user.call(Boolean.class, "has_group", "base.group_user"));
        }
    }

    protected void _compute_companies_count(RecordSet self) {
        Object companies_count = _companies_count(self);
        for (RecordSet user : self) {
            user.set(ResUsers.companies_count, companies_count);
        }
    }

    @api.depends("tz")
    protected void _compute_tz_offset(RecordSet self) {
        for (RecordSet user : self) {
            user.set(ResUsers.tz_offset, new Date());// todo
        }
    }

    protected void _compute_accesses_count(RecordSet self) {
        // for (RecordSet user : self) {
        // RecordSet groups = user.get(RecordSet.class, groups_id);
        // // user.accesses_count = len(groups.model_access)
        // // user.rules_count = len(groups.rule_groups)
        // // user.groups_count = len(groups)
        // }
    }

    @Override
    public RecordSet create(RecordSet self, Object values) {
        RecordSet users = super.create(self, values);
        for (RecordSet user : users) {
            user.get(RecordSet.class, partner_id).write(new Kvalues()
                    .set("company_id", user.get(RecordSet.class, company_id).id()).set("active", user.get(active)));
        }
        return users;
    }

    @Override
    public boolean write(RecordSet self, Map<String, Object> vals) {
        // TODO Auto-generated method stub
        boolean res = super.write(self, vals);
        // todo
        return res;
    }
}
