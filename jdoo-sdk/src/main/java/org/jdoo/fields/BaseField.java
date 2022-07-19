package org.jdoo.fields;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jdoo.Callable;
import org.jdoo.Default;
import org.jdoo.Field;
import org.jdoo.ICriteria;
import org.jdoo.core.Constants;

import org.apache.commons.lang3.StringUtils;

/**
 * 字段基类
 * 
 * @author lrz
 */
@SuppressWarnings("unchecked")
public class BaseField<T extends BaseField<T>> extends Field {
    /**
     * 存于数据库
     * 
     * @return
     */
    public T store() {
        args.put(Constants.STORE, true);
        return (T) this;
    }

    /**
     * 是否存于数据库
     * 
     * @param store
     * @return
     */
    public T store(boolean store) {
        args.put(Constants.STORE, store);
        return (T) this;
    }

    /**
     * 数据库索引
     * 
     * @return
     */
    public T index() {
        args.put(Constants.INDEX, true);
        return (T) this;
    }

    /**
     * 是否数据库索引
     * 
     * @param index
     * @return
     */
    public T index(boolean index) {
        args.put(Constants.INDEX, index);
        return (T) this;
    }

    /**
     * 复制模型时，复制字段
     * 
     * @return
     */
    public T copy() {
        args.put(Constants.COPY, true);
        return (T) this;
    }

    /**
     * 复制模型时，是否复制字段
     * 
     * @param copy
     * @return
     */
    public T copy(boolean copy) {
        args.put(Constants.COPY, copy);
        return (T) this;
    }

    /**
     * 依赖的其它字段
     * 
     * @param depends
     * @return
     */
    public T depends(String... depends) {
        args.put(Constants.DEPENDS, Arrays.asList(depends));
        return (T) this;
    }

    /**
     * 指定计算方法
     * 
     * @param compute
     * @return
     */
    public T compute(Callable compute) {
        args.put(Constants.COMPUTE, compute);
        return (T) this;
    }

    /**
     * 指定查询方法
     * 
     * @param criteria
     * @return
     */
    public T criteria(ICriteria criteria) {
        args.put(Constants.CRITERIA, criteria);
        return (T) this;
    }

    /**
     * 标题
     * 
     * @param label
     * @return
     */
    public T label(String caption) {
        args.put(Constants.LABEL, caption);
        return (T) this;
    }

    /**
     * 帮助说明
     * 
     * @param help
     * @return
     */
    public T help(String help) {
        args.put(Constants.HELP, help);
        return (T) this;
    }

    /**
     * 只读
     * 
     * @return
     */
    public T readonly() {
        args.put(Constants.READONLY, true);
        return (T) this;
    }

    /**
     * 是否只读
     * 
     * @param readonly
     * @return
     */
    public T readonly(boolean readonly) {
        args.put(Constants.READONLY, readonly);
        return (T) this;
    }

    /**
     * 必填
     * 
     * @return
     */
    public T required() {
        args.put(Constants.REQUIRED, true);
        return (T) this;
    }

    /**
     * 是否必填
     * 
     * @param required
     * @return
     */
    public T required(boolean required) {
        args.put(Constants.REQUIRED, required);
        return (T) this;
    }

    /**
     * 是否自动，默认自动字段:id, create_uid, create_data, update_uid, update_date
     */
    public T automatic(boolean automatic) {
        args.put(Constants.AUTOMATIC, automatic);
        return (T) this;
    }

    /**
     * 上下文
     * 
     * @param key
     * @param value
     * @return
     */
    public T context(String key, Object value) {
        Map<String, Object> ctx = new HashMap<>(1);
        ctx.put(key, value);
        args.put("context", ctx);
        return (T) this;
    }

    /**
     * 上下文
     * 
     * @param ctx
     * @return
     */
    public T context(Map<String, Object> ctx) {
        args.put("context", ctx);
        return (T) this;
    }

    /**
     * 默认值
     * 
     * @param defaultValue
     * @return
     */
    public T defaultValue(Default defaultValue) {
        args.put("defaultValue", defaultValue);
        return (T) this;
    }

    /**
     * 关联
     * 
     * @param related
     * @return
     */
    public T related(String related) {
        if (StringUtils.isNotBlank(related)) {
            args.put("related", related.split("\\."));
        }
        return (T) this;
    }

    /**
     * 需要授权
     * 
     * @return
     */
    public T auth() {
        args.put("auth", true);
        return (T) this;
    }

    /**
     * 是否需要授权
     * 
     * @return
     */
    public T auth(boolean auth) {
        args.put("auth", auth);
        return (T) this;
    }

    /**
     * 是否预加载
     */
    public T prefetch(boolean prefetch) {
        args.put("prefetch", prefetch);
        return (T) this;
    }
}
