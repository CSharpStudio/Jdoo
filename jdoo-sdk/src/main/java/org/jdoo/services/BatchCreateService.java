package org.jdoo.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdoo.ParamIn;
import org.jdoo.ParamOut;
import org.jdoo.Records;
import org.jdoo.Service;
import org.jdoo.core.MetaModel;
import org.jdoo.utils.Utils;

/**
 * 批量创建模型服务
 * 
 * @author lrz
 */
public class BatchCreateService extends Service {
    @Override
    protected void execute(ParamIn in, ParamOut out) {
        Records rec = in.getEnv().get(in.getModel()).createBatch(in.getArgs(BatchCreateParam.class).values);
        out.putData(rec.getIds());
    }

    @Override
    public Map<String, ApiDoc> getArgsDoc(MetaModel meta) {
        Object values = Arrays.asList(meta.getFields().entrySet().stream().filter(e -> !e.getValue().isAuto())
                .collect(Collectors.toMap(e -> e.getKey(), e -> Utils.getExampleValue(e.getValue(), false))));
        return new HashMap<String, ApiDoc>(1) {
            {
                put("values", new ApiDoc("字段/值", values));
            }
        };
    }

    @Override
    public ApiDoc getResultDoc(MetaModel meta) {
        return new ApiDoc("新记录id列表", Arrays.asList("01m5kvc8dzhfk", "01m5kvc8dzhfl"));
    }
}

class BatchCreateParam {
    public List<Map<String, Object>> values;
}