package org.jdoo.fields;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;

/**
 * 大文本
 * 
 * @author lrz
 */
public class TextField extends StringField<TextField> {
    public TextField() {
        type = Constants.TEXT;
        columnType = ColumnType.Text;
    }

    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
