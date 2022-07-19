package org.jdoo.base.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jdoo.*;
import org.jdoo.core.Constants;
import org.jdoo.core.MetaField;
import org.jdoo.core.MetaModel;
import org.jdoo.data.Cursor;
import org.jdoo.fields.Many2oneField;
import org.jdoo.fields.One2manyField;
import org.jdoo.fields.RelationalField;
import org.jdoo.fields.StringField;
import org.jdoo.fields.RelationalField.DeleteMode;
import org.jdoo.util.KvMap;
import org.jdoo.utils.IdWorker;
import org.apache.commons.lang3.StringUtils;

/**
 * 字段
 *
 * @author
 */
@Model.Meta(name = "ir.model.field", description = "字段", order = "id asc")
public class IrField extends Model {
	static Map<String, String> FIELD_TYPES = Field.getFieldTypes().stream().collect(Collectors.toMap(k -> k, v -> v));

	static Field name = Field.Char().label("名称").help("字段名").index(true).required(true);
	static Field model_id = Field.Many2one("ir.model").label("模型").required().index().help("字段所属的模型");
	static Field field_type = Field.Selection(Selection.value(FIELD_TYPES)).label("类型").help("字段类型").required(true).defaultValue(Default.value("char"));
	static Field relation = Field.Char().label("关联的模型");
	static Field relation_field = Field.Char().label("一对多关联字段");
	static Field label = Field.Char().label("标题");
	static Field help = Field.Char().label("帮助");
	static Field related = Field.Char().label("关联");
	static Field required = Field.Boolean().label("是否必填");
	static Field readonly = Field.Boolean().label("是否只读");
	static Field index = Field.Boolean().label("是否索引");
	static Field translate = Field.Boolean().label("是否翻译");
	static Field size = Field.Integer().label("大小");
	static Field state = Field.Selection(Selection.value(new HashMap<String, String>() {
		{
			put("manual", "自定义字段");
			put("base", "基础字段");
		}
	})).label("状态").defaultValue(Default.value("manual"));
	static Field on_delete = Field.Selection(Selection.value(new HashMap<String, String>() {
		{
			put("Cascade", "级联");
			put("SetNull", "设置为null");
			put("Restrict", "限制");
		}
	})).label("删除操作");
	static Field auth = Field.Boolean().label("是否需要权限访问");
	static Field relation_table = Field.Char().label("多对多关联表");
	static Field column1 = Field.Char().label("被关联表字段");
	static Field column2 = Field.Char().label("关联表字段");
	static Field compute = Field.Char().label("计算表达式");
	static Field default_value = Field.Char().label("默认值");
	static Field depends = Field.Char().label("依赖");
	static Field store = Field.Boolean().label("是否存储").defaultValue(Default.value(true));
	static Field copy = Field.Boolean().label("是否复制");

	public void addManualFields(Records rec, MetaModel model) {
		Cursor cr = rec.getEnv().getCursor();
		cr.execute(
				"SELECT f.* FROM ir_model_field f JOIN ir_model m ON f.model_id=m.id WHERE f.state='manual' AND m.model=%s",
				Arrays.asList(model.getName()));

		List<KvMap> fieldDatas = cr.fetchMapAll();
		for (KvMap fieldData : fieldDatas) {
			MetaField field = Field.create((String) fieldData.get("field_type"));
			KvMap args = getArgs(fieldData);
			field.setArgs(args);
			field.setName((String) fieldData.get("name"));
			model.addField(field.getName(), field);
		}
	}

	void putIf(KvMap map, String key, Object value) {
		if (value != null) {
			map.put(key, value);
		}
	}

	private KvMap getArgs(KvMap fieldData) {
		KvMap attrs = new KvMap(fieldData.size());
		attrs.put("manual", true);
		putIf(attrs, "label", fieldData.get("label"));
		putIf(attrs, "help", fieldData.get("help"));
		putIf(attrs, "index", fieldData.get("index"));
		putIf(attrs, "copy", fieldData.get("copy"));
		putIf(attrs, "required", fieldData.get("required"));
		putIf(attrs, "readonly", fieldData.get("readonly"));
		putIf(attrs, "store", fieldData.get("store"));

		String related = (String) fieldData.get("related");
		if (StringUtils.isNotBlank(related)) {
			putIf(attrs, "related", related.split("\\."));
		}

		String type = (String) fieldData.get("field_type");
		if (Constants.CHAR.equals(type) || Constants.TEXT.equals(type) || Constants.HTML.equals(type)) {
			putIf(attrs, "translate", fieldData.get("translate"));
			if (Constants.CHAR.equals(type)) {
				putIf(attrs, "size", fieldData.get("size"));
			}
		} else if (Constants.SELECTION.equals(type)) {
			// TODO get selection from ir.model.fields.selection putIf(attrs,"selection",
			// fieldData.get("id"));
		} else if (Constants.MANY2ONE.equals(type)) {
			putIf(attrs, "comodelName", fieldData.get("relation"));
			String ondelete = (String) fieldData.get("on_delete");
			putIf(attrs, "ondelete", DeleteMode.valueOf(ondelete));
		} else if (Constants.ONE2MANY.equals(type)) {
			putIf(attrs, "comodelName", fieldData.get("relation"));
			putIf(attrs, "inverseName", fieldData.get("relation_field"));
		} else if (Constants.MANY2MANY.equals(type)) {
			putIf(attrs, "comodelName", fieldData.get("relation"));
			putIf(attrs, "relation", fieldData.get("relation_table"));
			putIf(attrs, "column1", fieldData.get("column1"));
			putIf(attrs, "column2", fieldData.get("column2"));
		}
		String compute = (String) fieldData.get("compute");
		if (StringUtils.isNotBlank(compute)) {
			putIf(attrs, "compute", Callable.script(compute));
		}
		return attrs;
	}

