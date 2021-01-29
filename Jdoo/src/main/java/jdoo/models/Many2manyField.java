package jdoo.models;

import java.util.Map;

import jdoo.util.Dict;

public class Many2manyField extends _RelationalMultiField<Many2manyField> {
    public Many2manyField() {

    }

    boolean _explicit = true;
    String relation;
    String column1;
    String column2;
    Boolean auto_join;
    Integer limit;
    String ondelete;

    public Many2manyField relation(String relation) {
        this.relation = relation;
        return this;
    }

    public Many2manyField column1(String column1) {
        this.column1 = column1;
        return this;
    }

    public Many2manyField column2(String column2) {
        this.column2 = column2;
        return this;
    }

    public Many2manyField ondelete(String ondelete) {
        this.ondelete = ondelete;
        return this;
    }

    public Many2manyField limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Many2manyField auto_join(boolean auto_join) {
        this.auto_join = auto_join;
        return this;
    }

    @Override
    public boolean update_db(Self model, Map<String, Dict> columns) {
        return true;
    }
}
