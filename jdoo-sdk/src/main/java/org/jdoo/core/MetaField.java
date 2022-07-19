package org.jdoo.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.jdoo.Callable;
import org.jdoo.Default;
import org.jdoo.ICriteria;
import org.jdoo.Records;
import org.jdoo.data.ColumnType;
import org.jdoo.data.Cursor;
import org.jdoo.data.DbColumn;
import org.jdoo.data.SqlDialect;
import org.jdoo.exceptions.MissingException;
import org.jdoo.exceptions.TypeException;
import org.jdoo.fields.RelationalField;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jdoo.util.Cache;
import org.jdoo.util.Tuple;
import org.jdoo.util.ToUpdate.IdValues;

/**
 * The field descriptor contains the field definition, and manages accesses
 * and assignments of the corresponding field on records.
 * 
 * @author lrz
 */
public class MetaField {

    protected Logger logger = LogManager.getLogger(MetaField.class);

    /** 设置状态 */
    enum SetupState {
        /** 未设置 */
        None,
        /** 基础设置 */
        Base,
        /** 全部设置 */
        Full,
    }

    static int globalSeq = 0;
    int sequence;
    protected HashMap<String, Object> args = new HashMap<>();
    @JsonIgnore
    protected ColumnType columnType = ColumnType.None;
    protected String type;
    protected boolean prefetch = true;
    protected boolean store = true;
    protected String label;
    protected boolean readonly;

    String[] related;
    MetaField relatedField;
    String name;
    boolean automatic;
    boolean index;
    boolean copy;
    boolean manual;
    String modelName;
    String help;
    boolean required;
    @JsonIgnore
    Callable compute;
    Default defaultValue;
    ICriteria criteria;
    Collection<String> depends;
    Boolean auth;

    SetupState setupState = SetupState.None;

    public MetaField() {
        sequence = globalSeq++;
    }

    public MetaField newInstance() {
        try {
            MetaField field = getClass().getConstructor().newInstance();
            field.args = new HashMap<>(args);
            return field;
        } catch (Exception e) {
            throw new TypeException("创建MetaField失败:" + getClass().getName());
        }
    }

    public Object getDefault(Records rec) {
        if (defaultValue != null) {
            return defaultValue.call(rec);
        }
        return null;
    }

    public boolean isAuto() {
        return automatic;
    }

    public void setArgs(Map<String, Object> attrs) {
        args.putAll(attrs);
    }

    /**
     * set name of the field
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * return name of the field
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    public boolean getManual() {
        return manual;
    }

    public String[] getRelated() {
        if (related == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return related;
    }

    /**
     * return the label of the field seen by users; if not
     * set, the ORM takes the field name in the class (capitalized).
     * 
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * return whether the field is stored in database
     * (default:true, false for computed fields)
     * 
     * @return
     */
    public boolean isStore() {
        return store;
    }

    /**
     * return the tooltip of the field seen by users
     * 
     * @return
     */
    public String getHelp() {
        return help;
    }

    /**
     * whether the field is readonly
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * return whether the value of the field is required (default: false)
     * 
     * @return
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * compute(recs) computes field on recs
     */
    public Callable getCompute() {
        return compute;
    }

    /**
     * return whether the field is indexed in database. Note: no effect
     * on non-stored and virtual fields. (default: ``False``)
     * 
     * @return
     */
    public boolean getIndex() {
        return index;
    }

    /**
     * return whether the field value should be copied when the record
     * is duplicated (default: true for normal fields, false for
     * one2many and computed fields, including property fields and
     * related fields)
     * 
     * @return
     */
    public boolean isCopy() {
        return copy;
    }

    /**
     * return type of the field
     * 
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * return column type that can be convert to database column type with
     * {@link SqlDialect}
     * 
     * @return
     */
    public ColumnType getColumnType() {
        return columnType;
    }

