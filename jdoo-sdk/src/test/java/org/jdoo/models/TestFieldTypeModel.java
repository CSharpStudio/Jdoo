package org.jdoo.models;

import org.jdoo.*;

@Model.Meta(name = "test.field_type", description = "模型字段类型")
public class TestFieldTypeModel extends Model {
    static Field f_binary = Field.Binary().label("Binary").attachment(false);
    static Field f_boolean = Field.Boolean().label("Boolean");
    static Field f_char = Field.Char().label("Char");
    static Field f_date = Field.Date().label("Date");
    static Field f_datetime = Field.DateTime().label("DateTime");
    static Field f_float = Field.Float().label("Float");
    static Field f_html = Field.Html().label("Html");
    static Field f_image = Field.Image().label("Image").attachment(false);
    static Field f_int = Field.Integer().label("Integer");
    static Field f_selection = Field.Selection().label("Selection");
    static Field f_text = Field.Text().label("Text");
}
