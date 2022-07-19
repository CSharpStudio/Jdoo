package org.jdoo.base.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jdoo.*;
import org.jdoo.core.CustomModel;
import org.jdoo.core.MetaModel;
import org.jdoo.core.ModelBuilder;
import org.jdoo.core.Registry;
import org.jdoo.data.Cursor;
import org.jdoo.util.KvMap;
import org.jdoo.utils.IdWorker;

/**
 * 模型元数据
 *
 * @author
 */
@Model.Meta(name = "ir.model", description = "模型元数据")
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
	static Field state = Field.Selection().label("状态").selection(Selection.value(new HashMap<String, String>() {
		{
			put("base", "Base");
			put("manual", "Manual");
		}
	})).defaultValue(Default.value("manual"));

	public void addManualModels(Records rec) {
		Cursor cr = rec.getEnv().getCursor();
		cr.execute("SELECT * FROM ir_model WHERE state=%s", Arrays.asList("manual"));
		ModelBuilder builder = ModelBuilder.getBuilder();
		for (KvMap data : cr.fetchMapAll()) {
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

	public void reflectModels(Records rec, Collection<String> modelNames) {
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
		Cursor cr = rec.getEnv().getCursor();
		String colNames = cols.stream().map(c -> cr.quote(c)).collect(Collectors.joining(","));
		String sql = String.format("SELECT %s, id FROM ir_model WHERE model IN %%s", colNames);
		cr.execute(sql, Arrays.asList(models));
		List<KvMap> rows = cr.fetchMapAll();
		Map<String, Map<String, Object>> existing = new HashMap<>();
		for (KvMap kv : rows) {
			Map<String, Object> m = new HashMap<>(kv);
			m.remove("id");
			existing.put((String) kv.get("model"), m);
		}
		for (Entry<String, Map<String, Object>> e : expected.entrySet()) {
			Map<String, Object> exist = existing.get(e.getKey());
			List<Object> values = new ArrayList<>();
			Map<String, Object> v = e.getValue();
			for (String col : cols) {
				values.add(v.get(col));
			}
			if (exist == null) {
				String params = "%s," + cols.stream().map(c -> "%s").collect(Collectors.joining(","));
				String insert = String.format("INSERT INTO ir_model(%s,id) values (%s)", colNames, params);
				values.add(IdWorker.nextId());
				cr.execute(insert, values);
			} else if (!e.getValue().equals(exist)) {
				String sets = cols.stream().map(c -> cr.quote(c) + "=%s").collect(Collectors.joining(","));
				String update = String.format("UPDATE ir_model SET %s WHERE model=%%s", sets);
				values.add(e.getKey());
				cr.execute(update, values);
			}
		}
	}

	public Map<String, Object> reflectModelParams(Records rec, MetaModel model) {
		Map<String, Object> result = new HashMap<>(16);
		result.put("model", model.getName());
		result.put("name", model.getDescription());
		result.put("order", model.getOrder());
		// result.put("info", model.getName());
		result.put("state", model.isCustom() ? "manual" : "base");
		result.put("is_transient", model.isTransient());
		return result;
	}

	public Object getId(Records rec, String name) {
		Cursor cr = rec.getEnv().getCursor();
		cr.execute("SELECT id FROM ir_model WHERE model=%s", Arrays.asList(name));
		Object[] data = cr.fetchOne();
		return data.length > 0 ? data[0] : null;
	}
}
