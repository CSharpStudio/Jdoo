package org.jdoo.fields;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.util.ServerDate;
import org.jdoo.utils.DateUtils;

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
    public Object convertToRecord(Object value, Records rec) {
        return toTimestamp(value, rec);
    }

    /**
     * 转为用户的时区
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
        Timestamp ts = value instanceof ServerDate ? new Timestamp(System.currentTimeMillis())
                : toTimestamp(value, rec);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(getZone(rec));
        return format.format(ts);
    }

    /**
     * 转成UTC时间存储
     * 
     * @param value
     * @param rec
     * @param validate
     * @return
     */
    @Override
    public Object convertToColumn(Object value, Records rec, boolean validate) {
        if (value == null) {
            if (validate && isRequired()) {
                throw new ValidationException(rec.l10n("%s 不能为空", getLabel()));
            }
            return null;
        }
        if (value instanceof ServerDate) {
            return value;
        }
        Timestamp ts = toTimestamp(value, rec);
        return DateUtils.toUTC(ts);
    }

    /**
     * 转成服务器时区的本地日期时间
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
        return toTimestamp(value, rec);
    }

    /**
     * 转成服务器时区的本地日期时间
     * 
     * @param value
     * @param rec
     * @return
     */
    @Override
    public Object convertToWrite(Object value, Records rec) {
        return convertToCache(value, rec, true);
    }

    Timestamp toTimestamp(Object value, Records rec) {
        if (value == null || value instanceof Timestamp) {
            return (Timestamp) value;
        }
        try {
            if (value instanceof String) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                format.setTimeZone(getZone(rec));
                return new Timestamp(format.parse((String) value).getTime());
            }
            if (value instanceof Date) {
                return new Timestamp(((Date) value).getTime());
            }
        } catch (Exception exc) {
            throw new ValidationException(rec.l10n("%s不支持转换成Timestamp", value), exc);
        }
        throw new ValidationException(rec.l10n("%s不支持转换成Timestamp", value));
    }

    TimeZone getZone(Records rec) {
        String tz = (String) rec.getEnv().getTimezone();
        return TimeZone.getTimeZone(tz);
    }
}
