package org.jdoo.fields;

import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;

/**
 * 二进制
 * 
 * @author lrz
 */
public class BinaryField extends BinaryBaseField<BinaryField> {
    public BinaryField() {
        type = Constants.BINARY;
        columnType = ColumnType.Binary;
    }
}
