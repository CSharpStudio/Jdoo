package org.jdoo.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdoo.ParamIn;
import org.jdoo.ParamOut;
import org.jdoo.Service;
import org.jdoo.core.MetaModel;
import org.jdoo.utils.Utils;

/**
 * 更新模型服务
 * 
 * @author lrz
 */
public class UpdateService extends Service {
    @Override
    protected void execute(ParamIn in, ParamOut out) {
        UpdateParam param = in.getArgs(UpdateParam.class);
        in.getEnv().get(in.getModel(), param.ids).update(param.values);
        out.putData(true);
    }
    

    @Override
    public Map<String, ApiDoc> getArgsDoc(MetaModel meta) {        
        Object values = meta.getFields().entrySet().stream().filter(e -> !e.getValue().isAuto())
                .collect(Collectors.toMap(e -> e.getKey(), e -> Utils.getExampleValue(e.getValue(), false)));
        return new HashMap<String, ApiDoc>(1) {
            {
                put("ids", new ApiDoc("id集合",  Arrays.asList("01m5kvc8dzhfk", "01m5kvc8dzhfl")));
                put("values", new ApiDoc("字段/值", values));
            }
        };
    }

    @Override
    public ApiDoc getResultDoc(MetaModel meta) {
        return new ApiDoc("", true);
    }
}

class UpdateParam {
    public String[] ids;
    public Map<String, Object> values;
}