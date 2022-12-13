package org.jdoo.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
import org.jdoo.Records;
import org.jdoo.Search;
import org.jdoo.data.ColumnType;
import org.jdoo.data.Cursor;
import org.jdoo.data.DbColumn;
import org.jdoo.data.SqlDialect;
import org.jdoo.exceptions.MissingException;
import org.jdoo.exceptions.ModelException;
import org.jdoo.exceptions.TypeException;
import org.jdoo.fields.RelationalField;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected Logger logger = LoggerFactory.getLogger(MetaField.class);

    static int globalSeq = 0;
    boolean setupDone;

    String name;
    protected String type;
    @Related
    protected String label;
    @Related
    String help;
    protected boolean readonly;
    boolean store = true;
    boolean required;

    @JsonIgnore
    int sequence;
    @JsonIgnore
    protected HashMap<String, Object> args = new HashMap<>();
    @JsonIgnore
    protected ColumnType columnType = ColumnType.None;
    @JsonIgnore
    protected boolean prefetch = true;
    @JsonIgnore
    MetaField relatedField;
    @JsonIgnore
    boolean automatic;
    @JsonIgnore
    boolean index;
    @JsonIgnore
    boolean copy = true;
    @JsonIgnore
    boolean manual;
    @JsonIgnore
    String modelName;
    @JsonIgnore
    Callable compute;
    @JsonIgnore
    Search search;
    @JsonIgnore
    Default defaultValue;
    @JsonIgnore
    Collection<String> depends;
    @JsonIgnore
    String[] related;
    @JsonIgnore
    boolean auth;
    @JsonIgnore
    String module;
    @JsonIgnore
    boolean inherited;
    @JsonIgnore
    MetaField inheritedField;

    /**
     * 获取定义字段的模块
     * 
     * @return
     */
    public String getModule() {
        return module;
    }

    /**
     * 设置字段的模块
     * 
     * @param module
     * @return
     */
    public MetaField setModule(String module) {
        this.module = module;
        return this;
    }

    /**
     * 构造实例
     */
    public MetaField() {
        sequence = globalSeq++;
    }

    /**
     * 根据当前类型创建新实例
     * 
     * @return
     */
    public MetaField newInstance() {
        try {
            MetaField field = getClass().getConstructor().newInstance();
            field.args = new HashMap<>(args);
            field.module = module;
            return field;
        } catch (Exception e) {
            throw new TypeException("创建MetaField失败:" + getClass().getName());
        }
    }

    /**
     * 获取默认值
     * 
     * @param rec
     * @return
     */
    public Object getDefault(Records rec) {
        if (defaultValue != null) {
            return defaultValue.call(rec);
        }
        return null;
    }

    /**
     * 是否可以排序
     * 
     * @return
     */
    public boolean isSortable() {
        return (columnType != ColumnType.None && store) || (inherited && relatedField.isSortable());
    }

    /**
     * 是否需要授权
     */
    public boolean isAuth() {
        return auth;
    }

    /**
     * 是否自动注入的字段（如create_uid,create_date,update_uid,update_date）
     * 
     * @return
     */
    @JsonIgnore
    public boolean isAuto() {
        return automatic;
    }

    /**
     * 设置参数
     * 
     * @param attrs
     */
    public void setArgs(Map<String, Object> attrs) {
        args.putAll(attrs);
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    /**
     * 设置字段名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取字段名
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 是否自定义的字段
     * 
     * @return
     */
    public boolean isManual() {
        return manual;
    }

    /**
     * 获取关联字段，如 user_id.company_id.name 返回 ["user_id", "company_id", "name"]
     * 
     * @return
     */
    public String[] getRelated() {
        if (related == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return related;
    }

    /**
     * 相应的关联字段，如 user_id.company_id.name 返回 res.company 的 name 字段
     * 
     * @return
     */
    public MetaField getRelatedField() {
        return relatedField;
    }

    /**
     * 如果是委托继承的字段，返回被继承的字段，否则返回当前字段
     * 
     * @return
     */
    @JsonIgnore
    public MetaField getBaseField() {
        return inherited ? inheritedField.getBaseField() : this;
    }

    /**
     * 获取模型名称
     * 
     * @return
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * 获取用户可见的字段标题，如果没设置，返回字段的名称
     * 
     * @return
     */
    public String getLabel() {
        if (StringUtils.isEmpty(label)) {
            return name;
        }
        return label;
    }

    /**
     * 是否保存到数据库，默认是 true，计算字段是 false
     * 
     * @return
     */
    public boolean isStore() {
        return store;
    }

    /**
     * 是否继承
     * 
     * @return
     */
    public boolean isInherited() {
        return inherited;
    }

    /**
     * 用户可见的帮助信息
     * 
     * @return
     */
    public String getHelp() {
        return help;
    }

    /**
     * 是否只读
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * 字段的值是否必填 (默认: false)
     * 
     * @return
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * 获取字段的计算方法 compute(recs)
     */
    public Callable getCompute() {
        return compute;
    }

    /**
     * search(recs, operator, value) searches on self
     * 
     * @return
     */
    public Search getSearch() {
        return search;
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

    protected void setName(MetaModel model, String name) {
        setupAttrs(model, name);
    }

    protected void setup(MetaModel model) {
        if (!setupDone) {
            if (getRelated().length > 0) {
                setupRelated(model);
            } else {
                setupNonRelated(model);
            }
            setupDone = true;
        }
    }

    protected void setupNonRelated(MetaModel model) {

    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Related {
    }

    protected void setupRelated(MetaModel model) {
        String relModel = this.modelName;
        MetaField field = null;
        for (String fname : getRelated()) {
            field = model.getRegistry().get(relModel).findField(fname);
            if (field == null) {
                throw new ModelException(String.format("关联引用字段%s的字段%s不存在", this, fname));
            }
            if (!field.setupDone) {
                field.setup(model.getRegistry().get(relModel));
            }
            if (field instanceof RelationalField) {
                relModel = ((RelationalField<?>) field).getComodel();
            }
        }
        relatedField = field;
        // check type consistency
        if (field == null || !getType().equals(field.getType())) {
            throw new TypeException(String.format("关联字段[%s]的类型与[%s]不一致", this, field));
        }

        // TODO searchable

        if (depends == null) {
            depends = new ArrayList<>();
            depends.add(StringUtils.join(getRelated(), "."));
        }

        Class<?> clazz = getClass();
        for (Field f : getFields(clazz).values()) {
            if (f.isAnnotationPresent(Related.class)) {
                f.setAccessible(true);
                try {
                    f.set(this, f.get(field));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    Map<String, Field> getFields(Class<?> clazz) {
        Map<String, Field> result = new HashMap<>(16);
        Class<?> current = clazz;
        while (current != null) {
            for (Field f : current.getDeclaredFields()) {
                String name = f.getName();
                result.putIfAbsent(name, f);
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
        // this.args.put("_args", attrs);
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
     * 调用方法{@link BaseModel#read(Records, List)}时，从数据集格式转为读的格式
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
    public Object convertToPresent(Object value, Records rec) {
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
        if (column != null) {
            boolean hasNotNull = !column.getNullable();
            boolean needInitColumn = isRequired() && !hasNotNull;
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
        if (isInherited()) {
            String[] paths = getRelated();
            for (Records r : records) {
                Records rel = r;
                for (int i = 0; i < paths.length - 1; i++) {
                    rel = rel.getRel(paths[i]);
                }
                rel.set(relatedField.getName(), value);
            }
        } else {
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
                    throw new MissingException(String.format("记录 %s 不存在或者已被删除", record));
                }
                value = cache.get(record, this);
            } else {
                value = null;
            }
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
