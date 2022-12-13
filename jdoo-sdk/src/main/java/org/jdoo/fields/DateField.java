package org.jdoo.fields;

import java.sql.Date;
import java.text.SimpleDateFormat;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.exceptions.ValueException;
import org.jdoo.util.ServerDate;

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
    public Object convertToRecord(Object value, Records rec) {
        return toDate(value, rec);
    }

    /**
     * 转成本地日期
     * 
     * @param value
     * @param rec
     * @param usePresent
     * @return
     */
    @Override
    public Object convertToRead(Object value, Records rec, boolean usePresent) {
        if (value == null) {
            return "";
        }
        Date dt = value instanceof ServerDate ? new Date(System.currentTimeMillis())
                : toDate(value, rec);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(dt);
    }

    /**
     * 转成本地日期
     * 
     * @param value
     * @param rec
     * @param validate
     * @return
     */
    @Override
    public Object convertToColumn(Object value, Records rec, boolean validate) {
        return convertToCache(value, rec, validate);
    }

    /**
     * 转成本地日期
     * 
     * @param value
     * @param rec
     * @param validate
     * @return
     */
    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        if (value == null) {
            if (validate && isRequired()) {
                throw new ValidationException(rec.l10n("%s 不能为空", getLabel()));
            }
            return null;
        }
        if (value instanceof ServerDate) {
            return value;
        }
        return toDate(value, rec);
    }

    /**
     * 转成本地日期
     * 
     * @param value
     * @param rec
     * @return
     */
    @Override
    public Object convertToWrite(Object value, Records rec) {
        return convertToCache(value, rec, true);
    }

    Date toDate(Object value, Records rec) {
        if (value == null || value instanceof Date) {
            return (Date) value;
        }
        try {
            if (value instanceof String) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                return new Date(format.parse((String) value).getTime());
            }
            if (value instanceof java.util.Date) {
                return new Date(((java.util.Date) value).getTime());
            }
        } catch (Exception exc) {
            throw new ValueException(String.format("%s不支持转换成Date", value), exc);
        }
        throw new ValueException(String.format("%s不支持转换成Date", value));
    }
}
