package jdoo.models._fields;

import jdoo.util.Pair;

public class IntegerField extends BaseField<IntegerField> {
    public IntegerField(){
        group_operator = "sum";
        column_type = new Pair<>("int4", "int4");
    }
}
