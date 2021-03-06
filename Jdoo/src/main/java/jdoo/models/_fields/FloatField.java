package jdoo.models._fields;

import jdoo.util.Tuple;
import jdoo.util.TypeUtils;
import jdoo.apis.Environment;
import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.util.Kvalues;
import jdoo.util.Pair;

/**
 * The precision digits are given by the attribute.
 * 
 * :param digits: a pair (total, decimal), or a function taking a database
 * cursor and returning a pair (total, decimal)
 */
public class FloatField extends BaseField<FloatField> {
    /** digits argument passed to class initializer */
    public static final Slot _digits = new Slot("_digits");

    public FloatField() {
        setattr(Slots.group_operator, "sum");
        column_cast_from = new Tuple<>("int4", "numeric", "float8");
    }

    @Override
    public Pair<String, Object> column_type() {
        if (column_type == null) {
            if (hasattr(_digits))
                column_type = new Pair<>("numeric", "numeric");
            else
                column_type = new Pair<>("float8", "double precision");
        }
        return column_type;
    }

    public Pair<Integer, Integer> get_digits(Environment env) {
        Object digits = getattr(_digits);
        if (digits instanceof String) {
            int precision = env.get("decimal.precision").call(Integer.class, "precision_get", digits);
            return new Pair<>(16, precision);
        }
        return (Pair<Integer, Integer>) digits;
    }

    /** digits name from decimal.precision */
    public FloatField digits(String digits) {
        setattr(_digits, digits);
        return this;
    }

    /** a pair(total, decimal) */
    public FloatField digits(Pair<Integer, Integer> digits) {
        setattr(_digits, digits);
        return this;
    }

    /** total, decimal */
    public FloatField digits(int precision, int scale) {
        setattr(_digits, new Pair<>(precision, scale));
        return this;
    }

    /** total */
    public FloatField digits(int precision) {
        setattr(_digits, new Pair<>(precision, 0));
        return this;
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Kvalues values, boolean validate) {
        double result = TypeUtils.toDouble(value);
        Pair<Integer, Integer> digits = get_digits(record.env());
        if (digits != null) {
            result = TypeUtils.round(result, digits.second());
        }
        return result;
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        double result = TypeUtils.toDouble(value);
        if (!validate) {
            return result;
        }
        Pair<Integer, Integer> digits = get_digits(record.env());
        if (digits != null) {
            result = TypeUtils.round(result, digits.second());
        }
        return result;
    }

    @Override
    public Object convert_to_record(Object value, RecordSet record) {
        return value == null ? 0.0 : value;
    }
}
