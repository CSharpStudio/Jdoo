package org.jdoo.core;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jdoo.ParamIn;
import org.jdoo.ParamOut;
import org.jdoo.utils.Utils;

/**
 * 服务基类
 * 
 * @author lrz
 */
public class BaseService {
    String name;
    String description;
    String label;
    String auth;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAuth() {
        if (StringUtils.isEmpty(auth)) {
            auth = name;
        }
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        description = desc;
    }

    public Map<String, ApiDoc> getArgsDoc(MetaModel meta) {
        return null;
    }

    public ApiDoc getResultDoc(MetaModel meta) {
        return null;
    }

    protected void execute(ParamIn in, ParamOut out) {

    }

    protected void onError(ParamIn in, ParamOut out) {

    }

    public void executeService(ParamIn in, ParamOut out) {

        if (out.getErrors().isEmpty()) {
            execute(in, out);
        } else {
            onError(in, out);
        }
    }

    public class ApiDoc {
        String type;
        Object example;
        String description;

        public ApiDoc(String description, Object example, String type) {
            this.type = type;
            this.description = description;
            this.example = example;
        }

        public ApiDoc(String description, Object example) {
            this.type = Utils.getJsonType(example.getClass());
            this.description = description;
            this.example = example;
        }

        public String getType() {
            return type;
        }

        public Object getExample() {
            return example;
        }

        public String getDescription() {
            return description;
        }
    }
}
