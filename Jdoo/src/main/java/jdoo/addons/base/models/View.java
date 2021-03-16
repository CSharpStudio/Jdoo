package jdoo.addons.base.models;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.d;
import jdoo.models.fields;
import jdoo.tools.Selector;

public class View extends Model {
    public View() {
        _name = "ir.ui.view";
        _description = "View";
        _order = "priority,name,id";
    }

    static Field name = fields.Char().string("View Name").required(true);
    static Field model = fields.Char().index(true);
    static Field key = fields.Char();
    static Field priority = fields.Integer().string("Sequence").$default(16).required(true);
    static Field type = fields.Selection((Selector s) -> s.add("tree", "Tree")//
            .add("form", "Form")//
            .add("graph", "Graph")//
            .add("pivot", "Pivot")//
            .add("calendar", "Calendar")//
            .add("diagram", "Diagram")//
            .add("gantt", "Gantt")//
            .add("kanban", "Kanban")//
            .add("search", "Search")//
            .add("qweb", "QWeb")//
    ).string("View Type");
    static Field arch = fields.Text().compute("_compute_arch").inverse("_inverse_arch").string("View Architecture")
            .help("This field should be used when accessing view arch. It will use translation." + //
                    "Note that it will read `arch_db` or `arch_fs` if in dev-xml mode.");
    static Field arch_base = fields.Text().compute("_compute_arch_base").inverse("_inverse_arch_base")
            .string("Base View Architecture").help("This field is the same as `arch` field without translations");
    static Field arch_db = fields.Text().string("Arch Blob")
            /* todo . translate(xml_translate) */.help("This field stores the view arch.");
    static Field arch_fs = fields.Char().string("Arch Filename").help("File from where the view originates." + //
            "Useful to (hard) reset broken views or to read arch from file in dev-xml mode.");
    static Field arch_updated = fields.Boolean().string("Modified Architecture");
    static Field arch_prev = fields.Text().string("Previous View Architecture")
            .help("This field will save the current `arch_db` before writing on it." + // "
                    "Useful to (soft) reset a broken view.");
    static Field inherit_id = fields.Many2one("ir.ui.view").string("Inherited View").ondelete("restrict").index(true);
    static Field inherit_children_ids = fields.One2many("ir.ui.view", "inherit_id")
            .string("Views which inherit from this one");
    static Field field_parent = fields.Char().string("Child Field");
    static Field model_data_id = fields.Many2one("ir.model.data").string("Model Data").compute("_compute_model_data_id")
            .search("_search_model_data_id");
    static Field xml_id = fields.Char().string("External ID").compute("_compute_xml_id")
            .help("ID of the view defined in xml file");
    static Field groups_id = fields.Many2many("res.groups", "ir_ui_view_group_rel", "view_id", "group_id")
            .string("Groups")
            .help("If this field is empty, the view applies to all users. Otherwise, the view applies to the users of those groups only.");
    static Field model_ids = fields.One2many("ir.model.data", "res_id").string("Models")
            .domain(d.on("model", "=", "ir.ui.view")).auto_join(true);

    static Field mode = fields
            .Selection((Selector s) -> s.add("primary", "Base view").add("extension", "Extension View"))
            .string("View inheritance mode").$default("primary").required(true)
            .help("Only applies if this view inherits from an other one (inherit_id is not False/Null)." + //
                    "* if extension (default), if this view is requested the closest primary view" + //
                    "is looked up (via inherit_id), then all views inheriting from it with this" + //
                    "view's model are applied" + //
                    "* if primary, the closest primary view is fully resolved (even if it uses a" + //
                    "different model than this one), then this view's inheritance specs" + //
                    "(<xpath/>) are applied, and the result is used as if it were this view's" + //
                    "actual arch.");

    static Field active = fields.Boolean().$default(true).help("If this view is inherited," + //
            "* if True, the view always extends its parent" + //
            "* if False, the view currently does not extend its parent but can be enabled");
}
