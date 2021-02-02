package jdoo.models._fields;

import java.util.Map;

import jdoo.models.RecordSet;
import jdoo.util.Dict;

public class One2manyField extends _RelationalMultiField<One2manyField> {
    public One2manyField(){
        copy = false;
    }
    String inverse_name;
    Boolean auto_join;
    Integer limit;
    
    

    public One2manyField inverse_name(String inverse_name) {
        this.inverse_name = inverse_name;
        return this;
    }

    public One2manyField limit(int limit) {
        this.limit = limit;
        return this;
    }

    public One2manyField auto_join(boolean auto_join) {
        this.auto_join = auto_join;
        return this;
    }

    @Override
    public boolean update_db(RecordSet model, Map<String, Dict> columns) {
        return true;
    }
}
