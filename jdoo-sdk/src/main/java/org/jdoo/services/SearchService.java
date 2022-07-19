package org.jdoo.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdoo.Criteria;
import org.jdoo.ParamIn;
import org.jdoo.ParamOut;
import org.jdoo.Service;
import org.jdoo.core.MetaModel;
import org.jdoo.util.KvMap;
import org.jdoo.utils.Utils;

/**
 * 查询并读取模型数据服务
 * 
 * @author lrz
 */
public class SearchService extends Service {
    @Override
    protected void execute(ParamIn in, ParamOut out) {
        SearchReadParam param = in.getArgs(SearchReadParam.class);
        boolean getNext = param.nextTest && param.limit != null && param.limit > 0;
        Integer limit = null;
        if (getNext) {
            limit = param.limit + 1;
        } else {
            limit = param.limit;
        }
        List<Map<String, Object>> values = in.getEnv().get(in.getModel()).search(param.fields, param.criteria,
                param.offset, limit, param.order);
        Map<String, Object> data = new HashMap<>(2);
        if (getNext) {
            if (values.size() > param.limit) {
                values.remove(values.size() - 1);
                data.put("hasNext", true);
            } else {
                data.put("hasNext", false);
            }
        }
        data.put("values", values);
        out.putData(data);
    }

    @Override
    public Map<String, ApiDoc> getArgsDoc(MetaModel meta) {
        List<String> fields = meta.getFields().entrySet().stream().filter(e -> !e.getValue().isAuto())
                .map(m -> m.getKey()).collect(Collectors.toList());
        Object criteria = meta.findField("name") == null ? Arrays.asList(Arrays.asList("id", "=", "01o5ryusb9lhc"))
                : Arrays.asList(Arrays.asList("name", "=", "test"));
        return new HashMap<String, ApiDoc>(1) {
            {
                put("criteria", new ApiDoc("过滤条件", criteria));
                put("fields", new ApiDoc("字段集合", fields));
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
        Object values = meta.getFields().entrySet().stream().filter(e -> !e.getValue().isAuto())
                .collect(Collectors.toMap(e -> e.getKey(), e -> Utils.getExampleValue(e.getValue(), false)));
        example.put("values", values);
        example.put("hasNext", true);
        return new ApiDoc("values:字段值结果, hasNext:是否有下一页", example);
    }
}

@SuppressWarnings("all")
class SearchReadParam {
    public List<String> fields;
    public Criteria criteria;
    public Integer limit;
    public Integer offset;
    public String order;
    public boolean nextTest;
}