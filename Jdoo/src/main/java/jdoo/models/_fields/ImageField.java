package jdoo.models._fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.util.Pair;
import jdoo.util.Utils;

public class ImageField extends _BinaryField<ImageField> {
    public static final Slot max_width = new Slot("max_width", 0);
    public static final Slot max_height = new Slot("max_height", 0);

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
        List<Pair<RecordSet, Object>> new_record_values = new ArrayList<>();
        for (Pair<RecordSet, Object> pair : record_values) {
            RecordSet record = pair.first();
            Object value = pair.second();
            Object new_value = _image_process(value);
            new_record_values.add(new Pair<>(record, new_value));
            record.env().cache().update(record, this,
                    Utils.mutli(Arrays.asList(!_related().isEmpty() ? value : new_value), record.size()));
        }
        super.create(new_record_values);
    }

    @Override
    public Object write(RecordSet records, Object value) {
        Object new_value = _image_process(value);
        Object result = super.write(records, value);
        boolean related = !_related().isEmpty();
        records.env().cache().update(records, this,
                Utils.mutli(Arrays.asList(related ? value : new_value), records.size()));
        return result;
    }

    Object _image_process(Object value) {
        // if value and (self.max_width or self.max_height):
        // value = image_process(value, size=(self.max_width, self.max_height))
        return value;// todo
    }

    /** Override to resize the related value before saving it on self. */
    @Override
    protected Object _process_related(Object value) {
        return _image_process(super._process_related(value));
    }
}
