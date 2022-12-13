package org.jdoo.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jdoo.Model;
import org.jdoo.BoolState;
import org.jdoo.Doc;
import org.jdoo.Criteria;
import org.jdoo.DeleteMode;
import org.jdoo.Records;
import org.jdoo.Model.ServiceMethod;
import org.jdoo.data.AsSql;
import org.jdoo.data.ColumnType;
import org.jdoo.data.Cursor;
import org.jdoo.data.Expression;
import org.jdoo.data.Query;
import org.jdoo.data.Query.SelectClause;
import org.jdoo.exceptions.AccessException;
import org.jdoo.exceptions.ModelException;
import org.jdoo.exceptions.SqlConstraintException;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.exceptions.ValueException;
import org.jdoo.fields.Many2manyField;
import org.jdoo.fields.Many2oneField;
import org.jdoo.fields.RelationalField;
import org.jdoo.fields.RelationalMultiField;
import org.jdoo.fields.StringField;
import org.jdoo.services.*;
import org.jdoo.utils.ArrayUtils;
import org.jdoo.utils.IdWorker;
import org.jdoo.utils.ObjectUtils;
import org.jdoo.utils.StringUtils;
import org.jdoo.utils.Utils;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.lang.Nullable;

import org.jdoo.util.Cache;
import org.jdoo.util.KvMap;
import org.jdoo.util.ServerDate;
import org.jdoo.util.ToUpdate;
import org.jdoo.util.Tuple;
import org.jdoo.util.ToUpdate.IdValues;

/***
 * 模型基类，提供基础模型方法
 *
 * @author lrz
 */

@Model.Service(name = "read", label = "查看", description = "读取记录集指定字段的值", type = ReadService.class)
@Model.Service(name = "create", label = "创建", description = "为模型创建新记录", type = CreateService.class)
@Model.Service(name = "createBatch", label = "批量新建", auth = "create", description = "批量创建新记录", type = BatchCreateService.class)
@Model.Service(name = "find", label = "查找", auth = "read", description = "根据参数搜索记录", type = FindService.class)
@Model.Service(name = "search", label = "查询", auth = "read", description = "搜索并读取记录集指定字段的值", type = SearchService.class)
@Model.Service(name = "count", label = "计数", auth = "read", description = "统计匹配条件的记录数", type = CountService.class)
@Model.Service(name = "delete", label = "删除", description = "删除当前集合的记录", type = DeleteService.class)
@Model.Service(name = "update", label = "编辑", description = "使用提供的值更新当前集中的所有记录", type = UpdateService.class)
public class BaseModel {
    private static Logger logger = LoggerFactory.getLogger(BaseModel.class);
    final static List<String> LOG_ACCESS_COLUMNS = Arrays.asList(Constants.CREATE_DATE, Constants.CREATE_UID,
            Constants.UPDATE_DATE, Constants.UPDATE_UID);
    private Records recordSet;
    private MetaModel metaModel;

    /**
     * 是否自动创建数据库表，如果设置false，可以重写{@link BaseModel#init(Records)}方法手动创建数据库表。
     * {@link Model}默认是true, {@link AbstractModel}默认是false。
     */
    protected boolean isAuto;
    protected boolean isAbstract = true;
    protected boolean isTransient;
    protected boolean custom;

    // #region get/set

    MetaModel getMeta() {
        return metaModel;
    }

    void setMeta(MetaModel meta) {
        this.metaModel = meta;
    }

    /**
     * 设置记录集，模型绑定记录集后可通过模型的getter/setter方法操作记录集的数据
     *
     * @param rec 记录集
     */
    public void setRecords(Records rec) {
        this.recordSet = rec;
    }

    /**
     * 获取记录集
     *
     * @return 记录集
     * @exception ModelException 未设置记录集
     */
    public Records getRecords() {
        if (recordSet == null) {
            throw new ModelException(
                    String.format("class[%s],模型[%s]未设置记录集", getClass().getName(), metaModel.getName()));
        }
        return recordSet;
    }

    /**
     * 获取字段值
     *
     * @param field 字段名
     * @return 字段值
     */
    public Object get(String field) {
        Records rec = getRecords();
        MetaField f = rec.getMeta().getField(field);
        return f.get(rec);
    }

    /**
     * 获取字段值
     *
     * @param field
     * @return
     */
    public Object get(MetaField field) {
        // 不能直接使用field，因为field在setup后已经不是原来的field，需要根据名称重新从元数据中获取field
        return get(field.getName());
    }

    /**
     * 设置字段值
     *
     * @param field 字段名
     * @param value 要设置的值
     */
    public void set(String field, Object value) {
        Records rec = getRecords();
        MetaField f = rec.getMeta().getField(field);
        f.set(rec, value);
    }

    /**
     * 设置字段值
     *
     * @param field
     * @param value
     */
    public void set(MetaField field, Object value) {
        // 不能直接使用field，因为field在setup后已经不是原来的field，需要根据名称重新从元数据中获取field
        set(field.getName(), value);
    }

    /**
     * 模型可以使用查询视图做映射，重写些方法，提供视图sql
     *
     * @param rec
     * @return
     */
    public String getTableQuery(Records rec) {
        return null;
    }

    // #endregion

    /**
     * This method may be overridden to create or modify a model's database schema.
     *
     * @param rec
     */
    public void init(Records rec) {

    }

    // #region create

    /**
     * 创建模型的新记录，根据提供的字段值字典创建数据库一条记录，并返回新记录的ID
     *
     * @param rec    记录集
     * @param values 新值
     * @return 新记录
     */
    public Records create(Records rec, Map<String, Object> values) {
        return createBatch(rec, Arrays.asList(values));
    }

