package org.jdoo.fields;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;

/**
 * 布尔
 * 
 * @author lrz
 */
public class BooleanField extends BaseField<BooleanField> {
    public BooleanField() {
        columnType = ColumnType.Boolean;
        type = Constants.BOOLEAN;
    }

    @Override
    public Object convertToColumn(Object value, Records record, boolean validate) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return value;
        }
        if (value instanceof String) {
            return new Boolean((String) value);
        }
        return false;
    }

    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return value;
        }
        if (value instanceof String) {
            return new Boolean((String) value);
        }
        return false;
    }

    @Override
    public Object convertToExport(Object value, Records rec) {
        return value;
    }
}
