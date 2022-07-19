package org.jdoo.fields;

import java.util.List;
import java.util.Map;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.core.MetaModel;
import org.jdoo.data.ColumnType;

import org.jdoo.util.Tuple;

/**
 * 二进制基类
 * 
 * @author lrz
 */
public class BinaryBaseField<T extends BaseField<T>> extends BaseField<T> {
    Boolean attachment = true;

    public BinaryBaseField() {
        prefetch = false;
    }

    @SuppressWarnings("unchecked")
    public T attachment() {
        args.put("attachment", true);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T attachment(boolean attachment) {
        args.put("attachment", attachment);
        return (T) this;
    }

    public Boolean getAttachment() {
        return attachment;
    }

    @Override
    public ColumnType getColumnType() {
        return attachment ? ColumnType.None : ColumnType.Binary;
    }

    @Override
    protected Map<String, Object> getAttrs(MetaModel model, String name) {
        Map<String, Object> attrs = super.getAttrs(model, name);
        if (!(Boolean) attrs.getOrDefault(Constants.STORE, true)) {
            attrs.put("attachment", false);
        }
        return attrs;
    }

    @Override
    public Object convertToColumn(Object value, Records record, boolean validate) {
        // TODO Auto-generated method stub
        return super.convertToColumn(value, record, validate);
    }

    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        // TODO Auto-generated method stub
        return super.convertToCache(value, rec, validate);
    }

    @Override
    public Object convertToRecord(Object value, Records rec) {
        // TODO Auto-generated method stub
        return super.convertToRecord(value, rec);
    }

    @Override
    public void read(Records records) {
        // TODO Auto-generated method stub
        super.read(records);
    }

    @Override
    public void create(List<Tuple<Records, Object>> recordValues) {
        // TODO Auto-generated method stub
        super.create(recordValues);
    }

    @Override
    public Records write(Records records, Object value) {
        // TODO Auto-generated method stub
        return super.write(records, value);
    }
}
