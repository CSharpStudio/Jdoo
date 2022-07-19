package org.jdoo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import org.jdoo.Records;
import org.jdoo.core.MetaField;
import org.jdoo.exceptions.CacheMissException;
import org.jdoo.utils.ArrayUtils;

/**
 * 加载到内存的模型记录数据
 * 
 * @author lrz
 */
public class Cache {
    Map<MetaField, Map<String, Object>> data = new HashMap<>();

    Map<String, Object> getFieldCache(Records rec, MetaField field) {
        Map<String, Object> cache = data.getOrDefault(field, Collections.emptyMap());
        // TODO field_depends_context
        return cache;
    }

    Map<String, Object> setFieldCache(Records rec, MetaField field) {
        Map<String, Object> cache;
        if (data.containsKey(field)) {
            cache = data.get(field);
        } else {
            cache = new HashMap<>(16);
            data.put(field, cache);
        }
        // TODO field_depends_context
        return cache;
    }

    public boolean contains(Records rec, MetaField field) {
        return getFieldCache(rec, field).containsKey(rec.getId());
    }

    public Object get(Records rec, MetaField field) {
        Map<String, Object> cache = getFieldCache(rec, field);
        if (cache.containsKey(rec.getId())) {
            return cache.get(rec.getId());
        }
        throw new CacheMissException(String.format("找不到%s.%s的值", rec, field.getName()));
    }

    public Object get(Records rec, MetaField field, Object fallback) {
        Map<String, Object> cache = getFieldCache(rec, field);
        if (cache.containsKey(rec.getId())) {
            return cache.get(rec.getId());
        }
        return fallback;
    }

    public void set(Records rec, MetaField field, Object value) {
        Map<String, Object> cache = setFieldCache(rec, field);
        cache.put(rec.getId(), value);
    }

    public void update(Records rec, MetaField field, Collection<Object> values) {
        Map<String, Object> cache = setFieldCache(rec, field);
        for (List<Object> idValue : ArrayUtils.zip(Arrays.asList(rec.getIds()), values)) {
            cache.put((String) idValue.get(0), idValue.get(1));
        }
    }

    public void remove(Records rec, MetaField field) {
        Map<String, Object> cache = setFieldCache(rec, field);
        cache.remove(rec.getId());
    }

    public Collection<Object> getValues(Records rec, MetaField field) {
        List<Object> values = new ArrayList<>();
        Map<String, Object> cache = getFieldCache(rec, field);
        for (String id : rec.getIds()) {
            values.add(cache.get(id));
        }
        return values;
    }

    public Records getRecordsDifferentFrom(Records rec, MetaField field, Object value) {
        Map<String, Object> cache = getFieldCache(rec, field);
        List<String> ids = new ArrayList<>();
        for (String id : rec.getIds()) {
            if (cache.containsKey(id)) {
                Object val = cache.get(id);
                if (!Objects.equals(val, value)) {
                    ids.add(id);
                }
            } else {
                ids.add(id);
            }
        }
        return rec.browse(ids);
    }

    public List<MetaField> getFields(Records rec) {
        List<MetaField> result = new ArrayList<>();
        for (Entry<String, MetaField> f : rec.getMeta().getFields().entrySet()) {
            if (!"id".equals(f.getKey()) && getFieldCache(rec, f.getValue()).containsKey(rec.getId())) {
                result.add(f.getValue());
            }
        }
        return result;
    }

    public Records getRecords(Records rec, MetaField field) {
        Map<String, Object> cache = getFieldCache(rec, field);
        return rec.browse(cache.keySet());
    }

    public Collection<String> getMissingIds(Records rec, MetaField field) {
        Map<String, Object> cache = getFieldCache(rec, field);
        List<String> ids = new ArrayList<>();
        for (String id : rec.getIds()) {
            if (!cache.containsKey(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    public void invalidate() {
        data.clear();
    }

    public void invalidate(List<Tuple<MetaField, String[]>> spec) {
        for (Tuple<MetaField, String[]> tuple : spec) {
            if (tuple.getItem2().length > 0) {
                data.remove(tuple.getItem1());
            } else {
                Map<String, Object> fieldCache = data.get(tuple.getItem1());
                if (fieldCache != null) {
                    for (String id : tuple.getItem2()) {
                        fieldCache.remove(id);
                    }
                }
            }
        }
    }
}
