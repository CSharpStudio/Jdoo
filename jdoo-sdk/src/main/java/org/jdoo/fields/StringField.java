package org.jdoo.fields;

import java.util.Map;

import org.jdoo.Records;
import org.jdoo.core.MetaModel;

/**
 * 字符串
 * 
 * @author lrz
 */
public class StringField<T extends BaseField<T>> extends BaseField<T> {
    Boolean translate;

    public StringField() {
        relatedAttributes.add("translate");
    }

    public boolean getTranslate() {
        if (translate == null) {
            return false;
        }
        return translate;
    }

    @SuppressWarnings("unchecked")
    public T translate() {
        args.put("translate", true);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T translate(boolean translate) {
        args.put("translate", translate);
        return (T) this;
    }

    @Override
    protected Map<String, Object> getAttrs(MetaModel model, String name) {
        Map<String, Object> attrs = super.getAttrs(model, name);
        Boolean tran = (Boolean) attrs.getOrDefault("translate", false);
        // do not prefetch complex translated fields by default
        if (tran) {
            attrs.put("prefetch", attrs.getOrDefault("prefetch", false));
        }
        return attrs;
    }

    @Override
    public Records write(Records records, Object value) {
        // TODO
        return super.write(records, value);
    }
}
