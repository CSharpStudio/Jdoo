package jdoo.models;

import java.util.List;
import java.util.function.Function;

import jdoo.data.PgVarchar;
import jdoo.tools.Tuple2;

public class SelectionField extends BaseField<SelectionField> {

    public SelectionField() {
        column_type = new Tuple2<String, Object>("varchar", new PgVarchar());
    }

    List<Tuple2<String, String>> selection;
    Boolean validate;
    Function<Self, Object> func;

    boolean get_validate() {
        return validate == null || validate;
    }

    public SelectionField selection(List<Tuple2<String, String>> selection) {
        this.selection = selection;
        return this;
    }

    public SelectionField selection(Function<Self, Object> func) {
        this.func = func;
        return this;
    }

    public SelectionField validate(boolean validate) {
        this.validate = validate;
        return this;
    }
}
