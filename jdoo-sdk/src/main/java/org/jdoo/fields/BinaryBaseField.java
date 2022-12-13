package org.jdoo.fields;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.jdoo.Criteria;
import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.core.Environment;
import org.jdoo.core.MetaModel;
import org.jdoo.data.ColumnType;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.util.Cache;
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

    public Boolean isAttachment() {
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
        return convertToCache(value, record, validate);
    }

    @Override
    public Object convertToCache(Object value, Records rec, boolean validate) {
        if (value == null) {
            if (validate && isRequired()) {
                throw new ValidationException(rec.l10n("%s 不能为空", getLabel()));
            }
            return null;
        }
        if (value instanceof byte[]) {
            return value;
        }
        if (value instanceof String) {
            return java.util.Base64.getDecoder().decode((String) value);
        }
        if (validate) {
            throw new ValidationException(rec.l10n("%s不支持转换成byte[]", value));
        }
        return value;
    }

    @Override
    public Object convertToRecord(Object value, Records rec) {
        if (value == null) {
            return isRequired() ? new byte[0] : null;
        }
        if (value instanceof String) {
            return java.util.Base64.getDecoder().decode((String) value);
        }
        return value;
    }

    @Override
    public void read(Records records) {
        Map<String, Object> data = new HashMap<>();
        Records res = records.getEnv().get("ir.attachment").find(Criteria.equal("res_model", this.getModelName())
                .and(Criteria.equal("res_field", this.getName()))
                .and(Criteria.in("res_id", Arrays.asList(records.getIds()))));

        for (Records item : res) {
            data.put((String) item.get("res_id"), item.get("datas"));
        }
        Cache cache = records.getEnv().getCache();
        for (Records r : records) {
            Object cacheData = data.get(r.getId());
            cache.set(r, this, cacheData);
        }
    }

    @Override
    public void create(List<Tuple<Records, Object>> recordValues) {
        if (recordValues == null) {
            return;
        }
        Environment env = recordValues.get(0).getItem1().getEnv();
        Records irAttachment = env.get("ir.attachment").withContext("binary_field_real_user", env.getUser());
        List<Map<String, Object>> valuesList = new ArrayList<Map<String, Object>>();
        for (Tuple<Records, Object> tuple : recordValues) {
            if (tuple.getItem2() != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", this.getName());
                item.put("res_model", this.getModelName());
                item.put("res_field", this.getName());
                item.put("res_id", tuple.getItem1().getId());
                item.put("type", "binary");
                item.put("datas", tuple.getItem2());
                valuesList.add(item);
            }
        }
        if (valuesList.size() > 0) {
            irAttachment.createBatch(valuesList);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Records write(Records records, Object value) {
        if (!this.attachment) {
            return super.write(records, value);
        }
        if (value instanceof String) {
            value = ((String) value).getBytes(StandardCharsets.UTF_8);

            // value = java.util.Base64.getDecoder().decode((String) value);
        }
        Cache cache = records.getEnv().getCache();
        Object cacheValue = convertToCache(value, records, true);
        records = cache.getRecordsDifferentFrom(records, this, cacheValue);
        if (!records.any()) {
            return records;
        }
        Records notNull = null;
        if (this.isStore()) {
            notNull = cache.getRecordsDifferentFrom(records, this, cacheValue);
        }
        Object[] values = new Object[records.size()];
        Arrays.fill(values, cacheValue);
        cache.update(records, this, Arrays.asList(values));
        if (this.isStore() && records.any()) {
            Records atts = records.getEnv().get("ir.attachment");
            if (notNull != null) {
                atts = atts.find(Criteria.equal("res_model", this.getModelName())
                        .and(Criteria.equal("res_field", this.getName()))
                        .and(Criteria.in("res_id", Arrays.asList(records.getIds()))));
            }
            if (value != null) {
                if (!atts.any()) {
                    // 创建，好像有点问题
                    List<Map<String, Object>> valuesList = new ArrayList<Map<String, Object>>();
                    Map<String, Object> createValues = new HashMap<>();
                    createValues.put("name", this.getName());
                    createValues.put("res_model", this.getModelName());
                    createValues.put("res_field", this.getName());
                    createValues.put("res_id", records.getId());
                    createValues.put("type", "binary");
                    createValues.put("datas", value);
                    valuesList.add(createValues);
                    atts.createBatch(valuesList);
                } else {
                    // 更新
                    Map<String, Object> valuesMap = new HashMap<>(8);
                    if (!(value instanceof HashMap)) {
                        valuesMap.put("data", value);
                    } else {
                        valuesMap = (Map<String, Object>) value;
                    }

                    atts.call("SetAttachmentData", atts, valuesMap);
                }

            } else {
                atts.delete();
            }
        }
        return records;
    }
}
