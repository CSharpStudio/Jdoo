package org.jdoo.fields;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;
import org.jdoo.data.DbColumn;
import org.jdoo.data.SqlDialect;

/**
 * 文本
 * 
 * @author lrz
 */
public class CharField extends StringField<CharField> {
    int size = 240;
    Boolean trim;

    public CharField() {
        type = Constants.CHAR;
        columnType = ColumnType.VarChar;
    }

    public CharField size(Integer size) {
        args.put("size", size);
        return this;
    }

    public CharField trim(Boolean trim) {
        args.put("trim", trim);
        return this;
    }

    @Override
    public String getDbColumnType(SqlDialect sqlDialect) {
        return sqlDialect.getColumnType(columnType, size, null);
    }

    @Override
    protected void updateDbColumn(Records model, DbColumn column) {
        // TODO the column's varchar size does not match self.size; convert it
        super.updateDbColumn(model, column);
    }

    @Override
    public Object convertToColumn(Object value, Records record, boolean validate) {
        if (value == null || "".equals(value)) {
            return null;
        }
        String str = value.toString();
        if (str.length() > size) {
            return str.substring(0, size);
        }
        return str;
    }

    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        if (value == null) {
            return null;
        }
        String str = value.toString();
        if (str.length() > size) {
            return str.substring(0, size);
        }
        return str;
    }
}
