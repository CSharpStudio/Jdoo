package com.addons.demo.models.book;

import org.jdoo.*;

/**
 * 示例
 * 
 * @author lrz
 */
@Model.Meta(name = "demo.book", label = "图书")
public class Book extends Model {
    static Field name = Field.Char().label("名称").index(true).required(true);
    static Field author = Field.Char().label("作者");
    static Field category_id = Field.Many2one("demo.category").label("分类");
}
