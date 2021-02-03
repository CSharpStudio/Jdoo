package jdoo.models._fields;

import jdoo.util.Tuple;
import jdoo.util.TypeUtils;
import jdoo.apis.Environment;
import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.util.Dict;
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
        set(group_operator, "sum");
        column_cast_from = new Tuple<>("int4", "numeric", "float8");
    }

    @Override
    public Pair<String, Object> column_type() {
        if (column_type == null) {
            if (has(_digits))
                column_type = new Pair<>("numeric", "numeric");
            else
                column_type = new Pair<>("float8", "double precision");
        }
        return column_type;
    }

    @SuppressWarnings("unchecked")
    public Pair<Integer, Integer> get_digits(Environment env) {
        Object digits = get(_digits);
        if (digits instanceof String) {
            int precision = env.get("decimal.precision").call(Integer.class, "precision_get", digits);
            return new Pair<>(16, precision);
        }
        return (Pair<Integer, Integer>) digits;
    }

    public FloatField digits(String digits) {
        set(_digits, digits);
        return this;
    }

    public FloatField digits(Pair<Integer, Integer> digits) {
        set(_digits, digits);
        return this;
    }

    public FloatField digits(int precision, int scale) {
        set(_digits, new Pair<>(precision, scale));
        return this;
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Dict values, boolean validate) {
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
