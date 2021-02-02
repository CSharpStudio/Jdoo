package jdoo.models._fields;

import java.util.List;
import java.util.function.Function;

import jdoo.data.PgVarchar;
import jdoo.models.RecordSet;
import jdoo.util.Pair;

public class SelectionField extends BaseField<SelectionField> {

    public SelectionField() {
        column_type = new Pair<>("varchar", new PgVarchar());
    }

    List<Pair<String, String>> selection;
    Boolean validate;
    Function<RecordSet, Object> func;

    boolean get_validate() {
        return validate == null || validate;
    }

    public SelectionField selection(List<Pair<String, String>> selection) {
        this.selection = selection;
        return this;
    }

    public SelectionField selection(Function<RecordSet, Object> func) {
        this.func = func;
        return this;
    }

    public SelectionField validate(boolean validate) {
        this.validate = validate;
        return this;
    }
}
