package org.jdoo.fields;

import org.jdoo.data.ColumnType;
import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.SqlDialect;
import org.jdoo.utils.StringUtils;

/**
 * 小数
 * 
 * @author lrz
 */
public class FloatField extends BaseField<FloatField> {
    String digits;

    public FloatField() {
        type = Constants.FLOAT;
        columnType = ColumnType.Float;
        relatedAttributes.add("digits");
    }

    public FloatField digits(String digits) {
        args.put("digits", digits);
        return this;
    }

    @Override
    public String getDbColumnType(SqlDialect sqlDialect) {
        if (StringUtils.isNotBlank(digits)) {
            // TODO
        }
        return super.getDbColumnType(sqlDialect);
    }
    
    @Override
    public Object convertToColumn(Object value, Records record, boolean validate) {
        if (value == null || "".equals(value)) {
            return 0;
        }
        if (value instanceof Double) {
            return value;
        }
        return new Double(value.toString());
    }

    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        if (value == null || "".equals(value)) {
            return 0;
        }
        if (value instanceof Double) {
            return value;
        }
        return new Double(value.toString());
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
