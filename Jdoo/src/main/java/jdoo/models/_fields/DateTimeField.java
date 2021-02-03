package jdoo.models._fields;

import jdoo.util.Tuple;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jdoo.exceptions.ValueErrorException;
import jdoo.models.RecordSet;
import jdoo.util.Pair;

public class DateTimeField extends BaseField<DateTimeField> {
    public DateTimeField() {
        column_type = new Pair<>("timestamp", "timestamp");
        column_cast_from = new Tuple<>("date");
    }

    static Date to_datetime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }

        String fmt = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat date_format = new SimpleDateFormat(fmt);
        String str = value.toString().substring(0, fmt.length());
        try {
            return date_format.parse(str);
        } catch (ParseException e) {
            throw new ValueErrorException(String.format("value %s to datetime error", value), e);
        }
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        return to_datetime(value);
    }

    @Override
    public Object convert_to_export(Object value, RecordSet record) {
        if(value == null){
            return "";
        }
        return to_datetime(value);
    }

    @Override
    public String convert_to_display_name(Object value, RecordSet record) {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = to_datetime(value);
        return date_format.format(date);
    }
}
