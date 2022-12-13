package org.jdoo.fields;

import java.util.Map;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.data.ColumnType;
import org.jdoo.data.DbColumn;
import org.jdoo.exceptions.TypeException;
import org.jdoo.exceptions.ValueException;

/**
 * 主键
 * 
 * @author lrz
 */
public class IdField extends BaseField<IdField> {
    public IdField() {
        type = Constants.CHAR;
        label = "ID";
        readonly = true;
        prefetch = false;
        columnType = ColumnType.VarChar;
    }

    @Override
    protected void updateDb(Records model, Map<String, DbColumn> columns) {
        // do nothing
    }

    @Override
    public Object convertToColumn(Object value, Records record, boolean validate) {
        // TODO Auto-generated method stub
        return super.convertToColumn(value, record, validate);
    }

    @Override
    public Object convertToRecord(Object value, Records rec) {
        // TODO Auto-generated method stub
        return super.convertToRecord(value, rec);
    }

    @Override
    public Object get(Records record) {
        if (!record.any()) {
            return "";
        }
        Object[] ids = record.getIds();
        if (ids.length == 1) {
            return ids[0];
        }
        throw new ValueException(String.format("期望单个值%s", record));
    }

    @Override
    public void set(Records records, Object value) {
        throw new TypeException("ID字段不能赋值");
    }
}
