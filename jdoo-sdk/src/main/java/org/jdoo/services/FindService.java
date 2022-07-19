package org.jdoo.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jdoo.Criteria;
import org.jdoo.ParamIn;
import org.jdoo.ParamOut;
import org.jdoo.Records;
import org.jdoo.Service;
import org.jdoo.core.MetaModel;
import org.jdoo.util.KvMap;

/**
 * 查询模型服务
 * 
 * @author lrz
 */
public class FindService extends Service {
    @Override
    protected void execute(ParamIn in, ParamOut out) {
        SearchParam param = in.getArgs(SearchParam.class);
        boolean getNext = param.nextTest && param.limit != null && param.limit > 0;
        Integer limit = getNext ? param.limit + 1 : param.limit;
        Records rec = in.getEnv().get(in.getModel()).find(param.criteria, param.offset, limit, param.order);
        Map<String, Object> data = new HashMap<>(2);
        String[] ids = rec.getIds();
        if (getNext) {
            if (ids.length > param.limit) {
                ids = Arrays.copyOf(ids, param.limit);
                data.put("hasNext", true);
            } else {
                data.put("hasNext", false);
            }
        }
        data.put("ids", ids);
        out.putData(data);
    }

    @Override
    public Map<String, ApiDoc> getArgsDoc(MetaModel meta) {
        Object criteria = meta.findField("name") == null ? Arrays.asList(Arrays.asList("id", "=", "01o5ryusb9lhc"))
                : Arrays.asList(Arrays.asList("name", "=", "test"));
        return new HashMap<String, ApiDoc>(1) {
            {
                put("criteria", new ApiDoc("过滤条件", criteria));
                put("limit", new ApiDoc("限制记录数", 10));
                put("offset", new ApiDoc("记录偏移量", 0));
                put("order", new ApiDoc("排序条件", "id desc"));
                put("nextTest", new ApiDoc("是否验证下一页", true));
            }
        };
    }

    @Override
    public ApiDoc getResultDoc(MetaModel meta) {
        KvMap example = new KvMap();
        example.put("ids", Arrays.asList("01m5kvc8dzhfk", "01m5kvc8dzhfl"));
        example.put("hasNext", true);
        return new ApiDoc("ids:id集合, hasNext:是否有下一页", example);
    }
}

class SearchParam {
    public Criteria criteria;
    public Integer limit;
    public Integer offset;
    public String order;
    public boolean nextTest;
}