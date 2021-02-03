package jdoo.models._fields;

import jdoo.util.Tuple;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jdoo.exceptions.ValueErrorException;
import jdoo.models.RecordSet;
import jdoo.util.Pair;

public class DateField extends BaseField<DateField> {
    public DateField() {
        column_type = new Pair<>("date", "date");
        column_cast_from = new Tuple<>("timestamp");
    }

    public static Date today() {
        return new Date();
    }

    public static Date to_date(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        String fmt = "yyyy-MM-dd";
        SimpleDateFormat date_format = new SimpleDateFormat(fmt);
        String str = value.toString().substring(0, fmt.length());
        try {
            return date_format.parse(str);
        } catch (ParseException e) {
            throw new ValueErrorException(String.format("value %s to date error", value), e);
        }
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        if (value == null) {
            return null;
        }
        return to_date(value);
    }

    @Override
    public Object convert_to_export(Object value, RecordSet record) {
        if (value == null) {
            return "";
        }
        return to_date(value);
    }
}
