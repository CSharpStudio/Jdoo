package org.jdoo.fields;

import java.util.HashMap;
import java.util.Map;

import org.jdoo.Criteria;
import org.jdoo.ICriteria;
import org.jdoo.Records;
import org.jdoo.core.MetaModel;

/**
 * 关联
 * 
 * @author lrz
 */
public class RelationalField<T extends BaseField<T>> extends BaseField<T> {
    /** 删除模式 */
    public enum DeleteMode {
        /** 设置为空 */
        SetNull,
        /** 级联删除 */
        Cascade,
        /** 限制删除 */
        Restrict,
    }

    ICriteria criteria;
    Map<String, Object> context;

    public Map<String, Object> getContext() {
        if (context == null) {
            context = new HashMap<>();
        }
        return context;
    }

    @SuppressWarnings("unchecked")
    public T context(Map<String, Object> ctx) {
        args.put("context", ctx);
        return (T) this;
    }

    /** correlation model name */
    String comodelName;
    Boolean autoJoin = false;

    public RelationalField() {
        relatedAttributes.add("comodelName");
    }

    public Boolean getAutoJoin() {
        return autoJoin;
    }

    public String getComodel() {
        return comodelName;
    }

    @SuppressWarnings("unchecked")
    public T autoJoin() {
        args.put("autoJoin", true);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T autoJoin(boolean autoJoin) {
        args.put("autoJoin", autoJoin);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T criteria(ICriteria criteria) {
        args.put("criteria", criteria);
        return (T) this;
    }

    public Criteria getCriteria(Records model) {
        if (criteria != null) {
            return criteria.get(model);
        }
        return new Criteria();
    }

    @Override
    protected void setupBase(MetaModel model, String name) {
        super.setupBase(model, name);
        if (!model.getRegistry().contains(comodelName)) {
            logger.warn("字段[{}]使用了未知引用模型{}", this, comodelName);
            comodelName = "_unknown";
        }
    }
}
