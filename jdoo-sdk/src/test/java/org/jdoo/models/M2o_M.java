package org.jdoo.models;

import org.jdoo.*;

@Model.Meta(name = "test.m2o_m")
public class M2o_M extends Model {
	static Field name = Field.Char().label("名称");
	static Field one_id = Field.Many2one("test.m2o_o");
}
