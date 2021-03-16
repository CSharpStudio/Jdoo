package jdoo.addons.base.models;

import java.util.Arrays;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;
import jdoo.tools.Selector;
import jdoo.util.Tuple;

public class IrTranslation extends Model {
    public IrTranslation() {
        _name = "ir.translation";
        _description = "Translation";
        _log_access = false;

        _sql_constraints = Arrays
                .asList(new Tuple<>("lang_fkey_res_lang", "FOREIGN KEY(lang) REFERENCES res_lang(code)",
                        "Language code of translation item must be among known languages"));
    }

    static Field name = fields.Char().string("Translated field").required(true);
    static Field res_id = fields.Integer().string("Record ID").index(true);
    static Field lang = fields.Selection().selection("_get_languages").string("Language").validate(false);
    static Field type = fields.Selection(/* todo TRANSLATION_TYPE */).string("Type").index(true);
    static Field src = fields.Text().string("Internal Source"); // # stored in database, kept for backward compatibility
    static Field value = fields.Text().string("Translation Value");
    static Field module = fields.Char().index(true).help("Module this term belongs to");

    static Field state = fields.Selection((Selector s) -> s.add("to_translate", "To Translate")//
            .add("inprogress", "Translation in Progress")//
            .add("translated", "Translated")//
    ).string("Status").$default("to_translate")
            .help("Automatically set to let administators find new terms that might need to be translated");

    // # aka gettext extracted-comments - we use them to flag openerp-web
    // translation
    // # cfr:
    // http://www.gnu.org/savannah-checkouts/gnu/gettext/manual/html_node/PO-Files.html
    static Field comments = fields.Text().string("Translation comments").index(true);
}
