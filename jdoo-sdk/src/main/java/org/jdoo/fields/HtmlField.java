package org.jdoo.fields;

import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;

/**
 * 超文本
 * 
 * @author lrz
 */
public class HtmlField extends StringField<HtmlField> {
    public HtmlField() {
        type = Constants.HTML;
        columnType = ColumnType.Text;
    }
}
