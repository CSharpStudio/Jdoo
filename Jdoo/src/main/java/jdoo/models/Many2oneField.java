package jdoo.models;

import jdoo.tools.Tuple2;

public class Many2oneField extends _RelationalField<Many2oneField> {
    public Many2oneField() {
        column_type = new Tuple2<String, Object>("varchar", "varchar");
    }

    String ondelete;
    Boolean auto_join;
    Boolean delegate;

    boolean get_auto_join() {
        return auto_join != null && auto_join;
    }

    boolean get_delegate() {
        return delegate != null && delegate;
    }

    public Many2oneField ondelete(String ondelete) {
        this.ondelete = ondelete;
        return this;
    }

    public Many2oneField auto_join(boolean auto_join) {
        this.auto_join = auto_join;
        return this;
    }
}
