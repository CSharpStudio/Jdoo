package org.jdoo.base.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jdoo.*;
import org.jdoo.core.CustomModel;
import org.jdoo.core.MetaModel;
import org.jdoo.core.ModelBuilder;
import org.jdoo.core.Registry;
import org.jdoo.data.Cursor;
import org.jdoo.util.KvMap;

/**
 * 模型元数据
 *
 * @author
 */
@Model.Meta(name = "ir.model", label = "模型元数据")
@Model.UniqueConstraint(name = "unique_model", fields = "model")
public class IrModel extends Model {
	static Field name = Field.Char().label("名称").index(true).required(true);
	static Field model = Field.Char().label("模型").index(true).required(true);
	static Field description = Field.Char().label("描述").help("模型说明");
	static Field inherit = Field.Char().label("继承").help("模型的继承，多个使用逗号','分隔");
	static Field table = Field.Char().label("表名").help("映射数据库的表名");
	static Field order = Field.Char().label("排序").help("默认排序SQL, 如 name ASC, date DESC");
	static Field present = Field.Char().label("呈现").help("记录呈现的字段名，默认尝试使用name字段呈现");
	static Field is_transient = Field.Boolean().label("是否瞬态");
	static Field model_class = Field.Char().label("类").help("对应代码的类");
	static Field field_ids = Field.One2many("ir.model.field", "model_id").label("字段");
	static Field module_id = Field.Many2one("ir.module");
	static Field origin = Field.Selection().label("来源").selection(new HashMap<String, String>() {
		{
			put("base", "内置");
			put("manual", "自定义");
		}
	}).defaultValue("manual").readonly();

	public void addManualModels(Records rec) {
		Cursor cr = rec.getEnv().getCursor();
		cr.execute("SELECT * FROM ir_model WHERE origin=%s", Arrays.asList("manual"));
		ModelBuilder builder = ModelBuilder.getBuilder();
		for (Map<String, Object> data : cr.fetchMapAll()) {
			String name = (String) data.get("model");
			String label = (String) data.get("name");
			String desc = (String) data.get("description");
			String inherit = (String) data.get("inherit");
			String order = (String) data.get("order");
			String present = (String) data.get("present");
			Boolean isTransient = (Boolean) data.get("is_transient");
			String table = (String) data.get("table");
			CustomModel cm = new CustomModel(name, inherit, label, desc, order, present, table, isTransient);

			builder.buildModel(rec.getMeta().getRegistry(), cm, "");
		}
	}

	public void reflectModels(Records rec, Collection<String> modelNames, String module) {
		Registry reg = rec.getEnv().getRegistry();
		Map<String, Map<String, Object>> expected = new HashMap<>(16);
		Set<String> cols = null;
		List<String> models = new ArrayList<>();
		for (String modelName : modelNames) {
			Map<String, Object> meta = reflectModelParams(rec, reg.get(modelName));
			String model = (String) meta.get("model");
			models.add(model);
			expected.put(model, meta);
			if (cols == null) {
				cols = meta.keySet();
			}
		}
		if (cols == null) {
			return;
		}
		List<Map<String, Object>> rows = search(rec, cols, Criteria.in("model", models), null, null, null);
		Map<String, Map<String, Object>> existing = new HashMap<>();
		Map<String, String> modelIds = new HashMap<>();
		for (Map<String, Object> kv : rows) {
			Map<String, Object> m = new HashMap<>(kv);
			String model = (String) kv.get("model");
			modelIds.put(model, (String) m.remove("id"));
			existing.put(model, m);
		}
		for (Entry<String, Map<String, Object>> e : expected.entrySet()) {
			Map<String, Object> exist = existing.get(e.getKey());
			Map<String, Object> values = e.getValue();
			try {
				String name = "model_" + e.getKey().replaceAll("\\.", "_");
				if (exist == null) {
					rec = create(rec, values);
					rec.getEnv().get("ir.model.data").create(new KvMap()
							.set("name", name)
							.set("module", module)
							.set("model", "ir.model")
							.set("res_id", rec.getId()));

				} else {
					rec = rec.browse(modelIds.get(e.getKey()));
					if (!e.getValue().equals(exist)) {
						rec.update(values);
					}
					Records data = rec.getEnv().get("ir.model.data")
							.find(Criteria.equal("module", module).and(Criteria.equal("name", name)));
					if (!data.any()) {
						data.create(new KvMap()
								.set("name", name)
								.set("module", module)
								.set("model", "ir.model")
								.set("res_id", rec.getId()));
					}
				}
			} catch (Exception err) {
				throw err;
			}
		}
	}

