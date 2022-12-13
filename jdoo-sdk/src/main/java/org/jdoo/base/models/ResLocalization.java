package org.jdoo.base.models;

import org.jdoo.*;

@Model.Meta(name = "res.localization", label = "本地化", order = "name")
public class ResLocalization extends Model {
    static Field name = Field.Char().label("名称").required().length(2000);
    static Field value = Field.Char().label("翻译值").length(2000);
    static Field lang_id = Field.Many2one("res.lang").label("语言");
}
