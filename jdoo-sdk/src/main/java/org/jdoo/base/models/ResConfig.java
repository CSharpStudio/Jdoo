package org.jdoo.base.models;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jdoo.*;
import org.jdoo.core.Constants;
import org.jdoo.core.MetaField;
import org.jdoo.core.MetaModel;
import org.jdoo.data.ColumnType;
import org.jdoo.data.Cursor;
import org.jdoo.data.DbColumn;
import org.jdoo.data.SqlDialect;
import org.jdoo.util.Cache;
import org.jdoo.utils.IdWorker;

@Model.Meta(name = "res.config", label = "设置")
public class ResConfig extends Model {
    static final String RECORD_ID = "res-config";

    public ResConfig() {
        isAuto = false;
    }

    static Field pwd_complexity = Field.Boolean().label("密码必须符合复杂性要求")
            .help("在更改或创建密码时执行复杂性要求: 至少包含大写字母、小写字母、数字、特殊字符四类中的三类");
    static Field pwd_min_length = Field.Integer().label("密码长度最小值").defaultValue(6);
    static Field pwd_validity = Field.Integer().label("密码有效期(天)").defaultValue(45);
    static Field login_try_times = Field.Integer().label("允许错误次数").defaultValue(5);
    static Field login_lock_time = Field.Float().label("锁定时长(秒)").defaultValue(300);

    @ServiceMethod(label = "加载密码策略")
    public Map<String, Object> loadData(Records rec, List<String> fields) {
        return rec.getEnv().getRef("base.rbac_password_policy").read(fields).get(0);
    }

    @Override
    public void init(Records rec) {
        Cursor cr = rec.getEnv().getCursor();
        SqlDialect sd = cr.getSqlDialect();
        MetaModel meta = rec.getMeta();
        String table = meta.getTable();
        Map<String, DbColumn> columns = new HashMap<>();
        columns.put("name", new DbColumn("name", sd.getColumnType(ColumnType.VarChar, 200, null), 200, false));
        columns.put("value", new DbColumn("value", sd.getColumnType(ColumnType.VarChar, 800, null), 800, true));
        columns.put("update_uid",
                new DbColumn("update_uid", sd.getColumnType(ColumnType.VarChar, 13, null), 13, false));
        columns.put("update_date", new DbColumn("update_date", sd.getColumnType(ColumnType.DateTime), null, false));
        if (!sd.tableExists(cr, table)) {
            sd.createModelTable(cr, table, meta.getLabel());
            for (Entry<String, DbColumn> e : columns.entrySet()) {
                DbColumn column = e.getValue();
                sd.createColumn(cr, table, column.getColumn(), column.getType(), null, !column.getNullable());
            }
        } else {
            Map<String, DbColumn> cols = sd.tableColumns(cr, table);
            for (Entry<String, DbColumn> e : columns.entrySet()) {
                DbColumn column = e.getValue();
                if (!cols.containsKey(column.getColumn())) {
                    sd.createColumn(cr, table, column.getColumn(), column.getType(), null, !column.getNullable());
                }
            }
        }
    }

    @Override
    public Records createBatch(Records rec, List<Map<String, Object>> valuesList) {
        return rec.browse(RECORD_ID);
    }

    @Override
    public Records find(Records rec, Criteria criteria, Integer offset, Integer limit, String order) {
        return rec.browse(RECORD_ID);
    }

    @Override
    public void update(Records rec, Map<String, Object> values) {
        rec.ensureOne();
        MetaModel meta = rec.getMeta();
        Cursor cr = rec.getEnv().getCursor();
        String sql = "SELECT name FROM res_config";
        cr.execute(sql);
        Set<String> names = cr.fetchAll().stream().map(row -> (String) row[0]).collect(Collectors.toSet());
        for (Entry<String, Object> e : values.entrySet()) {
            String fname = e.getKey();
            meta.getField(fname);
            if (names.contains(fname)) {
                cr.execute("UPDATE res_config SET value=%s,update_uid=%s,update_date=" + cr.getSqlDialect().getNowUtc()
                        + " WHERE name=%s", Arrays.asList(e.getValue(), rec.getEnv().getUserId(), fname));
            } else {
                cr.execute("INSERT INTO res_config(id, name, value, update_uid, update_date) values (%s, %s, %s, %s, "
                        + cr.getSqlDialect().getNowUtc() + ")",
                        Arrays.asList(IdWorker.nextId(), fname, e.getValue(), rec.getEnv().getUserId()));
            }
        }
    }

    @Override
    public List<Map<String, Object>> read(Records rec, Collection<String> fields) {
        MetaModel meta = rec.getMeta();
        List<MetaField> metaFields = fields.stream().filter(f -> !Constants.ID.equals(f)).map(f -> meta.getField(f))
                .collect(Collectors.toList());
        List<String> names = metaFields.stream().filter(f -> f.isStore() && f.getColumnType() != ColumnType.None)
                .map(f -> f.getName()).collect(Collectors.toList());
        Cursor cr = rec.getEnv().getCursor();
        String sql = "SELECT name, value FROM res_config WHERE name in %s";
        cr.execute(sql, Arrays.asList(names));
        Map<String, Object> values = new HashMap<>();
        for (Object[] row : cr.fetchAll()) {
            values.put((String) row[0], row[1]);
        }
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.ID, RECORD_ID);
        Cache cache = rec.getEnv().getCache();
        for (MetaField field : metaFields) {
            String fname = field.getName();
            Object value = null;
            if (field.isStore()) {
                if (values.containsKey(fname)) {
                    value = values.get(fname);
                } else if (field.getColumnType() != ColumnType.None) {
                    value = field.getDefault(rec);
                    try (Cursor c = rec.getEnv().getRegistry().getTenant().getDatabase().openCursor()) {
                        c.execute(
                                "INSERT INTO res_config(id, name, value, update_uid, update_date) values (%s, %s, %s, %s, "
                                        + c.getSqlDialect().getNowUtc() + ")",
                                Arrays.asList(IdWorker.nextId(), fname, value, Constants.SYSTEM_USER));
                        c.commit();
                    }
                } else {
                    field.read(rec);
                    value = rec.get(fname);
                }
                Object cacheValue = field.convertToCache(value, rec, true);
                cache.update(rec, field, Arrays.asList(cacheValue));
            } else {
                value = rec.get(fname);
            }
            value = field.convertToRead(value, rec, false);
            map.put(fname, value);
        }
        for (Entry<String, Object> e : map.entrySet()) {
            cache.update(rec, meta.getField(e.getKey()), Arrays.asList(e.getValue()));
        }
        return Arrays.asList(map);
    }

    @Override
    public void fetchField(Records rec, MetaField field) {
        rec.read(Arrays.asList(field.getName()));
    }
}
