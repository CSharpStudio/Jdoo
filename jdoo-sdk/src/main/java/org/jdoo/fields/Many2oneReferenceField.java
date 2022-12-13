package org.jdoo.fields;
import org.jdoo.Records;
import org.jdoo.core.Constants;

import org.jdoo.data.ColumnType;

/**
 * @author lrz
 */
public class Many2oneReferenceField extends CharField {

    /**
     * 构建{@link Many2oneReferenceField}实例
     */
    public Many2oneReferenceField() {
        type = Constants.MANY2ONEREFERENCE;
        columnType = ColumnType.VarChar;
    }

    public Many2oneReferenceField(String model_field) {
        this();
        
    }
 
    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        if (value instanceof Records) {
            Records r = (Records) value;
            String[] ids = r.getIds();
            if (ids != null && ids.length > 0) {
                value = ids[0];
            } else
                value = null;
        }
        return super.convertToCache(value, rec, validate);
    }

    
 
 
}
