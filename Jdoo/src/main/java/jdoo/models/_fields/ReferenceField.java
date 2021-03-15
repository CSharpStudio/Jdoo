package jdoo.models._fields;

import jdoo.models.RecordSet;
import jdoo.util.Kvalues;

public class ReferenceField extends SelectionField {
    // todo
    @Override
    public Object convert_to_column(Object value, RecordSet record, Kvalues values, boolean validate) {
        // TODO Auto-generated method stub
        return super.convert_to_column(value, record, values, validate);
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        // TODO Auto-generated method stub
        return super.convert_to_cache(value, record, validate);
    }

    @Override
    public Object convert_to_record(Object value, RecordSet record) {
        // TODO Auto-generated method stub
        return super.convert_to_record(value, record);
    }

    @Override
    public Object convert_to_read(Object value, RecordSet record) {
        // TODO Auto-generated method stub
        return super.convert_to_read(value, record);
    }

    @Override
    public Object convert_to_export(Object value, RecordSet record) {
        // TODO Auto-generated method stub
        return super.convert_to_export(value, record);
    }

    @Override
    public String convert_to_display_name(Object value, RecordSet record) {
        // TODO Auto-generated method stub
        return super.convert_to_display_name(value, record);
    }
}
