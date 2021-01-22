package jdoo.models;

import jdoo.tools.Tuple2;

public class IntegerField extends BaseField<IntegerField> {
    public IntegerField(){
        group_operator = "sum";
        column_type = new Tuple2<String, Object>("int4", "int4");
    }
}
