package org.jdoo.models;

import org.jdoo.Field;
import org.jdoo.Model;

@Model.Meta(inherit = "test.a")
public class A1 extends Model {    
    static Field code = Field.Char().readonly();
}

