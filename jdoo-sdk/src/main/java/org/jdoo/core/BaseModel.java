package org.jdoo.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jdoo.Model;
import org.jdoo.BoolState;
import org.jdoo.Doc;
import org.jdoo.Criteria;
import org.jdoo.Records;
import org.jdoo.data.AsSql;
import org.jdoo.data.ColumnType;
import org.jdoo.data.Cursor;
import org.jdoo.data.Expression;
import org.jdoo.data.Query;
import org.jdoo.data.Query.SelectClause;
import org.jdoo.exceptions.ModelException;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.exceptions.ValueException;
import org.jdoo.fields.Many2oneField;
import org.jdoo.fields.StringField;
import org.jdoo.services.*;
import org.jdoo.utils.ArrayUtils;
import org.jdoo.utils.IdWorker;
import org.jdoo.utils.StringUtils;
import org.jdoo.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jdoo.util.Cache;
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
@Model.Service(name = "create", label = "新建", description = "为模型创建新记录", type = CreateService.class)
@Model.Service(name = "createBatch", label = "批量新建", auth = "create", description = "批量创建新记录", type = BatchCreateService.class)
@Model.Service(name = "find", label = "查找", auth = "read", description = "根据参数搜索记录", type = FindService.class)
@Model.Service(name = "search", label = "查询", auth = "read", description = "搜索并读取记录集指定字段的值", type = SearchService.class)
@Model.Service(name = "count", label = "计数", auth = "read", description = "统计匹配条件的记录数", type = CountService.class)
@Model.Service(name = "delete", label = "删除", description = "删除当前集合的记录", type = DeleteService.class)
@Model.Service(name = "update", label = "更新", description = "使用提供的值更新当前集中的所有记录", type = UpdateService.class)
public class BaseModel {
    private static Logger logger = LogManager.getLogger(BaseModel.class);
    final static List<String> LOG_ACCESS_COLUMNS = Arrays.asList(Constants.CREATE_DATE, Constants.CREATE_UID,
            Constants.UPDATE_DATE, Constants.UPDATE_UID);
    private Records recordSet;
    private MetaModel meta;
    protected boolean isAuto;
    protected boolean isAbstract = true;
    protected boolean isTransient;
    protected boolean custom;

    // #region get/set

    MetaModel getMeta() {
        return meta;
    }

