package org.jdoo.fields;

import org.jdoo.data.ColumnType;
import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.SqlDialect;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.utils.ObjectUtils;
import org.jdoo.utils.StringUtils;

/**
 * 小数
 * 
 * @author lrz
 */
public class FloatField extends BaseField<FloatField> {

    @Related
    String digits;
    @Related
    Double max;
    @Related
    Double min;

    public FloatField max(Double max) {
        args.put("max", max);
        return this;
    }

    public FloatField min(Double min) {
        args.put("min", min);
        return this;
    }

    public Double getMax() {
        return max;
    }

    public Double getMin() {
        return min;
    }

    public FloatField() {
        type = Constants.FLOAT;
        columnType = ColumnType.Float;
    }

    public FloatField digits(String digits) {
        args.put("digits", digits);
        return this;
    }

    @Override
    public String getDbColumnType(SqlDialect sqlDialect) {
        if (StringUtils.isNotBlank(digits)) {
            // TODO
        }
        return super.getDbColumnType(sqlDialect);
    }

    @Override
    public Object convertToColumn(Object value, Records record, boolean validate) {
        return convertToCache(value, record, validate);
    }

    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        if (value == null || "".equals(value)) {
            if (validate && isRequired()) {
                throw new ValidationException(rec.l10n("%s 不能为空", getLabel()));
            }
            return null;
        }
        Double num = ObjectUtils.toDouble(value);
        if (max != null && num > max) {
            if (validate) {
                throw new ValidationException(rec.l10n("%s 不能大于最大值 %s", max));
            } else {
                num = max;
            }
        }
        if (min != null && num < min) {
            if (validate) {
                throw new ValidationException(rec.l10n("%s 不能小于最小值 %s", min));
            } else {
                num = min;
            }
        }
        return num;
    }

    @Override
    public Object convertToRecord(Object value, Records rec) {
        if (value == null || "".equals(value)) {
            return isRequired() ? 0 : null;
        }
        return ObjectUtils.toDouble(value);
    }
}
