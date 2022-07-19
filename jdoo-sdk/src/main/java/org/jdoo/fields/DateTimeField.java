package org.jdoo.fields;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;

/**
 * 日期时间
 * 
 * @author lrz
 */
public class DateTimeField extends BaseField<DateTimeField> {
    public DateTimeField() {
        type = Constants.DATETIME;
        columnType = ColumnType.DateTime;
    }

    @Override
    public Object convertToRead(Object value, Records rec, boolean usePresent) {
        if (value instanceof Timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