    /**
     * 批量创建模型的记录，根据提供的字段值字典列表，批量创建数据库记录
     *
     * @param rec        记录集
     * @param valuesList 新值列表
     * @return 新记录
     */
    public Records createBatch(Records rec, List<Map<String, Object>> valuesList) {
        Set<String> badNames = new HashSet<>();
        if (rec.getMeta().getRegistry().loaded) {
            badNames.add(Constants.ID);
            badNames.add("parent_path");
            if (rec.getMeta().isLogAccess()) {
                badNames.addAll(LOG_ACCESS_COLUMNS);
            }
        }
        List<NewData> dataList = new ArrayList<>();
        for (Map<String, Object> values : valuesList) {
            values = addMissingDefaultValues(rec, values);

            for (String badName : badNames) {
                values.remove(badName);
            }
            // set magic fields
            if (rec.getMeta().isLogAccess()) {
                values.putIfAbsent(Constants.CREATE_UID, rec.getEnv().getUserId());
                values.putIfAbsent(Constants.CREATE_DATE, new ServerDate());
                values.putIfAbsent(Constants.UPDATE_UID, rec.getEnv().getUserId());
                values.putIfAbsent(Constants.UPDATE_DATE, new ServerDate());
            }

            // distribute fields into sets for various purposes
            NewData data = new NewData();
            data.stored = new HashMap<>(values.size());
            data.inherited = new HashMap<>();
            for (String key : values.keySet()) {
                Object val = values.get(key);
                MetaField field = rec.getMeta().findField(key);
                if (field == null) {
                    logger.warn("{}.create()遇到未知字段{}", rec.getMeta().getName(), key);
                    continue;
                }

                if (field.isStore()) {
                    data.stored.put(key, val);
                }
                if (field.isInherited()) {
                    Map<String, Object> m = data.inherited.get(field.relatedField.modelName);
                    if (m == null) {
                        m = new HashMap<>();
                        data.inherited.put(field.relatedField.modelName, m);
                    }
                    m.put(key, val);
                }
            }
            dataList.add(data);
        }
        Environment env = rec.getEnv();
        // create or update parent records
        for (Entry<String, String> e : rec.getMeta().delegates.entrySet()) {
            String modelName = e.getKey();
            String parentName = e.getValue();
            List<NewData> parentDataList = new ArrayList<>();
            for (NewData data : dataList) {
                if (!data.stored.containsKey(parentName)) {
                    parentDataList.add(data);
                } else if (data.inherited.containsKey(modelName)) {
                    Records parent = env.get(modelName).browse((String) data.stored.get(parentName));
                    parent.update(data.inherited.get(modelName));
                }
            }
            if (parentDataList.size() > 0) {
                List<Map<String, Object>> toCreate = new ArrayList<>();
                for (NewData d : parentDataList) {
                    toCreate.add(d.inherited.get(modelName));
                }
                Records parents = env.get(modelName).createBatch(toCreate);
                String[] ids = parents.getIds();
                for (int i = 0; i < parentDataList.size(); i++) {
                    parentDataList.get(i).stored.put(parentName, ids[i]);
                }
            }
        }
        // create records with stored fields
        Records records = doCreate(rec, dataList);

        // TODO validateConstraints
        return records;
    }

    /**
     * 获取字段默认值
     * 
     * @param rec
     * @param fields 字段列表
     * @return
     */
    @ServiceMethod(auth = "read", label = "获取默认值", doc = "获取属性默认值")
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDefaultValue(Records rec, Collection<String> fields) {
        KvMap defaults = new KvMap();
        MetaModel model = rec.getMeta();
        Map<String, List<String>> parentFields = new HashMap<>();
        for (String name : fields) {
            // TODO look up context
            // TODO look up ir.default
            MetaField field = model.getField(name);
            // look up field.default
            if (field.defaultValue != null) {
                Object value = field.getDefault(rec);
                value = field.convertToCache(value, rec, false);
                value = field.convertToWrite(value, rec);
                defaults.put(name, value);
            }
            // delegate to parent model
            if (field.isInherited()) {
                field = field.relatedField;
                List<String> list = parentFields.get(field.getModelName());
                if (list == null) {
                    list = new ArrayList<>();
                    parentFields.put(field.getModelName(), list);
                }
                list.add(field.getName());
            }
        }
        for (Entry<String, List<String>> entry : parentFields.entrySet()) {
            defaults.putAll(
                    (Map<String, Object>) rec.getEnv().get(entry.getKey()).call("getDefaultValue", entry.getValue()));
        }
        return defaults;
    }

    class NewData {
        public Map<String, Object> stored;
        public Map<String, Map<String, Object>> inherited;
        public Records records;
    }

    /**
     * 验证参数值（必填、唯一）
     * 
     * @param rec
     * @param values 值
     * @param create 是否创建中
     */
    public void validateValues(Records rec, Map<String, Object> values, boolean create) {
        validateRequried(rec, values, create);
        validateUnique(rec, values, create);
    }

