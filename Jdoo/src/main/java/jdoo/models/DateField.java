package jdoo.models;

import jdoo.tools.Tuple2;

public class DateField extends BaseField<DateField> {
    public DateField(){
        column_type = new Tuple2<String, Object>("date", "date");
    }
}
