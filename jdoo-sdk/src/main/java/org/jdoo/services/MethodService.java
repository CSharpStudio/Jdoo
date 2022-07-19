package org.jdoo.services;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdoo.Doc;
import org.jdoo.ParamIn;
import org.jdoo.ParamOut;
import org.jdoo.Records;
import org.jdoo.Service;
import org.jdoo.Model.ServiceMethod;
import org.jdoo.core.MetaModel;
import org.jdoo.utils.ThrowableUtils;
import org.jdoo.utils.Utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 方法服务
 * 
 * @author lrz
 */
public class MethodService extends Service {
    private Logger logger = LogManager.getLogger(MethodService.class);
    Method method;

    public MethodService(String name, ServiceMethod svc, Method method) {
        this.method = method;
        setName(name);
        setLabel(svc.label());
        setAuth(svc.auth());
        setDescription(svc.doc());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void execute(ParamIn in, ParamOut out) {
        Records rec = in.getEnv().get(in.getModel());
        Parameter[] params = method.getParameters();
        Map<String, Object> inArgs = new HashMap<>(in.getArgs());
        Object[] args = new Object[params.length];
        for (int i = 1; i < params.length; i++) {
            String argName = params[i].getName();
            args[i] = inArgs.remove(argName);
        }
        Object idsArg = inArgs.get("ids");
        if (idsArg instanceof List<?>) {
            args[0] = rec.browse((List<String>) idsArg);
        } else {
            args[0] = rec;
        }
        Object obj = rec.call(method.getName(), args);
        out.putData(obj);
    }

    @Override
    public Map<String, ApiDoc> getArgsDoc(MetaModel meta) {
        Parameter[] params = method.getParameters();
        Map<String, ApiDoc> result = new HashMap<String, ApiDoc>(params.length);
        ServiceMethod anno = method.getAnnotation(ServiceMethod.class);
        if (anno.records()) {
            result.put("ids", new ApiDoc("id集合", Arrays.asList("01ogtcwblyuio")));
        }
        boolean first = true;
        for (Parameter param : params) {
            if (first) {
                first = false;
                if (param.getType() == Records.class) {
                    continue;
                }
            }
            Doc doc = param.getAnnotation(Doc.class);
            Class<?> paramType = param.getType();
            Object example = "";
            String description = "";
            if (doc != null) {
                description = doc.doc();
                String value = doc.value();
                if (StringUtils.isNoneBlank(value)) {
                    ObjectMapper m = new ObjectMapper();
                    try {
                        example = m.readValue(value, paramType);
                    } catch (Exception e) {
                        logger.warn("方法[{}.{}]的参数[{}]示例[{}]反序列化失败：{}", method.getDeclaringClass().getName(),
                                method.getName(), param.getName(), value, ThrowableUtils.getDebug(e));
                        example = Utils.getExampleValue(paramType);
                    }
                } else {
                    example = Utils.getExampleValue(paramType);
                }
            } else {
                example = Utils.getExampleValue(paramType);
            }
            result.put(param.getName(), new ApiDoc(description, example, Utils.getJsonType(paramType)));
        }
        return result;
    }

    @Override
    public ApiDoc getResultDoc(MetaModel meta) {
        Doc doc = method.getAnnotation(Doc.class);
        String desc = doc != null ? doc.doc() : "";
        Class<?> methodType = method.getReturnType();
        Object example = "";
        if (methodType != Void.class) {
            String value = doc != null ? doc.value() : "";
            if (StringUtils.isNotEmpty(value)) {
                ObjectMapper m = new ObjectMapper();
                try {
                    example = m.readValue(value, methodType);
                } catch (Exception e) {
                    logger.warn("方法[{}.{}]的返回示例[{}]反序列化失败：{}", method.getDeclaringClass().getName(), method.getName(),
                            value, ThrowableUtils.getDebug(e));
                    example = Utils.getExampleValue(methodType);
                }
            }
        }
        return new ApiDoc(desc, example, Utils.getJsonType(methodType));
    }
}