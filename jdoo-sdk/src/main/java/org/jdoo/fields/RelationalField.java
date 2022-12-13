package org.jdoo.fields;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.jdoo.Criteria;
import org.jdoo.ICriteria;
import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.core.MetaModel;

/**
 * 关联
 * 
 * @author lrz
 */
public class RelationalField<T extends BaseField<T>> extends BaseField<T> {

    @JsonIgnore
    ICriteria criteria;
    @JsonIgnore
    Boolean autoJoin = false;
    @Related
    Map<String, Object> context;
    /** corresponding model name */
    @Related
    String comodelName;

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

    /**
     * 指定查询条件
     * 
     * @param criteria
     * @return
     */
    @SuppressWarnings("unchecked")
    public T criteria(ICriteria criteria) {
        args.put(Constants.CRITERIA, criteria);
        return (T) this;
    }

    /**
     * 指定查询条件
     * 
     * @param method
     * @return
     */
    @SuppressWarnings("unchecked")
    public T criteria(String method) {
        args.put(Constants.CRITERIA, ICriteria.method(method));
        return (T) this;
    }

    /**
     * 指定查询条件
     * 
     * @param criteria
     * @return
     */
    @SuppressWarnings("unchecked")
    public T criteria(Criteria criteria) {
        args.put(Constants.CRITERIA, ICriteria.criteria(criteria));
        return (T) this;
    }

    public Criteria getCriteria(Records model) {
        if (criteria != null) {
            return criteria.get(model);
        }
        return new Criteria();
    }

    @Override
    protected void setName(MetaModel model, String name) {
        super.setName(model, name);
        if (!model.getRegistry().contains(comodelName)) {
            logger.warn("字段[{}]使用了未知引用模型{}", this, comodelName);
            comodelName = "_unknown";
        }
    }
}
