package com.addons.demo.models.book;

import org.jdoo.*;

/**
 * 示例
 * 
 * @author lrz
 */
@Model.Meta(name = "demo.category", label = "图书分类")
public class Category extends Model {
    static Field name = Field.Char().label("名称").index(true).required(true);
    static Field sumary = Field.Char().label("备注");
}
