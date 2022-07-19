package org.jdoo.fields;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;

/**
 * 整型
 * 
 * @author lrz
 */
public class IntegerField extends BaseField<IntegerField> {
    public IntegerField() {
        type = Constants.INTEGER;
        columnType = ColumnType.Integer;
    }

    @Override
    public Object convertToColumn(Object value, Records record, boolean validate) {
        if (value == null || "".equals(value)) {
            return 0;
        }
        if (value instanceof Integer) {
            return value;
        }
        return new Integer(value.toString());
    }

    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        if (value == null || "".equals(value)) {
            return 0;
        }
        if (value instanceof Integer) {
            return value;
        }
        return new Integer(value.toString());
    }

    @Override
    public Object convertToRecord(Object value, Records rec) {
        return value == null || "".equals(value) ? 0 : value;
    }

    @Override
    public Object convertToRead(Object value, Records rec, boolean usePresent) {
        return value;
    }
}
