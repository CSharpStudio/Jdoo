package jdoo.models._fields;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.util.Pair;
import jdoo.util.Utils;

public class ImageField extends _BinaryField<ImageField> {
    public static final Slot max_width = new Slot("max_width");
    public static final Slot max_height = new Slot("max_height");

    static {
        default_slots().put(max_width, 0);
        default_slots().put(max_height, 0);
    }

    public ImageField attachment(boolean attachment) {
        setattr(BinaryField.attachment, attachment);
        return this;
    }

    public ImageField max_width(int max_width) {
        setattr(ImageField.max_width, max_width);
        return this;
    }

    public ImageField max_height(int max_height) {
        setattr(ImageField.max_height, max_height);
        return this;
    }

    @Override
    public void create(List<Pair<RecordSet, Object>> record_values) {

        super.create(record_values);
    }

    @Override
    public RecordSet write(RecordSet records, Object value) {
        Object new_value = _image_process(value);
        RecordSet result = super.write(records, value);
        Collection<String> related = related();
        records.env().cache().update(records, this,
                Utils.mutli(Arrays.asList(related != null && !related.isEmpty() ? value : new_value), records.size()));
        return result;
    }

    Object _image_process(Object value) {
        // if value and (self.max_width or self.max_height):
        // value = image_process(value, size=(self.max_width, self.max_height))
        return value;// todo
    }
}
