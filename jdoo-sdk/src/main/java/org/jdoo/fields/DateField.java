package org.jdoo.fields;

import java.sql.Date;
import java.text.SimpleDateFormat;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;

/**
 * 日期
 * 
 * @author lrz
 */
public class DateField extends BaseField<DateField> {
    public DateField() {
        type = Constants.DATE;
        columnType = ColumnType.Date;
    }

    @Override
    public Object convertToRead(Object value, Records rec, boolean usePresent) {
        if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(value);
        }
        return value;
    }

    @Override
    public Object convertToColumn(Object value, Records record, boolean validate) {
        // TODO Auto-generated method stub
        return super.convertToColumn(value, record, validate);
    }

    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        // TODO Auto-generated method stub
        return super.convertToCache(value, rec, validate);
    }

    @Override
    public Object convertToWrite(Object value, Records rec) {
        // TODO Auto-generated method stub
        return super.convertToWrite(value, rec);
    }
}
