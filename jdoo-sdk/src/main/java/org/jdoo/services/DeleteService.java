package org.jdoo.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jdoo.ParamIn;
import org.jdoo.ParamOut;
import org.jdoo.Service;
import org.jdoo.core.MetaModel;

/**
 * 删除模型服务
 * 
 * @author lrz
 */
public class DeleteService extends Service {
    @Override
    protected void execute(ParamIn in, ParamOut out) {
        DeleteParam param = in.getArgs(DeleteParam.class);
        in.getEnv().get(in.getModel(), param.ids).delete();
        out.putData(true);
    }

    @Override
    public Map<String, ApiDoc> getArgsDoc(MetaModel meta) {
        return new HashMap<String, ApiDoc>(1) {
            {
                put("ids", new ApiDoc("id集合", Arrays.asList("01m5kvc8dzhfk", "01m5kvc8dzhfl")));
            }
        };
    }

    @Override
    public ApiDoc getResultDoc(MetaModel meta) {
        return new ApiDoc("", true);
    }
}

class DeleteParam {
    public String[] ids;
}