package org.jdoo.utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.jdoo.exceptions.ValueException;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static Date atZone(Date date, TimeZone zone) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dt = format.format(date);
        format.setTimeZone(zone);
        try {
            return new Date(format.parse(dt).getTime());
        } catch (Exception exc) {
            throw new ValueException("时区转换失败", exc);
        }
    }

    public static Timestamp atZone(Timestamp date, TimeZone zone) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dt = format.format(date);
        format.setTimeZone(zone);
        try {
            return new Timestamp(format.parse(dt).getTime());
        } catch (Exception exc) {
            throw new ValueException("时区转换失败", exc);
        }
    }

    public static java.util.Date atZone(java.util.Date date, TimeZone zone) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dt = format.format(date);
        format.setTimeZone(zone);
        try {
            return format.parse(dt);
        } catch (Exception exc) {
            throw new ValueException("时区转换失败", exc);
        }
    }

    public static Date toUTC(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dt = format.format(date);
        format.setTimeZone(TimeZone.getDefault());
        try {
            return new Date(format.parse(dt).getTime());
        } catch (Exception exc) {
            throw new ValueException("时区转换失败", exc);
        }
    }

    public static Timestamp toUTC(Timestamp date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dt = format.format(date);
        format.setTimeZone(TimeZone.getDefault());
        try {
            return new Timestamp(format.parse(dt).getTime());
        } catch (Exception exc) {
            throw new ValueException("时区转换失败", exc);
        }
    }

    public static java.util.Date toUTC(java.util.Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dt = format.format(date);
        format.setTimeZone(TimeZone.getDefault());
        try {
            return format.parse(dt);
        } catch (Exception exc) {
            throw new ValueException("时区转换失败", exc);
        }
    }
}
