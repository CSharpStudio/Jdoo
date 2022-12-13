package org.jdoo.fields;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.utils.ObjectUtils;

/**
 * 整型
 * 
 * @author lrz
 */
public class IntegerField extends BaseField<IntegerField> {
    public IntegerField() {
        type = Constants.INTEGER;
        columnType = ColumnType.Integer;
    }

    @Related
    Integer max;
    @Related
    Integer min;

    public IntegerField max(Integer max) {
        args.put("max", max);
        return this;
    }

    public IntegerField min(Integer min) {
        args.put("min", min);
        return this;
    }

    public Integer getMax() {
        return max;
    }

    public Integer getMin() {
        return min;
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
        Integer num = ObjectUtils.toInt(value);
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
        return ObjectUtils.toInt(value);
    }
}
