package org.jdoo.fields;

import org.jdoo.core.Constants;

/**
 * 图片
 * 
 * @author lrz
 */
public class ImageField extends BinaryBaseField<ImageField> {
    Integer maxWidth;
    Integer maxHeight;

    public ImageField() {
        type = Constants.IMAGE;
    }
}
