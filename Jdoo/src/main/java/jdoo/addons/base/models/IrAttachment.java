package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.fields;
import jdoo.tools.Selector;

public class IrAttachment extends Model {
    public IrAttachment() {
        _name = "ir.attachment";
        _description = "Attachment";
        _order = "id desc";
    }

    static Field name = fields.Char("Name").required(true);
    static Field description = fields.Text("Description");
    static Field res_name = fields.Char("Resource Name").compute("_compute_res_name");
    static Field res_model = fields.Char("Resource Model").readonly(true)
            .help("The database object this attachment will be attached to.");
    static Field res_field = fields.Char("Resource Field").readonly(true);
    // static Field res_id = fields.Many2oneReference("Resource
    // ID").model_field("res_model").readonly(true)
    // .help("The record id this is attached to.");
    static Field company_id = fields.Many2one("res.company").string("Company").change_default(true)
            .$default(self -> self.env().company());
    static Field type = fields.Selection((Selector s) -> s.add("url", "URL").add("binary", "File")).string("Type")
            .required(true).$default("binary").change_default(true)
            .help("You can either upload a file from your computer or copy/paste an internet link to your file.");
    static Field url = fields.Char("Url").index(true).size(1024);
    static Field $public = fields.Boolean("Is public document");

    // for external access
    static Field access_token = fields.Char("Access Token").groups("base.group_user");

    // the field "datas" is computed and may use the other fields below
    static Field datas = fields.Binary().string("File Content").compute("_compute_datas").inverse("_inverse_datas");
    static Field db_datas = fields.Binary("Database Data").attachment(false);
    static Field store_fname = fields.Char("Stored Filename");
    static Field file_size = fields.Integer("File Size").readonly(true);
    static Field checksum = fields.Char("Checksum/SHA1").size(40).index(true).readonly(true);
    static Field mimetype = fields.Char("Mime Type").readonly(true);
    static Field index_content = fields.Text("Indexed Content").readonly(true).prefetch(false);
}
