package org.jdoo.services;

import java.util.Map;
import java.util.stream.Collectors;

import org.jdoo.ParamIn;
import org.jdoo.ParamOut;
import org.jdoo.Records;
import org.jdoo.Service;
import org.jdoo.core.MetaField;
import org.jdoo.core.MetaModel;
import org.jdoo.utils.Utils;

/**
 * 创建模型服务
 * 
 * @author lrz
 */
public class CreateService extends Service {
    @Override
    protected void execute(ParamIn in, ParamOut out) {
        Records rec = in.getEnv().get(in.getModel()).create(in.getArgs());
        out.putData(rec.getId());
    }

    @Override
    public Map<String, ApiDoc> getArgsDoc(MetaModel meta) {
        return meta.getFields().entrySet().stream().filter(e -> !e.getValue().isAuto())
                .collect(Collectors.toMap(e -> e.getKey(),
                        e -> new ApiDoc(getHelp(e.getValue()), Utils.getExampleValue(e.getValue(), false))));
    }

    String getHelp(MetaField field) {
        String help = field.getHelp();
        if (help == null) {
            help = "";
        }
        return help;
    }

    @Override
    public ApiDoc getResultDoc(MetaModel meta) {
        return new ApiDoc("新记录id", "01m5kvc8dzhfk");
    }
}