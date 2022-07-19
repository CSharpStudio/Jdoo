package org.jdoo.models;

import org.jdoo.*;

@Model.Meta(name = "test.model", description = "模型元数据")
public class TestModel extends Model {
	static Field name = Field.Char().label("名称").help("模型名称").index(true).required(true);// .translate();
	static Field description = Field.Char().label("描述").help("模型说明");
	static Field inherit = Field.Char().label("继承").help("模型的继承，多个使用逗号','分隔");
	static Field table = Field.Char().label("表名").help("映射数据库的表名");
	static Field order = Field.Char().label("排序").help("默认排序SQL, 如 name ASC, date DESC");
	static Field present = Field.Char().label("呈现").help("记录呈现的字段名，默认尝试使用name字段呈现");
	static Field type = Field.Selection().label("类型").help("模型的类型：普通、抽象、瞬态");
	static Field model_class = Field.Char().label("类").help("对应代码的类");
	// static Field module_id = Field.Many2one("ir.module");
	// static Field field_ids = Field.One2many("ir.field", "model_id");
	static Field type_name = Field.Char().label("类名").compute(Callable.script("r->r.get('name')+'('+r.get('type')+')'"))
			.store(false).depends("name", "type");

	/** 名称 */
	public String get_name() {
		return (String) get(name);
	}

	/** 名称 */
	public void set_name(String value) {
		set(name, value);
	}

	/** 描述 */
	public String get_description() {
		return (String) get(description);
	}

	/** 描述 */
	public void set_description(String value) {
		set(description, value);
	}

	/** 继承 */
	public String get_inherit() {
		return (String) get(inherit);
	}

	/** 继承 */
	public void set_inherit(String value) {
		set(inherit, value);
	}

	/** 表名 */
	public String get_table() {
		return (String) get(table);
	}

	/** 表名 */
	public void set_table(String value) {
		set(table, value);
	}

	/** 排序 */
	public String get_order() {
		return (String) get(order);
	}

	/** 排序 */
	public void set_order(String value) {
		set(order, value);
	}

	/** 呈现 */
	public String get_present() {
		return (String) get(present);
	}

	/** 呈现 */
	public void set_present(String value) {
		set(present, value);
	}

	/** 类型 */
	public String get_type() {
		return (String) get(type);
	}

	/** 类型 */
	public void set_type(String value) {
		set(type, value);
	}

	/** 类型 */
	public String get_type_name() {
		return (String) get(type_name);
	}

	/** 类 */
	public String get_model_class() {
		return (String) get(model_class);
	}

	/** 类 */
	public void set_model_class(String value) {
		set(model_class, value);
	}
}