	public Map<String, Object> reflectModelParams(Records rec, MetaModel model) {
		Map<String, Object> result = new HashMap<>(16);
		result.put("model", model.getName());
		result.put("name", model.getLabel());
		result.put("description", model.getDescription());
		result.put("order", model.getOrder());
		// result.put("info", model.getName());
		result.put("origin", model.isCustom() ? "manual" : "base");
		result.put("is_transient", model.isTransient());
		return result;
	}

	public Object getId(Records rec, String name) {
		Cursor cr = rec.getEnv().getCursor();
		cr.execute("SELECT id FROM ir_model WHERE model=%s", Arrays.asList(name));
		Object[] data = cr.fetchOne();
		return data.length > 0 ? data[0] : null;
	}

	@ServiceMethod(auth = "update", doc = "生成默认视图")
	public Object initDefaultViews(Records rec) {
		Records view = rec.getEnv().get("ir.ui.view");
		for (Records r : rec) {
			String name = (String) r.get("name");
			String model = (String) r.get("model");
			Criteria criteria = Criteria.equal("model", model).and(Criteria.equal("mode", "primary"));
			Records views = view.find(criteria);
			List<Map<String, Object>> valuesList = new ArrayList<>();

			if (!views.filter(v -> "grid".equals(v.get("type"))).any()) {
				Map<String, Object> values = new HashMap<>(16);
				values.put("name", name + "-表格");
				values.put("model", model);
				values.put("type", "grid");
				values.put("mode", "primary");
				values.put("arch", getGridArch(r));
				valuesList.add(values);
			}
			if (!views.filter(v -> "form".equals(v.get("type"))).any()) {
				Map<String, Object> values = new HashMap<>(16);
				values.put("name", name + "-表单");
				values.put("model", model);
				values.put("type", "form");
				values.put("mode", "primary");
				values.put("arch", getFormArch(r));
				valuesList.add(values);
			}
			if (!views.filter(v -> "search".equals(v.get("type"))).any()) {
				Map<String, Object> values = new HashMap<>(16);
				values.put("name", name + "-查询");
				values.put("model", model);
				values.put("type", "search");
				values.put("mode", "primary");
				values.put("arch", getSearchArch(r));
				valuesList.add(values);
			}
			views.createBatch(valuesList);
		}
		return Action.reload("保存成功");
	}

	String getSearchArch(Records model) {
		String arch = "<search>";
		Records fields = (Records) model.get("field_ids");
		for (Records field : fields) {
			String fieldType = (String) field.get("field_type");
			if (!fieldType.endsWith("many")) {
				arch += "<field name='" + field.get("name") + "'>";
			}
		}
		arch += "</search>";
		return arch;
	}

	String getGridArch(Records model) {
		String arch = "<grid><toolbar buttons='default'></toolbar>";
		Records fields = (Records) model.get("field_ids");
		for (Records field : fields) {
			String fieldType = (String) field.get("field_type");
			if (!fieldType.endsWith("many")) {
				arch += "<field name='" + field.get("name") + "'>";
			}
		}
		arch += "</grid>";
		return arch;
	}

	String getFormArch(Records model) {
		String arch = "<form><toolbar buttons='default'></toolbar>";
		Records fields = (Records) model.get("field_ids");
		for (Records field : fields) {
			String fieldType = (String) field.get("field_type");
			if (!fieldType.endsWith("many")) {
				arch += "<field name='" + field.get("name") + "'/>";
			}
		}
		arch += "</form>";
		return arch;
	}
}
