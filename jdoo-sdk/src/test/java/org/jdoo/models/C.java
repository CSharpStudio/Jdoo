package org.jdoo.models;

import org.jdoo.Field;
import org.jdoo.Model;

@Model.Meta(name="test.c", inherit="test.a")
public class C extends Model {
    static Field code = Field.Char().copy();
}