    /**
     * return database column type
     */
    public String getDbColumnType(SqlDialect sqlDialect) {
        return sqlDialect.getColumnType(getColumnType());
    }

    protected void setupBase(MetaModel model, String name) {
        if (setupState == SetupState.None) {
            setupAttrs(model, name);
            if (getRelated().length == 0) {
                setupRegularBase(model);
            }
            setupState = SetupState.Base;
        }
    }

    protected void setupFull(MetaModel model, Environment env) {
        if (setupState != SetupState.Full) {
            if (getRelated().length == 0) {
                setupRegularFull(model, env);
            } else {
                setupRelatedFull(model, env);
            }
            setupState = SetupState.Full;
        }
    }

    protected void setupRegularFull(MetaModel model, Environment env) {

    }

    protected List<String> relatedAttributes = new ArrayList<>(Arrays.asList("label", "help", "auth"));

    protected void setupRelatedFull(MetaModel model, Environment env) {
        String modelName = this.modelName;
        MetaField field = null;
        for (String name : getRelated()) {
            field = model.getRegistry().get(modelName).getField(name);
            if (field.setupState != SetupState.Full) {
                field.setupFull(model.getRegistry().get(modelName), env);
            }
            if (field instanceof RelationalField) {
                modelName = ((RelationalField<?>) field).getComodel();
            }
        }
        relatedField = field;

        if (!getType().equals(field.getType())) {
            throw new TypeException(String.format("关联字段[%s]的类型与[%s]不一致", this, field));
        }

        if (depends == null) {
            depends = new ArrayList<>();
            depends.add(StringUtils.join(getRelated(), "."));
        }

        Class<?> clazz = getClass();
        Map<String, Field> fields = getFields(clazz);
        for (String attr : relatedAttributes) {
            try {
                Field f = fields.get(attr);
                if (f.get(this) == null) {
                    f.set(this, f.get(field));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void setupAttrs(MetaModel model, String name) {
        Map<String, Object> attrs = getAttrs(model, name);
        Map<String, Field> fields = getFields(getClass());
        for (String key : attrs.keySet()) {
            try {
                Field field = fields.get(key);
                field.setAccessible(true);
                field.set(this, attrs.get(key));
            } catch (Exception e) {
                logger.warn("Field {}.{} unknown parameter {}", model.getName(), getName(), key);
            }
        }
    }

    protected void setupRegularBase(MetaModel model) {

    }

    Map<String, Field> getFields(Class<?> clazz) {
        Map<String, Field> result = new HashMap<>(16);
        Class<?> current = clazz;
        while (current != null) {
            for (Field f : current.getDeclaredFields()) {
                String name = f.getName();
                result.put(name, f);
            }
            current = current.getSuperclass();
        }
        return result;
    }

    protected Map<String, Object> getAttrs(MetaModel model, String name) {
        Boolean automatic = (Boolean) args.getOrDefault(Constants.AUTOMATIC, false);
        Boolean manual = (Boolean) args.getOrDefault(Constants.MANUAL, false);
        Map<String, Object> attrs = new HashMap<>(16);
        if (!automatic && !manual) {
            for (MetaField field : resolveMro(model, name)) {
                attrs.putAll(field.args);
            }
        }
        attrs.putAll(this.args);
        attrs.put(Constants.ARGS, this.args);
        attrs.put(Constants.MODEL_NAME, model.getName());
        attrs.put(Constants.NAME, name);

        if (attrs.get(Constants.COMPUTE) instanceof Callable) {
            // 计算字段不保存
            attrs.put(Constants.STORE, false);
        }

        if (Constants.STATE.equals(name)) {
            // by default, `state` fields should be reset on copy
            attrs.put(Constants.COPY, attrs.getOrDefault(Constants.COPY, false));
        }
        if (attrs.containsKey(Constants.RELATED)) {
            attrs.put(Constants.STORE, false);
            attrs.put(Constants.COPY, attrs.getOrDefault(Constants.COPY, false));
            attrs.put(Constants.READONLY, attrs.getOrDefault(Constants.READONLY, true));
        }
        return attrs;
    }

    protected List<MetaField> resolveMro(MetaModel model, String name) {
        List<MetaField> result = new ArrayList<>();
        for (MetaModel cls : model.getMro()) {
            if (cls.registry == null) {
                MetaField field = cls.findField(name);
                if (field != null) {
                    result.add(field);
                }
            }
        }
        return result;
    }

    /**
     * Convert value from the write format to the SQL format.
     * 
     * @param value
     * @param record
     * @param validate 默认true
     * @return
     */
    public Object convertToColumn(Object value, Records record, boolean validate) {
        return value;
    }

    /**
     * Convert value to the cache format; value may come from an
     * assignment, or have the format of methods
     * {@link BaseModel#read(Records, List)} or
     * {@link BaseModel#update(Records, Map)}. If the value represents a recordset,
     * it should be added for prefetching on record
     * 
     * @param value
     * @param rec
     * @param validate when True, field-specific validation of value will be
     *                 performed
     * @return
     */
    public Object convertToCache(Object value, Records rec, boolean validate) {
        return value;
    }

    /**
     * Convert value from the cache format to the record format.
     * If the value represents a recordset, it should share the prefetching of
     * record.
     * 
     * @param value
     * @param rec
     * @return
     */
    public Object convertToRecord(Object value, Records rec) {
        return value;
    }

    /**
     * Convert ``value`` from the record format to the format returned by
     * method {@link BaseModel#read(Records, List)}.
     * 
     * @param value
     * @param rec
     * @param usePresent when True, the value's display name will be computed
     *                   using {@link BaseModel#getPresent(Records, String)},
     *                   if relevant for the field
     * @return
     */
    public Object convertToRead(Object value, Records rec, boolean usePresent) {
        return value;
    }

    /**
     * Convert value from any format to the format of method
     * {@link BaseModel#update(Records, Map)}.
     */
    public Object convertToWrite(Object value, Records rec) {
        Object cacheValue = convertToCache(value, rec, true);
        Object recordValue = convertToRecord(cacheValue, rec);
        return convertToRead(recordValue, rec, true);
    }

    /**
     * Convert value from the record format to the export format.
     * 
     * @param value
     * @param rec
     * @return
     */
    public Object convertToExport(Object value, Records rec) {
        return value == null ? "" : value;
    }

    /**
     * Convert value from the record format to a suitable display name.
     * 
     * @param value
     * @param rec
     * @return
     */
    public Object convertToDisplayName(Object value, Records rec) {
        return value == null ? "" : value.toString();
    }

    // update database schema

    /**
     * Update the database schema to implement this field
     * 
     * @param model
     * @param columns
     */
    protected void updateDb(Records model, Map<String, DbColumn> columns) {
        if (getColumnType() == ColumnType.None) {
            return;
        }
        DbColumn column = columns.get(name);
        updateDbColumn(model, column);
        updateDbNotNull(model, column);

    }

    protected void updateDbNotNull(Records model, DbColumn column) {
        boolean hasNotNull = column != null && !column.getNullable();
        boolean needInitColumn = column == null || isRequired() && !hasNotNull;
        if (needInitColumn) {
            // TODO init if table has rows
        }

        Cursor cr = model.getEnv().getCursor();
        SqlDialect sd = cr.getSqlDialect();
        if (isRequired() && !hasNotNull) {
            sd.setNotNull(cr, model.getMeta().getTable(), getName(), getDbColumnType(sd));
        } else if (!isRequired() && hasNotNull) {
            sd.dropNotNull(cr, model.getMeta().getTable(), getName(), getDbColumnType(sd));
        }
    }

    /**
     * Create/update the column corresponding to this field
     * 
     * @param model
     * @param column
     */
    protected void updateDbColumn(Records model, DbColumn column) {
        Cursor cr = model.getEnv().getCursor();
        SqlDialect sd = cr.getSqlDialect();
        if (column == null) {
            sd.createColumn(cr, model.getMeta().getTable(), name, getDbColumnType(sd), label, isRequired());
            return;
        }

        // TODO rename or recreate column
    }

    // ############################################################################
    // #
    // # Alternatively stored fields: if fields don't have a {@link ColumnType}}
    // (not stored as regular db columns) they go through a read/create/write
    // # protocol instead
    // #

    /**
     * Read the value of this field on records, and store it in cache.
     */
    public void read(Records records) {
        throw new UnsupportedOperationException(String.format("Method read() undefined on %s", this));
    }

    /**
     * Write the value of this field on the given records, which have just
     * been created.
     */
    public void create(List<Tuple<Records, Object>> recordValues) {

    }

    /**
     * Write the value of this field on records. This method must update
     * the cache and prepare database updates.
     * 
     * @param records
     * @param value
     * @return
     */
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
            Records record = records.browse(records.getIds()[0]);
            Object writeValue = convertToWrite(cacheValue, record);
            Object columnValue = convertToColumn(writeValue, record, true);
            for (String id : records.getIds()) {
                toupdate.get(id).put(getName(), columnValue);
            }
        }

        return records;
    }

    /**
     * return the value of field this on record
     */
    public Object get(Records record) {
        if (!record.any()) {
            Object value = convertToCache(null, record, false);
            return convertToRecord(value, record);
        }
        record.ensureOne();
        if (getCompute() != null) {
            Object value = getCompute().call(record);
            return convertToRecord(value, record);
        }
        if (getRelated().length > 0) {
            return computeRelated(record);
        }
        Environment env = record.getEnv();
        Cache cache = env.getCache();
        Object value = cache.get(record, this, Void.class);
        if (value == Void.class) {
            if (isStore() && StringUtils.isNotEmpty(record.getId())) {
                Records rec = inCacheWithout(record, this, 0);
                rec.call("fetchField", this);
                if (!cache.contains(record, this) && !record.exists().any()) {
                    throw new MissingException("记录不存在或者已被删除");
                }
                value = cache.get(record, this);
            }
            // many2one
        }
        return convertToRecord(value, record);
    }

    Object computeRelated(Records record) {
        Records rec = record;
        Object value = null;
        for (String name : getRelated()) {
            if (rec.size() == 1) {
                value = rec.get(name);
            } else {
                for (Records r : rec) {
                    value = r.get(name);
                    break;
                }
            }
            if (value instanceof Records) {
                rec = (Records) value;
            }
        }
        return value;
    }

    Records inCacheWithout(Records rec, MetaField field, Integer limit) {
        if (limit == null) {
            limit = 1000;
        }

        Collection<String> ids = expandIds(rec.getId(), rec.getPrefetchIds());
        ids = rec.getEnv().getCache().getMissingIds(rec.browse(ids), field);
        if (limit > 0 && ids.size() > limit) {
            ids = ids.stream().limit(limit).collect(Collectors.toList());
        }
        return rec.browse(ids);
    }

    Collection<String> expandIds(String id, Supplier<String[]> prefetchIds) {
        Set<String> ids = new HashSet<>();
        ids.add(id);
        if (prefetchIds != null) {
            for (String pid : prefetchIds.get()) {
                ids.add(pid);
            }
        }
        return ids;
    }

    /**
     * set the value of field this on records
     * 
     * @param records
     * @param value
     */
    public void set(Records records, Object value) {
        Object writeValue = convertToWrite(value, records);
        records.update(new HashMap<String, Object>(1) {
            {
                put(getName(), writeValue);
            }
        });
    }

    @Override
    public String toString() {
        return String.format("MetaField(%s.%s) - %s", modelName, name, type);
    }
}
