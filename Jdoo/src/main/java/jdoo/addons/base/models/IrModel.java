package jdoo.addons.base.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jdoo.models.Field;
import jdoo.models.Model;
import jdoo.models.RecordSet;
import jdoo.models.fields;
import jdoo.util.Dict;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.apis.api;
import jdoo.data.Cursor;

public class IrModel extends Model {
    public IrModel() {
        _name = "ir.model";
        _description = "Models";
        _order = "model";
    }

    static Field name = fields.Char("Model Description").translate(true).required(true);
    static Field model = fields.Char().$default("x_").required(true).index(true);
    static Field info = fields.Text().string("Information");
    static Field field_id = fields.One2many("ir.model.fields", "model_id").string("Fields").required(true).copy(true)
            .$default("_default_field_id");
    static Field inherited_model_ids = fields.Many2many("ir.model").compute("_inherited_models")
            .string("Inherited models").help("The list of models that extends the current model.");
    static Field state = fields
            .Selection(Arrays.asList(new Pair<>("manual", "Custom Object"), new Pair<>("base", "Base Object")))
            .string("Type").$default("manual").readonly(true);
    static Field access_ids = fields.One2many("ir.model.access", "model_id").string("Access");
    static Field rule_ids = fields.One2many("ir.rule", "model_id").string("Record Rules");
    static Field $transient = fields.Boolean().string("Transient Model");
    static Field modules = fields.Char().compute("_in_modules").string("In Apps")
            .help("List of modules in which the object is defined or inherited");
    static Field view_ids = fields.One2many("ir.ui.view").compute("_view_ids").string("Views");
    static Field count = fields.Integer().compute("_compute_count").string("Count (Incl. Archived)")
            .help("Total number of records in this model");

    public List<Tuple<Object>> _default_field_id(RecordSet self) {
        if ((boolean) self.env().context().get("install_mode")) {
            return Collections.emptyList();// no default field when importing
        }
        return Arrays.asList(new Tuple<>(0, 0,
                new Dict().set("name", "x_name").set("field_description", "Name").set("ttype", "char")));
    }

    @api.depends()
    public void _inherited_models(RecordSet self) {
        for (RecordSet model : self) {
            Set<String> parent_names = self.env(model.get(String.class, IrModel.model)).type().inherits().keySet();
            if (parent_names == null) {
                model.set(inherited_model_ids,
                        self.call("search", Arrays.asList(new Tuple<>("model", "in", parent_names))));
            }
        }
    }

    @api.depends()
    public void _in_modules(RecordSet self) {
        RecordSet installed_modules = self.env("ir.module.module").call(RecordSet.class, "search",
                Arrays.asList(new Tuple<>("state", "=", "installed")));
        List<String> installed_names = new ArrayList<>();
        for (RecordSet r : installed_modules) {
            installed_names.add(r.get(String.class, "name"));
        }
        // installed_names = set(installed_modules.mapped('name'))
        // xml_ids = models.Model._get_external_ids(self)
        // for model in self:
        // module_names = set(xml_id.split('.')[0] for xml_id in xml_ids[model.id])
        // model.modules = ", ".join(sorted(installed_names & module_names))
    }

    @api.depends()
    public void _view_ids(RecordSet self) {
        for (RecordSet model : self) {
            model.set(view_ids, self.env("ir.ui.view").call("search",
                    Arrays.asList(new Tuple<>("model", "=", model.get(IrModel.model)))));
        }
    }

    @api.depends()
    public void _compute_count(RecordSet self) {
        Cursor cr = self.env().cr();
        for (RecordSet model : self) {
            RecordSet records = self.env(model.get(String.class, IrModel.model));
            if (!records.type().$abstract()) {
                cr.execute(String.format("SELECT COUNT(*) FROM \"%s\"", records.table()));
                model.set(count, cr.fetchone().get(0));
            }
        }
    }
}