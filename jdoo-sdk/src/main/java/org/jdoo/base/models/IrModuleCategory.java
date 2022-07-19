package org.jdoo.base.models;

import org.jdoo.*;

/**
 * 模块分类
 *
 * @author
 */
@Model.Meta(name = "ir.module.category", description = "模块分类")
public class IrModuleCategory extends Model {
	static Field name = Field.Char().label("名称").help("模块的名称");
	static Field module_ids = Field.One2many("ir.module", "category_id");

	/** 名称 */
	public String getName() {
		return (String) get(name);
	}

	/** 名称 */
	public void setName(String value) {
		set(name, value);
	}
}
