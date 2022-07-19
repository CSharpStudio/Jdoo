package org.jdoo.base.models;
import org.jdoo.*;

/**
 * 模块依赖
 *
 * @author
*/
@Model.Meta(name="ir.module.dependency", description="模块依赖")
public class IrModuleDependency extends Model{
	static Field name = Field.Char().label("名称").help("模块的名称");
	static Field module_id = Field.Many2one("ir.module");
	/** 名称 */
	public String getName(){
		return (String)get(name);
	}
	/** 名称 */
	public void setName(String value){
		set(name, value);
	}
}
