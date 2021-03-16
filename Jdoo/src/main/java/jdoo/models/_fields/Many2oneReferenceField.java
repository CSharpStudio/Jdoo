package jdoo.models._fields;

import jdoo.models.RecordSet;
import jdoo.tools.Slot;

public class Many2oneReferenceField extends BaseField<Many2oneReferenceField> {
    public static final Slot model_field = new Slot("model_field", null);

    public Many2oneReferenceField model_field(String model_field) {
        setattr(Many2oneReferenceField.model_field, model_field);
        return this;
    }

    public String _model_field() {
        return getattr(String.class, model_field);
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        // cache format: id or None
        if (value instanceof RecordSet) {
            RecordSet rec = (RecordSet) value;
            value = rec.hasId() ? rec.ids().get(0) : null;
        }
        return super.convert_to_cache(value, record, validate);
    }

    // todo
}
