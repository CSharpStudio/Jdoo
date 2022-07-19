package org.jdoo.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdoo.ParamIn;
import org.jdoo.ParamOut;
import org.jdoo.Service;
import org.jdoo.core.MetaModel;
import org.jdoo.utils.Utils;

/**
 * 读取模型数据服务
 * 
 * @author lrz
 */
public class ReadService extends Service {
    @Override
    protected void execute(ParamIn in, ParamOut out) {
        ReadParam param = in.getArgs(ReadParam.class);
        List<Map<String, Object>> values = in.getEnv().get(in.getModel(), param.ids).read(param.fields);
        out.putData(values);
    }

    @Override
    public Map<String, ApiDoc> getArgsDoc(MetaModel meta) {
        List<String> fields = meta.getFields().entrySet().stream().filter(e -> !e.getValue().isAuto())
                .map(m -> m.getKey()).collect(Collectors.toList());
        return new HashMap<String, ApiDoc>(1) {
            {
                put("ids", new ApiDoc("id集合", Arrays.asList("01m5kvc8dzhfk", "01m5kvc8dzhfl")));
                put("fields", new ApiDoc("字段集合", fields));
            }
        };
    }

    @Override
    public ApiDoc getResultDoc(MetaModel meta) {
        Object example = Arrays.asList(meta.getFields().entrySet().stream().filter(e -> !e.getValue().isAuto())
                .collect(Collectors.toMap(e -> e.getKey(), e -> Utils.getExampleValue(e.getValue(), true))));
        return new ApiDoc("字段/值列表", example);
    }
}

class ReadParam {
    public List<String> ids;
    public List<String> fields;
}