    /**
     * 验证必填, 当字段使用{@link org.jdoo.Field#isRequired()}声明为必需时，验证提供的值为非空值(字符串非empty)
     * 
     * @param rec
     * @param values
     * @param create
     */
    public void validateRequried(Records rec, Map<String, Object> values, boolean create) {
        List<String> errors = new ArrayList<>();
        Object defaultValue = create ? null : true;
        for (MetaField f : rec.getMeta().getFields().values()) {
            if (f.isStore() && f.isRequired()) {
                Object value = values.getOrDefault(f.getName(), defaultValue);
                if (value == null || (value instanceof String && StringUtils.isEmpty((String) value))) {
                    errors.add(rec.l10n(f.getLabel()));
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(rec.l10n("%s 不能为空", StringUtils.join(errors)));
        }
    }

    /**
     * 验证使用{@link UniqueConstraint}声明的唯一约束,
     * 判断条件后执行{@link BaseModel#validateUniqueConstraint}
     * 
     * @param records
     * @param values
     * @param create
     */
    public void validateUnique(Records records, Map<String, Object> values, boolean create) {
        for (UniqueConstraint constraint : records.getMeta().getUniques()) {
            boolean match = false;
            for (String field : constraint.getFields()) {
                if (values.containsKey(field)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                if (create) {
                    validateUniqueConstraint(records, constraint, values, create);
                } else {
                    for (Records rec : records) {
                        validateUniqueConstraint(rec, constraint, values, create);
                    }
                }
            }
        }
    }

    /**
     * 验证使用{@link UniqueConstraint}声明的唯一约束
     * 
     * @param records
     * @param constraint
     * @param values
     * @param create
     */
    public void validateUniqueConstraint(Records records, UniqueConstraint constraint, Map<String, Object> values,
            boolean create) {
        Criteria criteria = new Criteria();
        for (String field : constraint.getFields()) {
            Object value;
            if (!create && !values.containsKey(field)) {
                value = records.get(field);
                if (value instanceof Records) {
                    MetaField f = records.getMeta().getField(field);
                    if (f instanceof Many2oneField) {
                        value = ((Records) value).getId();
                    }
                }
            } else {
                value = values.get(field);
            }
            if (value != null && (!(value instanceof String) || StringUtils.isNotEmpty((String) value))) {
                criteria.and(Criteria.equal(field, value));
            }
        }
        if (criteria.size() > 0) {
            if (!create) {
                criteria.and(Criteria.binary("id", "!=", records.getId()));
            }
            boolean exists = records.count(criteria) > 0;
            if (exists) {
                String message = constraint.getMessage();
                if (StringUtils.isNotEmpty(message)) {
                    throw new ValidationException(records.l10n(message));
                }
                List<String> errors = new ArrayList<>();
                for (String field : constraint.getFields()) {
                    errors.add(records.l10n(records.getMeta().getField(field).getLabel()));
                }
                throw new ValidationException(records.l10n("%s 必须唯一", StringUtils.join(errors)));
            }
        }
    }

    /**
     * 验证使用{@link MethodConstrains}声明的约束
     * 
     * @param rec
     * @param fields
     */
    public void validateConstraints(Records rec, Collection<String> fields) {
        for (MethodConstrains constrains : rec.getMeta().getConstrains()) {
            String[] fnames = constrains.getFields();
            boolean match = fnames.length == 0;
            for (String field : fnames) {
                if (fields.contains(field)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                rec.call(constrains.getMethod());
            }
        }
    }

    Records doCreate(Records rec, List<NewData> dataList) {
        Cursor cr = rec.getEnv().getCursor();
        List<String> newIds = new ArrayList<>();
        List<MetaField> otherFields = new ArrayList<>();
        Set<MetaField> translatedFields = new HashSet<>();
        for (NewData data : dataList) {
            Map<String, Object> stored = data.stored;
            validateValues(rec, stored, true);
            List<String> columns = new ArrayList<>();
            List<String> formats = new ArrayList<>();
            List<Object> values = new ArrayList<>();
            if (!stored.containsKey(Constants.ID)) {
                String id = IdWorker.nextId();
                columns.add(cr.quote(Constants.ID));
                formats.add("%s");
                values.add(id);
                newIds.add(id);
            } else {
                newIds.add((String) stored.get(Constants.ID));
            }

            for (String name : stored.keySet().stream().sorted().toArray(String[]::new)) {
                MetaField field = rec.getMeta().getField(name);
                if (field.getColumnType() != ColumnType.None) {
                    Object val = stored.get(name);
                    if (val instanceof AsSql) {
                        columns.add(cr.quote(name));
                        formats.add(((AsSql) val).getValue());
                    } else if (val instanceof ServerDate) {
                        columns.add(cr.quote(name));
                        formats.add(cr.getSqlDialect().getNowUtc());
                    } else {
                        Object colVal = field.convertToColumn(val, rec, true);
                        columns.add(cr.quote(name));
                        formats.add("%s");
                        values.add(colVal);
                        if (field instanceof StringField && ((StringField<?>) field).isTranslate()) {
                            translatedFields.add(field);
                        }
                    }
                } else {
                    otherFields.add(field);
                }
            }
            String sql = String.format("INSERT INTO %s(%s) VALUES (%s)", cr.quote(rec.getMeta().getTable()),
                    StringUtils.join(columns), StringUtils.join(formats));
            cr.execute(sql, values);
        }

        // put the new records in cache, and update inverse fields, for many2one
        // cachetoclear is an optimization to avoid modified()'s cost until other_fields
        // are processed
        Records records = putNewInCache(rec, newIds, otherFields, dataList);
        Set<String> fields = new HashSet<>();
        for (NewData data : dataList) {
            fields.addAll(data.stored.keySet());
        }
        validateConstraints(records, fields);
        checkAccessRule(records, "create");

        // TODO add translations
        // if(rec.getEnv().getLang() != "zh_CN"){

        // }
        return records;
    }

    static Records putNewInCache(Records rec, List<String> newIds, List<MetaField> otherFields,
            List<NewData> dataList) {
        Records records = rec.browse(newIds.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        Cache cache = rec.getEnv().getCache();
        List<Tuple<Records, MetaField>> cachetoclear = new ArrayList<>();
        for (List<?> datas : ArrayUtils.zip(dataList, records)) {
            NewData data = (NewData) datas.get(0);
            Records record = (Records) datas.get(1);
            data.records = record;
            Map<String, Object> stored = data.stored;
            for (MetaField field : rec.getMeta().getFields().values()) {
                if ("one2many".equals(field.type) || "many2many".equals(field.type)) {
                    cache.set(record, field, Collections.emptyList());
                }
            }
            for (Entry<String, Object> entry : stored.entrySet()) {
                String fname = entry.getKey();
                Object value = entry.getValue();
                MetaField field = rec.getMeta().getField(fname);
                if (value instanceof AsSql || "one2many".equals(field.type) || "many2many".equals(field.type)) {
                    cachetoclear.add(new Tuple<>(record, field));
                } else {
                    Object cacheValue = field.convertToCache(value, rec, true);
                    cache.set(record, field, cacheValue);
                }
            }
        }

        if (!otherFields.isEmpty()) {
            Records others = records.withContext(Utils.clearContext(rec.getEnv().getContext()));
            // TODO sort by sequence
            for (MetaField field : otherFields) {
                List<Tuple<Records, Object>> recordValues = new ArrayList<>();
                for (List<Object> x : ArrayUtils.zip(others, dataList)) {
                    NewData data = (NewData) x.get(1);
                    Map<String, Object> stored = data.stored;
                    if (stored.containsKey(field.getName())) {
                        Records other = (Records) x.get(0);
                        Object value = stored.get(field.getName());
                        recordValues.add(new Tuple<>(other, value));
                    }
                }
                field.create(recordValues);
            }
        }
        for (Tuple<Records, MetaField> tuple : cachetoclear) {
            if (cache.contains(tuple.getItem1(), tuple.getItem2())) {
                cache.remove(tuple.getItem1(), tuple.getItem2());
            }
        }
        return records;
    }

    void collectModelsToAvoid(Set<String> avoidModels, Records model, Map<String, Object> vals) {
        for (Entry<String, String> entry : model.getMeta().delegates.entrySet()) {
            if (vals.containsKey(entry.getValue())) {
                avoidModels.add(entry.getKey());
            } else {
                collectModelsToAvoid(avoidModels, model.getEnv().get(entry.getKey()), vals);
            }
        }
    }

    public Map<String, Object> addMissingDefaultValues(Records rec, Map<String, Object> vals) {
        // avoid overriding inherited values when parent is set
        Set<String> avoidModels = new HashSet<>();
        collectModelsToAvoid(avoidModels, rec, vals);
        Set<String> missingDefaults = new HashSet<>();
        Predicate<MetaField> avoid = field -> {
            if (avoidModels.size() > 0) {
                while (field.inherited) {
                    field = field.relatedField;
                    if (avoidModels.contains(field.modelName)) {
                        return true;
                    }
                }
            }
            return false;
        };
        for (Entry<String, MetaField> entry : rec.getMeta().getFields().entrySet()) {
            if (!vals.containsKey(entry.getKey()) && !avoid.test(entry.getValue())) {
                missingDefaults.add(entry.getKey());
            }
        }
        Map<String, Object> defaults = getDefaultValue(rec, missingDefaults);
        defaults.putAll(vals);
        return defaults;
    }

    // #endregion

    /**
     * 读取记录集指定字段的数据
     *
     * @param rec    记录集
     * @param fields
     * @return 字段值列表
     */
    public List<Map<String, Object>> read(Records rec, Collection<String> fields) {
        Set<String> storedFields = new HashSet<>(fields.size());
        for (String name : fields) {
            MetaField field = rec.getMeta().findField(name);
            if (field == null) {
                throw new ValueException(String.format("模型[%s]的字段[%s]无效", rec.getMeta().getName(), name));
            }
            if (field.isStore()) {
                storedFields.add(name);
            }
        }
        doRead(rec, storedFields);
        List<Map<String, Object>> result = new ArrayList<>();
        boolean usePresent = (Boolean) rec.getEnv().getContext().getOrDefault("usePresent", false);
        for (Records r : rec) {
            Map<String, Object> vals = new HashMap<>(fields.size());
            vals.put(Constants.ID, r.getId());
            for (String fname : fields) {
                MetaField field = rec.getMeta().getField(fname);
                vals.put(fname, field.convertToRead(r.get(fname), rec, usePresent));
            }
            result.add(vals);
        }
        return result;
    }

    public Records exists(Records rec) {
        if (!rec.any()) {
            return rec;
        }
        Cursor cr = rec.getEnv().getCursor();
        String query = String.format("SELECT id FROM %s WHERE id IN %%s",
                cr.quote(rec.getMeta().getTable()));
        cr.execute(query, Arrays.asList(Arrays.asList(rec.getIds())));
        Set<String> set = cr.fetchAll().stream().map(row -> (String) row[0]).collect(Collectors.toSet());
        List<String> ids = new ArrayList<>();
        for (String id : rec.getIds()) {
            if (set.contains(id)) {
                ids.add(id);
            }
        }
        return rec.browse(ids);
    }

    public void fetchField(Records rec, MetaField field) {
        List<String> fnames = new ArrayList<>();
        if ((Boolean) rec.getEnv().getContext().getOrDefault(Constants.PREFETCH_FIELDS, true) && field.prefetch) {
            for (MetaField f : rec.getMeta().getFields().values()) {
                if (f.prefetch) {
                    fnames.add(f.getName());
                }
            }
        } else {
            fnames.add(field.getName());
        }
        doRead(rec, fnames);
    }

    /**
     * 查询关联的字段(many2one/one2many/many2many)的模型数据。
     * 调用关联字段的comodel的search方法进行查询。
     * options选项包含：
     * criteria:过滤条件;
     * fields:要查询的字段,默认查询present字段;
     * limit:记录限制数,默认10;
     * offset:记录开始位置;
     * order:排序;
     * nextTest:是否测试有没有下一页;
     * activeTest:是否只加载active=true的数据,模型没有active字段则忽略;
     * 
     * @param rec
     * @param relatedField 关联的字段
     * @param options      选项
     * @return
     */
    @SuppressWarnings("unchecked")
    @Model.ServiceMethod(auth = "read", label = "获取关联模型的数据", doc = "读取关联模型的记录")
    public Map<String, Object> searchRelated(Records rec, @Doc(doc = "关联的字段") String relatedField,
            @Doc(doc = "选项") Map<String, Object> options) {
        MetaField f = rec.getMeta().findField(relatedField);
        if (f == null) {
            throw new ModelException(String.format("模型[%s]不存在字段[%s]", rec.getMeta().getName(), relatedField));
        }
        if (!(f instanceof RelationalField)) {
            throw new ModelException(String.format("模型[%s]的字段[%s]不是关联字段", rec.getMeta().getName(), relatedField));
        }
        RelationalField<?> relational = (RelationalField<?>) f;
        Records comodel = rec.getEnv().get(relational.getComodel());
        Criteria criteria = relational.getCriteria(comodel);
        if (options.containsKey("criteria")) {
            criteria.and(Criteria.parse((List<Object>) options.get("criteria")));
        }
        Boolean activeTest = (Boolean) options.getOrDefault("activeTest", false);
        if (activeTest && comodel.getMeta().getFields().containsKey("active")) {
            criteria.and(Criteria.equal("active", true));
        }
        List<String> fields = (List<String>) options.get("fields");
        if (fields == null) {
            fields = Arrays.asList(Constants.PRESENT);
        }
        Integer limit = (Integer) options.get("limit");
        Boolean nextTest = (Boolean) options.getOrDefault("nextTest", false);
        if (limit == null) {
            limit = 10;
        }
        Integer offset = (Integer) options.get("offset");
        String order = (String) options.get("order");
        List<Map<String, Object>> data = comodel.search(fields, criteria, offset, nextTest ? limit + 1 : limit, order);

        Map<String, Object> result = new HashMap<>();
        boolean hasNext = nextTest && data.size() > limit;
        if (hasNext) {
            data.remove(data.size() - 1);
        }
        result.put("hasNext", hasNext);
        result.put("values", data);
        return result;
    }

    @Model.ServiceMethod(auth = "read", label = "获取展示值", doc = "读取记录的展示值")
    @Doc(doc = "id/名称的列表", value = "[[\"01ogtcwblyuio\", \"name\"]]")
    public List<Object[]> getPresent(Records rec) {
        List<Object[]> result = new ArrayList<>();
        MetaModel meta = rec.getMeta();
        String[] present = meta.getPresent();
        for (Records r : rec) {
            String display = "";
            if (present.length > 0) {
                String format = meta.getPresentFormat();
                if (StringUtils.isEmpty(format)) {
                    for (String fname : present) {
                        MetaField field = meta.getField(fname);
                        if (display.length() > 0) {
                            display += ",";
                        }
                        display += field.convertToPresent(r.get(field), r);
                    }
                } else {
                    display = format;
                    for (String fname : present) {
                        MetaField field = meta.getField(fname);
                        display = display.replaceAll("\\{" + fname + "\\}",
                                (String) field.convertToPresent(r.get(field), r));
                    }
                }
            } else {
                display = String.format("%s,%s", meta.getName(), r.getId());
            }
            result.add(new Object[] { r.getId(), display });
        }
        return result;
    }

    void doRead(Records rec, Collection<String> fields) {
        if (!rec.any()) {
            return;
        }
        // TODO flush fields only
        rec.flush();

        Set<MetaField> fieldsRead = new HashSet<>();
        Set<MetaField> fieldsPre = new HashSet<>();
        for (String name : fields) {
            if (Constants.ID.equals(name)) {
                continue;
            }
            MetaField field = rec.getMeta().getField(name);
            if (field.isStore()) {
                if (field.getColumnType() == ColumnType.None) {
                    fieldsRead.add(field);
                    // else if (!(field instanceof StringField) || !((StringField<?>)
                    // field).getTranslate()) {
                    // TODO 翻译处理
                } else {
                    fieldsPre.add(field);
                }
            }
        }
        List<Object[]> result = new ArrayList<>();
        if (!fieldsPre.isEmpty()) {
            Cursor cr = rec.getEnv().getCursor();
            Query query = new Query(cr, rec.getMeta().getTable(), getTableQuery(rec));
            // TODO _apply_ir_rules(rec, query, "read");
            List<String> qualNames = new ArrayList<>();
            qualNames.add(qualify(rec, rec.getMeta().getField(Constants.ID), query));
            for (MetaField field : fieldsPre) {
                qualNames.add(qualify(rec, field, query));
            }

            query.addWhere(String.format("%s.%s IN %%s", cr.quote(rec.getMeta().getTable()), cr.quote(Constants.ID)));
            SelectClause sql = query.select(qualNames);
            for (Object[] subIds : cr.splitForInConditions(rec.getIds())) {
                List<Object> params = new ArrayList<>(sql.getParams());
                params.add(Arrays.asList(subIds));
                cr.execute(sql.getQuery(), params);
                result.addAll(cr.fetchAll());
            }

        } else {
            checkAccessRule(rec, "read");
            for (String id : rec.getIds()) {
                result.add(new Object[] { id });
            }
        }

        Records fetched = rec.browse();
        if (!result.isEmpty()) {
            List<List<Object>> col = ArrayUtils.zip(result.toArray(new Object[0][0]));
            int next = 0;
            List<Object> ids = col.get(next++);
            fetched = rec.browse(ids.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            Cache cache = rec.getEnv().getCache();
            for (MetaField field : fieldsPre) {
                List<Object> values = col.get(next++);
                // TODO translate
                cache.update(fetched, field, values);
            }
            for (MetaField field : fieldsRead) {
                field.read(fetched);
            }
        }

        // TODO missing
    }

    static String qualify(Records rec, MetaField field, Query query) {
        String col = field.getName();
        String res = inheritsJoinCalc(rec, rec.getMeta().getTable(), field.getName(), query);
        if (Constants.BINARY.equals(field.type)) {
            // TODO
        }
        return String.format("%s as %s", res, rec.getEnv().getCursor().quote(col));
    }

    static String inheritsJoinCalc(Records rec, String alias, String fname, Query query) {
        Environment env = rec.getEnv();
        Cursor cr = env.getCursor();
        Records model = rec;
        MetaField field = rec.getMeta().getField(fname);
        while (field.isInherited()) {
            Records parentModel = env.get(field.getRelatedField().getModelName());
            String parentFname = field.getRelated()[0];
            String parentAlias = query.leftJoin(alias, parentFname, parentModel.getMeta().getTable(), "id",
                    parentFname);
            model = parentModel;
            alias = parentAlias;
            field = field.getRelatedField();
        }
        if (field instanceof Many2manyField) {
            Many2manyField m2m = (Many2manyField) field;
            // TODO comodel._apply_ir_rules(subquery)
            // Records comodel = env.get(m2m.getComodel());
            // Query subQuery = new Query(cr, comodel.getMeta().getTable());
            String relAlias = query.leftJoin(alias, "id", m2m.getRelation(), m2m.getColumn1(), field.getName());
            return String.format("%s.%s", cr.quote(relAlias), cr.quote(m2m.getColumn2()));
        } else if (field instanceof StringField && ((StringField<?>) field).isTranslate()) {
            return generateTranslatedField(model, alias, fname, query);
        }
        return String.format("%s.%s", cr.quote(alias), cr.quote(fname));
    }

    static String generateTranslatedField(Records rec, String tableAlias, String field, Query query) {
        Cursor cr = rec.getEnv().getCursor();
        // TODO rec.getEnv().getLang();
        return String.format("%s.%s", cr.quote(tableAlias), cr.quote(field));
    }

    /**
     * 根据查询条件查找记录集，根据指定参数查询满足条件的记录ID集合，返回记录集
     *
     * @param rec      记录集
     * @param criteria 查询条件
     * @param offset   记录开始位置
     * @param limit    记录限制数
     * @param order    记录排序顺序
     * @return 记录集
     */
    public Records find(Records rec, Criteria criteria, Integer offset, Integer limit, String order) {
        List<String> ids = doFind(rec, criteria, offset, limit, order);
        return rec.browse(ids);
    }

    List<String> doFind(Records rec, Criteria criteria, Integer offset, Integer limit, String order) {
        if (Expression.isFalse(rec, criteria)) {
            return Collections.emptyList();
        }
        flushSearch(rec, criteria, null, order);

        Query query = whereCalc(rec, criteria, true);
        // _apply_ir_rules

        query.setOrder(generateOrderBy(rec, order, query)).setLimit(limit).setOffset(offset);

        Cursor cr = rec.getEnv().getCursor();
        SelectClause select = query.select();
        cr.execute(select.getQuery(), select.getParams());
        List<String> ids = new ArrayList<>();
        for (Object[] row : cr.fetchAll()) {
            ids.add((String) row[0]);
        }
        return ids;
    }

    static List<String> generateM2oOrderBy(Records rec, String alias, String orderField, Query query,
            boolean reverseDirection, Set<String> seen) {
        MetaField field = rec.getMeta().getField(orderField);
        if (field.isInherited()) {
            String qualifiedField = inheritsJoinCalc(rec, alias, orderField, query);
            String[] arr = qualifiedField.split("\\.");
            // TODO remove quote
            alias = arr[0];
            orderField = arr[1];
            field = field.getBaseField();
        }
        if (!field.isStore()) {
            return Collections.emptyList();
        }
        Many2oneField m2o = (Many2oneField) field;
        Records destModel = rec.getEnv().get(m2o.getComodel());
        String m2oOrder = destModel.getMeta().getOrder();
        if (m2oOrder.contains(Constants.COMMA)) {
            // _order is complex, can't use it here, so we default to _rec_name
            m2oOrder = String.join(",", destModel.getMeta().getPresent());
        }
        String destAlias = query.leftJoin(alias, orderField, destModel.getMeta().getTable(), Constants.ID, orderField);
        return generateOrderByInner(destModel, destAlias, m2oOrder, query, reverseDirection, seen);
    }

    static List<String> generateOrderByInner(Records rec, String alias, String orderSpec, Query query,
            boolean reverseDirection, Set<String> seen) {
        if (seen == null) {
            seen = new HashSet<>();
        }
        // TODO _check_qorder
        List<String> orderByElements = new ArrayList<>();
        Cursor cr = rec.getEnv().getCursor();
        for (String orderPart : orderSpec.split(Constants.COMMA)) {
            String[] orderSplit = orderPart.trim().split(" ");
            String orderField = orderSplit[0].trim();
            String orderDirection = orderSplit.length == 2 ? orderSplit[1].trim().toLowerCase() : "";
            if (reverseDirection) {
                orderDirection = "DESC".equals(orderDirection) ? "ASC" : "DESC";
            }
            boolean doReverse = "DESC".equals(orderDirection);
            MetaField field = rec.getMeta().getField(orderField);
            if (Constants.ID.equals(orderField)) {
                orderByElements
                        .add(String.format("%s.%s %s", cr.quote(alias), cr.quote(orderField), orderDirection));
            } else {
                if (field.isInherited()) {
                    field = field.getBaseField();
                }
                if (field.isStore() && "many2one".equals(field.type)) {
                    Many2oneField m2o = (Many2oneField) field;
                    String key = field.modelName + "|" + m2o.getComodel() + "|" + orderField;
                    if (!seen.contains(key)) {
                        seen.add(key);
                        orderByElements
                                .addAll(generateM2oOrderBy(rec, alias, orderField, query, doReverse, seen));
                    }
                } else if (field.isStore() && field.getColumnType() != ColumnType.None) {
                    String qualifieldName = inheritsJoinCalc(rec, alias, orderField, query);
                    if ("boolean".equals(field.type)) {
                        qualifieldName = String.format("COALESCE(%s, false)", qualifieldName);
                    }
                    orderByElements.add(String.format("%s %s", qualifieldName, orderDirection));
                } else {
                    logger.warn("模型{}不能按字段{}排序", rec.getMeta().getName(), orderField);
                }
            }
        }
        return orderByElements;
    }

    public static String generateOrderBy(Records rec, String orderSpec, Query query) {
        String orderByClause = "";
        if (StringUtils.isBlank(orderSpec)) {
            orderSpec = rec.getMeta().getOrder();
        }
        if (StringUtils.isNotBlank(orderSpec)) {
            List<String> orderByElements = generateOrderByInner(rec, rec.getMeta().getTable(), orderSpec, query,
                    false, null);
            if (!orderByElements.isEmpty()) {
                orderByClause = StringUtils.join(orderByElements);
            }
        }
        return orderByClause;
    }

    public static Query whereCalc(Records rec, Criteria criteria, boolean activeTest) {
        // TODO active criteria
        if (!criteria.isEmpty()) {
            return new Expression(criteria, rec, null, null).getQuery();
        } else {
            return new Query(rec.getEnv().getCursor(), rec.getMeta().getTable(), (String) rec.call("getTableQuery"));
        }
    }

    /**
     * 查询并读取，返回字段值列表
     *
     * @param rec      记录集
     * @param fields   字段列表
     * @param criteria 查询条件
     * @param offset   记录开始位置
     * @param limit    记录限制数
     * @param order    记录排序顺序
     * @return 字段值列表
     */
    public List<Map<String, Object>> search(Records rec, Collection<String> fields, Criteria criteria, Integer offset,
            Integer limit, String order) {
        return read(find(rec, criteria, offset, limit, order), fields);
    }

    /**
     * 按present字段查询
     * 
     * @param rec
     * @param fields  要查询的字段
     * @param keyword 查询关键字
     * @param offset  记录开始位置
     * @param limit   记录限制数
     * @param order   记录排序顺序
     * @return
     */
    @ServiceMethod(auth = "read", label = "展示值查询", doc = "按记录展示字段查询")
    public List<Map<String, Object>> presentSearch(Records rec, Collection<String> fields, String keyword,
            Integer offset, Integer limit, String order) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotEmpty(keyword)) {
            String[] present = rec.getMeta().getPresent();
            for (String fname : present) {
                criteria.or(Criteria.like(fname, keyword));
            }
        }
        return read(find(rec, criteria, offset, limit, order), fields);
    }

    public String computePresent(Records rec) {
        rec.ensureOne();
        return (String) rec.getPresent().get(0)[1];
    }

    public Criteria searchPresent(Records rec, String op, Object value) {
        Criteria criteria = new Criteria();
        boolean hasValue = value != null;
        if (hasValue && value instanceof String) {
            hasValue = StringUtils.isNotEmpty((String) value);
        }
        if (hasValue) {
            for (String field : rec.getMeta().getPresent()) {
                criteria.or(field, op, value);
            }
        }
        return criteria;
    }

    /**
     * 统计记录集的数量
     *
     * @param rec      记录集
     * @param criteria 查询条件
     * @return
     */
    public long count(Records rec, Criteria criteria) {
        if (Expression.isFalse(rec, criteria)) {
            return 0;
        }
        flushSearch(rec, criteria, null, null);

        Query query = whereCalc(rec, criteria, true);
        // _apply_ir_rules

        Cursor cr = rec.getEnv().getCursor();
        SelectClause select = query.select("count(1)");
        cr.execute(select.getQuery(), select.getParams());
        return ObjectUtils.toLong(cr.fetchOne()[0]);
    }

    // #region update

    /**
     * 更新所有记录的值
     *
     * @param rec    记录集
     * @param values 字段值字典
     */
    public void update(Records rec, Map<String, Object> values) {
        if (!rec.any()) {
            return;
        }
        checkAccessRule(rec, "write");

        MetaModel meta = rec.getMeta();
        Set<String> badNames = new HashSet<>(5);
        badNames.add(Constants.ID);
        // TODO badNames.add(Constants.PARENT_PATH);
        if (meta.isLogAccess()) {
            badNames.addAll(LOG_ACCESS_COLUMNS);
        }
        for (String badName : badNames) {
            values.remove(badName);
        }
        // set magic fields
        if (meta.isLogAccess()) {
            values.putIfAbsent(Constants.UPDATE_UID, rec.getEnv().getUserId());
            values.putIfAbsent(Constants.UPDATE_DATE, new ServerDate());
        }

        // X2many fields must be written last, because they flush other fields when
        // deleting lines.
        values.keySet().stream().map(fname -> meta.getField(fname))
                .sorted(Comparator.comparing(f -> (f instanceof RelationalMultiField) ? 20 : 0))
                .forEach(field -> {
                    field.write(rec, values.get(field.getName()));
                });

        validateConstraints(rec, values.keySet());
    }

    void doUpdate(Records rec, Map<String, Object> vals) {
        if (!rec.any()) {
            return;
        }
        validateValues(rec, vals, false);
        // 并发检查
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        Cursor cr = rec.getEnv().getCursor();

        if (rec.getMeta().isLogAccess()) {
            vals.putIfAbsent(Constants.UPDATE_UID, rec.getEnv().getUserId());
            vals.putIfAbsent(Constants.UPDATE_DATE, new ServerDate());
        }

        vals.entrySet().stream().sorted(Comparator.comparing(v -> v.getKey())).forEach(entry -> {
            String fname = entry.getKey();
            Object val = entry.getValue();
            if (rec.getMeta().isLogAccess() && LOG_ACCESS_COLUMNS.contains(fname) && val == null) {
                return;
            }
            MetaField field = rec.getMeta().getField(fname);
            assert field.isStore();
            if (val instanceof AsSql) {
                columns.add(String.format("%s=%s", cr.quote(fname), ((AsSql) val).getValue()));
            } else if (val instanceof ServerDate) {
                columns.add(String.format("%s=%s", cr.quote(fname), cr.getSqlDialect().getNowUtc()));
            } else {
                columns.add(String.format("%s=%%s", cr.quote(fname)));
                values.add(val);
            }
        });

        if (!columns.isEmpty()) {
            String sql = String.format("UPDATE %s SET %s WHERE id IN %%s", cr.quote(rec.getMeta().getTable()),
                    StringUtils.join(columns));
            for (Object[] subIds : cr.splitForInConditions(rec.getIds())) {
                List<Object> params = new ArrayList<>(values);
                params.add(Arrays.asList(subIds));
                cr.execute(sql, params);
                // TODO check rowcount == sub_ids.length
            }
        }
    }

    // #endregion

    /**
     * 更新记录集的值
     *
     * @param rec    记录集
     * @param values 字段值字典
     */
    public void load(Records rec, Map<String, Object> values) {
        for (Records r : rec) {
            for (Entry<String, Object> entry : values.entrySet()) {
                r.set(entry.getKey(), entry.getValue());
            }
        }
    }

    public void checkDeleteParent(Records rec) {
        MetaField f = rec.getMeta().findField("parent_id");
        if (f instanceof Many2oneField) {
            Many2oneField pid = (Many2oneField) f;
            if (DeleteMode.Restrict.equals(pid.getOnDelete())) {
                if (rec.count(Criteria.in("parent_id", Arrays.asList(rec.getIds()))) > 0) {
                    throw new ValidationException(rec.l10n("删除失败, 请先删除子") + rec.l10n(rec.getMeta().getLabel()));
                }
            }
        }
    }

    public boolean delete(Records rec) {
        if (!rec.any()) {
            return false;
        }
        flush(rec);
        checkDeleteParent(rec);
        Cursor cr = rec.getEnv().getCursor();
        for (Object[] subIds : cr.splitForInConditions(rec.getIds())) {
            // log delete data
            String sql = "DELETE FROM " + cr.quote(rec.getMeta().getTable()) + " WHERE id IN %s";
            try {
                cr.execute(sql, Arrays.asList(Arrays.asList(subIds)));
            } catch (SqlConstraintException e) {
                String constraint = e.getConstraint();
                String comodel = (String) rec.getEnv().get("ir.model.constraint")
                        .find(Criteria.equal("name", constraint)).get("message");
                String msg = rec.l10n("删除失败, 数据被 %s 引用", rec.l10n(rec.getEnv().getRegistry().get(comodel).getLabel()));
                throw new ValidationException(msg);
            }
        }
        return true;
    }

    // #region flush

    /**
     * 把当前模型待更新的所有数据保存到数据库
     *
     * @param rec 记录集
     */
    public void flushModel(Records rec, Collection<String> fnames) {
        // TODO related_field
        IdValues idVals = rec.getEnv().getToUpdate().remove(rec.getMeta().getName());
        processFlush(rec.getEnv(), rec.getMeta().getName(), idVals);
    }

    /**
     * 把待更新的所有数据保存到数据库
     *
     * @param rec
     */
    public void flush(Records rec) {
        ToUpdate toupdate = rec.getEnv().getToUpdate();
        Environment env = rec.getEnv();
        for (String model : toupdate.getModels().toArray(ArrayUtils.EMPTY_STRING_ARRAY)) {
            IdValues idVals = toupdate.remove(model);
            processFlush(env, model, idVals);
        }
    }

    public void flushSearch(Records rec, Criteria criteria, Collection<String> fields, String order) {
        // TODO
        flushModel(rec, fields);
    }

    void processFlush(Environment env, String model, IdValues idVals) {
        // group record ids by vals, to update in batch when possible
        Map<Map<String, Object>, List<String>> valsIds = new HashMap<>(idVals.size());
        for (Entry<String, Map<String, Object>> e : idVals.entrySet()) {
            Map<String, Object> vals = e.getValue();
            List<String> ids = valsIds.get(vals);
            if (ids == null) {
                ids = new ArrayList<>();
                valsIds.put(vals, ids);
            }
            ids.add(e.getKey());
        }
        for (Entry<Map<String, Object>, List<String>> e : valsIds.entrySet()) {
            Map<String, Object> vals = e.getKey();
            List<String> ids = e.getValue();
            Records recs = env.get(model, ids);
            doUpdate(recs, vals);
        }
    }

    // #endregion

    /**
     * 复制记录
     *
     * @param rec 记录集
     * @return
     */
    @ServiceMethod(auth = "create", label = "复制记录", doc = "复制记录的数据并保存")
    public Records copy(Records rec, Map<String, Object> defaultValues) {
        rec.ensureOne();
        Map<String, Object> vals = copyData(rec.withContext("active_test", false), defaultValues);
        return rec.create(vals);
    }

    public Map<String, Object> copyData(Records rec, @Nullable Map<String, Object> defaultValues) {
        Map<String, Object> defaults = new HashMap<>();
        if (defaultValues != null) {
            defaults.putAll(defaultValues);
        }
        for (Entry<String, MetaField> e : rec.getMeta().getFields().entrySet()) {
            String fname = e.getKey();
            MetaField field = e.getValue();
            if (Constants.ID.equals(fname) || LOG_ACCESS_COLUMNS.contains(fname)
                    || defaults.containsKey(fname) || !field.isCopy()) {
                continue;
            }
            if (Constants.ONE2MANY.equals(field.getType())) {
                Records o2m = (Records) rec.get(fname);
                List<List<Object>> lines = new ArrayList<List<Object>>();
                for (Records r : o2m) {
                    lines.add(Arrays.asList(0, 0, copyData(r, null)));
                }
                defaults.put(fname, lines);
            } else if (Constants.MANY2MANY.equals(field.getType())) {
                List<String> ids = Arrays.asList(((Records) rec.get(fname)).getIds());
                defaults.put(fname, Arrays.asList(Arrays.asList(6, 0, ids)));
            } else {
                defaults.put(fname, field.convertToWrite(rec.get(field), rec));
            }
        }
        MetaModel model = rec.getMeta();
        for (UniqueConstraint u : model.getUniques()) {
            for (String field : u.getFields()) {
                boolean isDefaultValue = defaultValues != null && defaultValues.containsKey(field);
                if (!isDefaultValue && defaults.containsKey(field)) {
                    String v = String.format("%s (副本)", defaults.get(field));
                    defaults.put(field, v);
                }
            }
        }
        return defaults;
    }

    // #region check access

    /**
     * 检查字段访问权限
     * 
     * @param rec
     * @param operation
     * @param fields
     * @return
     */
    @SuppressWarnings("unchecked")
    public Collection<String> checkFieldAccessRights(Records rec, String operation, Collection<String> fields) {
        MetaModel m = rec.getMeta();
        Set<String> deny = (Set<String>) rec.getEnv().get("rbac.permission").call("loadModelDenyFields", m.getName());
        for (String field : fields) {
            if (deny.contains(field)) {
                throw new AccessException(rec.l10n("没有访问模型[%s(%s)]的字段[%s(%s)]的权限", m.getLabel(), m.getName(),
                        m.getField(field).getLabel(), field));
            }
        }
        return fields;
    }

    public void checkAccessRule(Records rec, String action) {

    }

    // #endregion

    private ModelRefector refector;

    ModelRefector getRefector() {
        if (refector == null) {
            refector = new ModelRefector();
            refector.model = this;
        }
        return refector;
    }

    public class ModelRefector {

        BaseModel model;

        /** 从注解获取名称 */
        public String getName() {
            Class<?> clazz = model.getClass();
            Model.Meta a = clazz.getAnnotation(Model.Meta.class);
            if (a != null) {
                return a.name();
            }
            return clazz.getName();
        }

        /** 从注解获取继承 */
        public List<String> getInherit() {
            Class<?> clazz = model.getClass();
            Model.Meta a = clazz.getAnnotation(Model.Meta.class);
            if (a != null) {
                return new ArrayList<String>(Arrays.asList(a.inherit()));
            }
            return new ArrayList<String>();
        }

        /** 从注解获取参数 */
        public Map<String, Object> getArgs() {
            Map<String, Object> map = new HashMap<>(16);
            Class<?> clazz = model.getClass();
            Model.Meta a = clazz.getAnnotation(Model.Meta.class);
            if (a != null) {
                String label = a.label();
                if (StringUtils.isNotBlank(label)) {
                    map.put(Constants.LABEL, label);
                }
                String authModel = a.authModel();
                if (StringUtils.isNotBlank(authModel)) {
                    map.put(Constants.AUTH_MODEL, authModel);
                }
                String desc = a.description();
                if (StringUtils.isNotBlank(desc)) {
                    map.put(Constants.DESCRIPTION, desc);
                }
                String order = a.order();
                if (StringUtils.isNotBlank(order)) {
                    map.put(Constants.ORDER, order);
                }
                String[] present = a.present();
                if (present.length > 0) {
                    map.put(Constants.PRESENT, present);
                }
                String presentFormat = a.presentFormat();
                if (StringUtils.isNoneBlank(presentFormat)) {
                    map.put(Constants.PRESENT_FORMAT, presentFormat);
                }
                String table = a.table();
                if (StringUtils.isNotBlank(table)) {
                    map.put(Constants.TABLE, table);
                }
                BoolState log = a.logAccess();
                if (log == BoolState.False) {
                    map.put(Constants.LOG_ACCESS, false);
                } else if (log == BoolState.True) {
                    map.put(Constants.LOG_ACCESS, true);
                }
            }
            return map;
        }
    }
}
