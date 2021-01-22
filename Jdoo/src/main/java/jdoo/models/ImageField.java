package jdoo.models;

public class ImageField extends _BinaryField<ImageField> {
    Integer max_width;
    Integer max_height;

    int get_max_width() {
        if (max_width == null)
            return 0;
        return max_width;
    }

    int get_max_height() {
        if (max_height == null)
            return 0;
        return max_height;
    }

    public ImageField max_width(int max_width){
        this.max_width = max_width;
        return this;
    }

    public ImageField max_height(int max_height){
        this.max_height = max_height;
        return this;
    }
}
