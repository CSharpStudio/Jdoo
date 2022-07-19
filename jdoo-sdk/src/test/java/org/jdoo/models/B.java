package org.jdoo.models;

import org.jdoo.Field;
import org.jdoo.Model;

@Model.Meta(name = "test.b")
public class B extends Model {    
    static Field code = Field.Char().label("B Code").readonly();
}
