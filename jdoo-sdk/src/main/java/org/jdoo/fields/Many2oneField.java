package org.jdoo.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.core.MetaField;
import org.jdoo.core.MetaModel;
import org.jdoo.data.ColumnType;
import org.jdoo.data.DbColumn;
import org.jdoo.data.SqlDialect;
import org.jdoo.exceptions.MissingException;
import org.jdoo.exceptions.ValueException;
import org.jdoo.util.Cache;
import org.jdoo.util.ToUpdate.IdValues;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 多对一
 * 
 * @author lrz
 */
public class Many2oneField extends RelationalField<Many2oneField> {

    DeleteMode ondelete;

    public Many2oneField() {
        type = Constants.MANY2ONE;
        columnType = ColumnType.VarChar;
    }

    public Many2oneField(String comodel) {
        this();
        args.put("comodelName", comodel);
    }

    public Many2oneField ondelete(DeleteMode ondelete) {
        args.put("ondelete", ondelete);
        return this;
    }

    public DeleteMode getOnDelete() {
        return ondelete;
    }

    @Override
    public String getDbColumnType(SqlDialect sqlDialect) {
        return sqlDialect.getColumnType(columnType, 13, null);
    }

    @Override
    protected void setupBase(MetaModel model, String name) {
        super.setupBase(model, name);
        if (ondelete == null) {
            MetaModel comodel = model.getRegistry().get(getComodel());
            if (model.isTransient() && !comodel.isTransient()) {
                ondelete = isRequired() ? DeleteMode.Cascade : DeleteMode.SetNull;
            } else {
                ondelete = isRequired() ? DeleteMode.Restrict : DeleteMode.SetNull;
            }
        }

        if (ondelete == DeleteMode.SetNull && isRequired()) {
            throw new ValueException(String.format("模型[%s]多对一字段[%s]是必填,但ondelete设为SetNull，只能是Restrict/Cascade",
                    model.getName(), getName()));
        }

        // TODO model_relate in ir_models 不能设置ondelete为Restrict
    }

    @Override
    protected void updateDb(Records model, Map<String, DbColumn> columns) {
        MetaModel meta = model.getMeta();
        MetaModel comodel = meta.getRegistry().get(getComodel());
        if (!meta.isTransient() && comodel.isTransient()) {
            throw new ValueException(String.format("多对一[%s]禁止从Model关联TransientModel"));
        }
        super.updateDb(model, columns);
    }

    @Override
    protected void updateDbColumn(Records model, DbColumn column) {
        super.updateDbColumn(model, column);
        // TODO update foreign key
    }

    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        Object id = null;
        if (value instanceof String) {
            id = value;
        } else if (value instanceof Records) {
            Records r = (Records) value;
            boolean error = validate && (comodelName != r.getMeta().getName() || r.size() > 1);
            if (error) {
                throw new ValueException(String.format("字段%s的值%s无效", this, value));
            }
            id = r.any() ? r.getIds()[0] : null;
        } else if (value instanceof List<?>) {
            // value is either a Tuple (id, name), or a tuple of ids
            List<?> lst = (List<?>) value;
            id = !lst.isEmpty() ? lst.get(0) : null;
        }
        // TODO Map values

        return id;
    }

    @Override
    public Object convertToRecord(Object value, Records rec) {
        String[] ids = StringUtils.isEmpty((String) value) ? ArrayUtils.EMPTY_STRING_ARRAY
                : new String[] { (String) value };
        Supplier<String[]> prefetchIds = () -> prefetchMany2oneIds(rec, this);
        return rec.getEnv().getRegistry().get(comodelName).browse(rec.getEnv(), ids, prefetchIds);
    }

    String[] prefetchMany2oneIds(Records rec, MetaField field) {
        Supplier<String[]> prefetchIds = rec.getPrefetchIds();
        String[] p = prefetchIds != null ? prefetchIds.get() : ArrayUtils.EMPTY_STRING_ARRAY;
        Records records = rec.browse(p);
        Set<String> ids = new HashSet<>();
        for (Object v : rec.getEnv().getCache().getValues(records, field)) {
            if (v != null) {
                ids.add((String) v);
            }
        }
        return ids.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public Object convertToRead(Object value, Records rec, boolean usePresent) {
        Records r = (Records) value;
        if (usePresent && r.any()) {
            List<Object[]> list = r.getPresent();
            return list.get(0);
        }
        return r.getId();
    }

    @Override
    public Object convertToColumn(Object value, Records record, boolean validate) {
        if (value instanceof List) {
            return ((List<?>) value).get(0);
        }
        return value;
    }

    @Override
    public Object convertToWrite(Object value, Records rec) {
        if (value == null || value instanceof String) {
            return value;
        }
        if (value instanceof Records) {
            Records r = (Records) value;
            if (r.getMeta().getName() == getComodel()) {
                return r.getId();
            }
        }
        if (value instanceof List<?>) {
            // value is either a Tuple (id, name), or a tuple of ids
            List<?> lst = (List<?>) value;
            return !lst.isEmpty() ? lst.get(0) : null;
        }
        // TODO map values

        throw new ValueException(String.format("无效的值[%s:%s]", this, value));
    }

    @Override
    public Object convertToExport(Object value, Records rec) {
        if (value instanceof Records) {
            Records r = (Records) value;
            if (r.any()) {
                return r.get("present");
            }
        }
        return "";
    }

    @Override
    public Object convertToDisplayName(Object value, Records rec) {
        if (value instanceof Records) {
            Records r = (Records) value;
            if (r.any()) {
                return r.get("present");
            }
        }
        return "";
    }

    @Override
    public Records write(Records records, Object value) {
        Cache cache = records.getEnv().getCache();
        Object cacheValue = convertToCache(value, records, true);
        records = cache.getRecordsDifferentFrom(records, this, cacheValue);
        if (!records.any()) {
            return records;
        }
        Object[] values = new Object[records.size()];
        Arrays.fill(values, cacheValue);
        cache.update(records, this, Arrays.asList(values));

        // update toupdate
        if (isStore()) {
            IdValues toupdate = records.getEnv().getToUpdate().get(records.getMeta().getName());
            for (String id : records.getIds()) {
                // cache_value is already in database format
                toupdate.get(id).put(getName(), cacheValue);
            }
        }

        // update the cache of one2many fields of new corecord

        return records;
    }
}