	public void reflectFields(Records rec, Collection<String> modelNames) {
		Map<String, Map<String, Object>> expected = new LinkedHashMap<>(16);
		Set<String> cols = null;
		for (String modelName : modelNames) {
			Object modelId = rec.getEnv().get("ir.model").call("getId", modelName);
			MetaModel model = rec.getEnv().getRegistry().get(modelName);
			List<String> fields = new ArrayList<>();
			for (MetaField field : model.getFields().values()) {
				Map<String, Object> meta = reflectFieldsParams(rec, field, modelId);
				String name = (String) meta.get("name");
				fields.add(name);
				expected.put(modelName + "#" + name, meta);
				if (cols == null) {
					cols = meta.keySet();
				}
			}
		}
		Cursor cr = rec.getEnv().getCursor();
		String colNames = cols.stream().map(c -> "f." + cr.quote(c)).collect(Collectors.joining(","));
		String sql = String.format(
				"SELECT %s, m.model, f.id FROM ir_model_field f join ir_model m on f.model_id=m.id WHERE m.model IN %%s",
				colNames);
		cr.execute(sql, Arrays.asList(modelNames));
		List<KvMap> rows = cr.fetchMapAll();
		Map<String, Map<String, Object>> existing = new HashMap<>();
		for (KvMap kv : rows) {
			Map<String, Object> m = new HashMap<>(kv);
			m.remove("id");
			m.remove("model");
			existing.put(kv.get("model") + "#" + kv.get("name"), m);
		}
		colNames = cols.stream().map(c -> cr.quote(c)).collect(Collectors.joining(","));
		for (Entry<String, Map<String, Object>> e : expected.entrySet()) {
			Map<String, Object> exist = existing.get(e.getKey());
			List<Object> values = new ArrayList<>();
			Map<String, Object> v = e.getValue();
			for (String col : cols) {
				values.add(v.get(col));
			}
			if (exist == null) {
				String params = "%s," + cols.stream().map(c -> "%s").collect(Collectors.joining(","));
				String insert = String.format("INSERT INTO ir_model_field(%s,id) values (%s)", colNames, params);
				values.add(IdWorker.nextId());
				cr.execute(insert, values);
			} else if (!e.getValue().equals(exist)) {
				String sets = cols.stream().map(c -> cr.quote(c) + "=%s").collect(Collectors.joining(","));
				String update = String.format("UPDATE ir_model_field SET %s WHERE model_id=%%s", sets);
				values.add(e.getKey());
				cr.execute(update, values);
			}
		}
	}

	public Map<String, Object> reflectFieldsParams(Records rec, MetaField field, Object modelId) {
		Map<String, Object> result = new HashMap<>(16);
		result.put("model_id", modelId);
		result.put("name", field.getName());
		result.put("label", field.getLabel());
		result.put("help", field.getHelp());
		result.put("field_type", field.getType());
		result.put("state", field.getManual() ? "manual" : "base");
		String relation = null;
		if (field instanceof RelationalField) {
			relation = ((RelationalField<?>) field).getComodel();
		}
		result.put("relation", relation);
		result.put("index", field.getIndex());
		result.put("store", field.isStore());
		result.put("copy", field.isCopy());
		String on_delete = null;
		if (field instanceof Many2oneField) {
			on_delete = ((Many2oneField) field).getOnDelete().name();
		}
		result.put("on_delete", on_delete);
		result.put("related", String.join(".", field.getRelated()));
		result.put("readonly", field.isReadonly());
		result.put("required", field.isRequired());
		// result.put("size", field.getSize());
		Boolean translate = null;
		if (field instanceof StringField) {
			translate = ((StringField<?>) field).getTranslate();
		}
		result.put("translate", translate);
		String relation_field = null, relation_table = null, column1 = null, column2 = null;
		if (field instanceof One2manyField) {
			relation_field = ((One2manyField) field).getInverseName();
		}
		if (field instanceof Many2oneField) {
			Many2oneField m2o = (Many2oneField) field;
			// TODO
			// relation_table = m2o.getRelation();
		}

		result.put("relation_field", relation_field);
		result.put("relation_table", relation_table);
		result.put("column1", column1);
		result.put("column2", column2);

		return result;
	}
}