    void setMeta(MetaModel meta) {
        this.meta = meta;
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
            throw new ModelException(String.format("class[%s],模型[%s]未设置记录集", getClass().getName(), meta.getName()));
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
     * @return 新记录ID
     */
    public Records create(Records rec, Map<String, ? extends Object> values) {
        return createBatch(rec, Arrays.asList(values));
    }

    /**
     * 批量创建模型的记录，根据提供的字段值字典列表，批量创建数据库记录
     * 
     * @param rec        记录集
     * @param valuesList 新值列表
     * @return 新记录ID列表
     */
    public Records createBatch(Records rec, List<? extends Map<String, ? extends Object>> valuesList) {
        checkAccessRights(rec, "create");
        Set<String> badNames = new HashSet<>();
        if (rec.getMeta().getRegistry().loaded) {
            badNames.add(Constants.ID);
            badNames.add("parent_path");
            if (rec.getMeta().isLogAccess()) {
                badNames.addAll(LOG_ACCESS_COLUMNS);
            }
        }
        List<NewData> dataList = new ArrayList<>();
        for (Map<String, ? extends Object> values : valuesList) {
            Map<String, ? extends Object> vals = addMissingDefaultValues(rec, values);

            NewData data = new NewData();
            data.stored = new HashMap<>(values.size());

            for (String key : vals.keySet()) {
                if (badNames.contains(key)) {
                    continue;
                }
                Object val = vals.get(key);
                MetaField field = rec.getMeta().findField(key);
                if (field == null) {
                    logger.warn("{}.create()遇到未知字段{}}", rec.getMeta().getName(), key);
                    continue;
                }

                if (field.isStore()) {
                    data.stored.put(key, val);
                }
            }
            dataList.add(data);
        }
        Records records = doCreate(rec, dataList);

        return records;
    }

    class NewData {
        public Map<String, Object> stored;
        public Records records;
    }

    static void validateFieldsRequried(Records rec, Map<String, Object> values, boolean create) {
        List<String> errors = new ArrayList<>();
        Object defaultValue = create ? null : true;
        for (MetaField f : rec.getMeta().getFields().values()) {
            if (f.isRequired()) {
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

    static void validateFieldsUnique(Records records, Map<String, Object> values, boolean create) {
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
                    validateFieldsUnique(records, constraint, values, create);
                } else {
                    for (Records rec : records) {
                        validateFieldsUnique(rec, constraint, values, create);
                    }
                }
            }
        }
    }

    static void validateFieldsUnique(Records records, UniqueConstraint constraint, Map<String, Object> values,
            boolean create) {
        Criteria criteria = new Criteria();
        for (String field : constraint.getFields()) {
            Object value;
            if (!create && !values.containsKey(field)) {
                value = records.get(field);
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

    static void validateConstraints(Records rec, Set<String> fields) {
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

    static Records doCreate(Records rec, List<NewData> dataList) {
        Cursor cr = rec.getEnv().getCursor();
        List<String> newIds = new ArrayList<>();
        List<MetaField> otherFields = new ArrayList<>();
        Set<MetaField> translatedFields = new HashSet<>();
        for (NewData data : dataList) {
            Map<String, Object> stored = data.stored;
            validateFieldsRequried(rec, stored, true);
            validateFieldsUnique(rec, stored, true);
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
                        if (field instanceof StringField && ((StringField<?>) field).getTranslate()) {
                            translatedFields.add(field);
                        }
                    }
                } else {
                    otherFields.add(field);
                }
            }

            if (rec.getMeta().isLogAccess()) {
                columns.add(cr.quote(Constants.CREATE_UID));
                formats.add("%s");
                values.add(rec.getEnv().getUserId());
                columns.add(cr.quote(Constants.CREATE_DATE));
                formats.add(cr.getSqlDialect().getNowUtc());
                columns.add(cr.quote(Constants.UPDATE_UID));
                formats.add("%s");
                values.add(rec.getEnv().getUserId());
                columns.add(cr.quote(Constants.UPDATE_DATE));
                formats.add(cr.getSqlDialect().getNowUtc());
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
        rec.call("checkAccessRule", "create");

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
                if ("one2many".equals(field.type) || "many2many".equals(field.type)) {
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

    static Map<String, ? extends Object> addMissingDefaultValues(Records rec, Map<String, ? extends Object> vals) {
        // TODO
        return vals;
    }

    // #endregion

    /**
     * 读取记录集指定字段的数据
     * 
     * @param rec    记录集
     * @param fields
     * @return 字段值列表
     */
    public List<Map<String, Object>> read(Records rec, List<String> fields) {
        fields = checkFieldAccessRight(rec, "read", fields);
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
        for (Records r : rec) {
            Map<String, Object> vals = new HashMap<>(fields.size());
            vals.put(Constants.ID, r.getId());
            for (String fname : fields) {
                MetaField field = rec.getMeta().getField(fname);
                vals.put(fname, field.convertToRead(r.get(fname), rec, true));
            }
            result.add(vals);
        }
        return result;
    }

    public List<String> checkFieldAccessRight(Records rec, String operation, List<String> fields) {
        return fields;
    }

    public Records exists(Records rec) {
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
        checkFieldAccessRights(rec, "read", ArrayUtils.asList(field.getName()));
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

    @Model.ServiceMethod(auth = "read")
    public Map<String, Object> lookup(Records rec, String field, String keyword, Integer limit, Integer offset) {
        if (limit == null) {
            limit = 10;
        }
        MetaField f = rec.getMeta().findField(field);
        if (f == null) {
            throw new ModelException(String.format("模型[%s]不存在字段[%s]", rec.getMeta().getName(), field));
        }
        if (!(f instanceof Many2oneField)) {
            throw new ModelException(String.format("模型[%s]的字段[%s]不是Many2oneField", rec.getMeta().getName(), field));
        }
        Many2oneField m2o = (Many2oneField) f;
        Records comodel = rec.getEnv().get(m2o.getComodel());
        String[] present = comodel.getMeta().getPresent();
        Criteria criteria = new Criteria();
        if (StringUtils.isNotEmpty(keyword)) {
            String[] fields = present;
            for (String fname : fields) {
                criteria.or(Criteria.like(fname, keyword));
            }
        }
        comodel = comodel.find(criteria, offset, limit + 1, null);
        List<Object[]> data = comodel.getPresent();
        Map<String, Object> result = new HashMap<>();
        boolean hasNext = comodel.size() > limit;
        if (hasNext) {
            data.remove(data.size() - 1);
        }
        result.put("hasNext", hasNext);
        result.put("values", data);
        return result;
    }

    @Model.ServiceMethod(auth = "read")
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
                        display += field.convertToDisplayName(r.get(field), r);
                    }
                } else {
                    display = format;
                    for (String fname : present) {
                        MetaField field = meta.getField(fname);
                        display = display.replaceAll("\\{" + fname + "\\}",
                                (String) field.convertToDisplayName(r.get(field), r));
                    }
                }
            } else {
                display = String.format("%s,%s", meta.getName(), r.getId());
            }
            result.add(new Object[] { r.getId(), display });
        }
        return result;
    }

    static void doRead(Records rec, Collection<String> fields) {
        if (!rec.any()) {
            return;
        }
        rec.call("checkAccessRights", "read");
        // TODO flush fields only
        rec.flush();

        Set<MetaField> fieldsRead = new HashSet<>();
        Set<MetaField> fieldsPre = new HashSet<>();
        for (String name : fields) {
            if (name == Constants.ID) {
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
            Query query = new Query(cr, rec.getMeta().getTable(), (String) rec.call("getTableQuery"));
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
            rec.call("checkAccessRule", "read");
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
        String res = joinCalc(rec, rec.getMeta().getTable(), field.getName(), query);
        if (Constants.BINARY.equals(field.type)) {
            // TODO
        }
        return String.format("%s as %s", res, rec.getEnv().getCursor().quote(col));
    }

    static String joinCalc(Records rec, String alias, String fname, Query query) {
        Cursor cr = rec.getEnv().getCursor();
        MetaField field = rec.getMeta().getField(fname);
        if (field instanceof StringField<?> && ((StringField<?>) field).getTranslate()) {
            return generateTranslatedField(rec, alias, fname, query);
        } else {
            return String.format("%s.%s", cr.quote(alias), cr.quote(fname));
        }
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

    static List<String> doFind(Records rec, Criteria criteria, Integer offset, Integer limit, String order) {
        rec.call("checkAccessRights", "read");
        if (Expression.isFalse(rec, criteria)) {
            return Collections.emptyList();
        }
        rec.call("flushSearch", criteria, null, order);

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
                if (field.isStore() && "many2one".equals(field.type)) {
                    Many2oneField m2o = (Many2oneField) field;
                    String key = field.modelName + "|" + m2o.getComodel() + "|" + orderField;
                    if (!seen.contains(key)) {
                        seen.add(key);
                        orderByElements
                                .addAll(generateM2oOrderBy(rec, alias, orderField, query, doReverse, seen));
                    }
                } else if (field.isStore() && field.getColumnType() != ColumnType.None) {
                    String qualifieldName = joinCalc(rec, alias, orderField, query);
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
    public List<Map<String, Object>> search(Records rec, List<String> fields, Criteria criteria, Integer offset,
            Integer limit, String order) {
        return read(find(rec, criteria, offset, limit, order), fields);
    }

    public String computePresent(Records rec) {
        rec.ensureOne();
        return (String) rec.getPresent().get(0)[1];
    }

    /**
     * 统计记录集的数量
     * 
     * @param rec      记录集
     * @param criteria 查询条件
     * @return
     */
    public long count(Records rec, Criteria criteria) {
        checkAccessRights(rec, "read");
        if (Expression.isFalse(rec, criteria)) {
            return 0;
        }
        flushSearch(rec, criteria, null, null);

        Query query = whereCalc(rec, criteria, true);
        // _apply_ir_rules

        Cursor cr = rec.getEnv().getCursor();
        SelectClause select = query.select("count(1)");
        cr.execute(select.getQuery(), select.getParams());
        return (Long) cr.fetchOne()[0];
    }

    // #region update

    /**
     * 更新所有记录的值
     * 
     * @param rec    记录集
     * @param values 字段值字典
     */
    public void update(Records rec, Map<String, ? extends Object> values) {
        checkAccessRights(rec, "write");
        checkFieldAccessRights(rec, "write", values.keySet());
        checkAccessRule(rec, "write");

        MetaModel meta = rec.getMeta();
        Set<String> badNames = new HashSet<>(5);
        badNames.add(Constants.ID);
        if (meta.isLogAccess()) {
            badNames.addAll(LOG_ACCESS_COLUMNS);
        }

        if (rec.any() && meta.isLogAccess()) {
            IdValues toupdate = rec.getEnv().getToUpdate().get(meta.getName());
            for (String id : rec.getIds()) {
                Map<String, Object> data = toupdate.get(id);
                data.put(Constants.UPDATE_UID, rec.getEnv().getUserId());
                data.put(Constants.UPDATE_DATE, null);
            }
            // invalidate cache
            rec.getEnv().getCache().invalidate(new ArrayList<Tuple<MetaField, String[]>>() {
                {
                    add(new Tuple<>(meta.getField(Constants.UPDATE_UID), rec.getIds()));
                    add(new Tuple<>(meta.getField(Constants.UPDATE_DATE), rec.getIds()));
                }
            });
        }

        for (String fname : values.keySet()) {
            if (badNames.contains(fname)) {
                continue;
            }
            MetaField field = rec.getMeta().getField(fname);
            field.write(rec, values.get(fname));
        }

        validateConstraints(rec, values.keySet());
    }

    static void doUpdate(Records rec, Map<String, Object> vals) {
        if (!rec.any()) {
            return;
        }
        validateFieldsRequried(rec, vals, false);
        validateFieldsUnique(rec, vals, false);
        // 并发检查
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        Cursor cr = rec.getEnv().getCursor();
        for (Entry<String, Object> entry : vals.entrySet()) {
            String fname = entry.getKey();
            Object val = entry.getValue();
            if (rec.getMeta().isLogAccess() && LOG_ACCESS_COLUMNS.contains(fname) && val == null) {
                continue;
            }
            MetaField field = rec.getMeta().getField(fname);
            assert field.isStore();
            if (val instanceof ServerDate) {
                columns.add(String.format("%s=%s", cr.quote(fname), cr.getSqlDialect().getNowUtc()));
            } else {
                columns.add(String.format("%s=%%s", cr.quote(fname)));
                values.add(val);
            }
        }
        if (rec.getMeta().isLogAccess()) {
            if (vals.getOrDefault(Constants.UPDATE_UID, null) == null) {
                columns.add(String.format("%s=%%s", cr.quote(Constants.UPDATE_UID)));
                values.add(rec.getEnv().getUserId());
            }
            if (vals.getOrDefault(Constants.UPDATE_DATE, null) == null) {
                columns.add(String.format("%s=%s", cr.quote(Constants.UPDATE_DATE),
                        cr.getSqlDialect().getNowUtc()));
            }
        }
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
    public void load(Records rec, Map<String, ? extends Object> values) {
        for (Records r : rec) {
            for (Entry<String, ? extends Object> entry : values.entrySet()) {
                r.set(entry.getKey(), entry.getValue());
            }
        }
    }

    public boolean delete(Records rec) {
        checkAccessRights(rec, "delete");
        flush(rec);
        Cursor cr = rec.getEnv().getCursor();
        for (Object[] subIds : cr.splitForInConditions(rec.getIds())) {
            // log delete data
            String sql = "DELETE FROM " + cr.quote(rec.getMeta().getTable()) + " WHERE id IN (%s)";
            cr.execute(sql, Arrays.asList(subIds));
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

    static void processFlush(Environment env, String model, IdValues idVals) {
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
    public Records copy(Records rec) {
        throw new UnsupportedOperationException();
    }

    // #region check access

    public void checkFieldAccessRights(Records rec, String operation, Collection<String> fields) {
        // TODO
    }

    public void checkAccessRights(Records rec, String action) {

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
                String desc = a.description();
                if (StringUtils.isNotBlank(desc)) {
                    map.put(Constants.DESCRIPTION, desc);
                }
                String order = a.order();
                if (StringUtils.isNotBlank(order)) {
                    map.put(Constants.ORDER, order);
                }
                map.put(Constants.PRESENT, a.present());
                map.put(Constants.PRESENT_FORMAT, a.presentFormat());
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
