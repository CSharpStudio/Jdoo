package org.jdoo.models;

import org.jdoo.Field;
import org.jdoo.Model;

@Model.Meta(name = "test.a")
public class A extends Model {
    static Field code = Field.Char().label("编码").index().required();
}
