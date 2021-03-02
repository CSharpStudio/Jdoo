package jdoo.models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.lang.Nullable;

import jdoo.apis.api;
import jdoo.exceptions.InvocationException;
import jdoo.util.Default;
import jdoo.util.Kwargs;
import jdoo.util.Tuple;
import jdoo.util.TypeUtils;
import jdoo.util.Utils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class MethodInfo {
    static ThreadLocal<Boolean> callingThis = new ThreadLocal<>();
    private Method method;
    private MetaModel meta;
    private BaseModel proxy;
    private List<ParameterInfo> parameters;
    private Returns returns;
    private String api;

    public MethodInfo(MetaModel meta, Method method) {
        this.method = method;
        this.meta = meta;
        parameters = new ArrayList<>();
        for (Parameter p : method.getParameters()) {
            parameters.add(new ParameterInfo(p));
        }
        api.returns spec = method.getAnnotation(api.returns.class);
        if (spec != null) {
            returns = new Returns(spec);
        }
        if (method.getAnnotation(api.model.class) != null) {
            api = "model";
        } else if (method.getAnnotation(api.model_create_multi.class) != null) {
            api = "model_create";
        }
    }

    public Method getMethod() {
        return method;
    }

    public MetaModel getMeta() {
        return meta;
    }

    public void setMeta(MetaModel meta) {
        this.meta = meta;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public String api() {
        return api;
    }

    public Returns returns() {
        return returns;
    }

    public Object invoke(Object[] args, @Nullable Kwargs kwargs)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        BaseModel obj = getProxy();
        args = getArgs(args, kwargs);
        return method.invoke(obj, args);
    }

    public Object invoke(RecordSet self, Collection<Object> args, @Nullable Kwargs kwargs) {
        BaseModel obj = getProxy();
        Object[] $args = getArgs(self, args, kwargs);
        try {
            return method.invoke(obj, $args);
        } catch (Exception e) {
            throw new InvocationException(String.format("Method %s, args: %s, invoke error", toString(), $args), e);
        }
    }

    Object invokeThis(Object[] args)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        MethodInfo.callingThis.set(true);
        return invoke(args, null);
    }

    Object[] getArgs(RecordSet self, Collection<Object> args, @Nullable Kwargs kwargs) throws IllegalArgumentException {
        Map<String, Object> kw = kwargs == null ? Collections.emptyMap() : kwargs;
        if (args.size() > parameters.size() - 1) {
            List<Object> $args = Utils.asList(self);
            $args.addAll(args);
            throw new IllegalArgumentException(
                    String.format("Method %s get too much args: %s, kwargs: %s", toString(), $args, kw));
        }
        Object[] params = new Object[parameters.size()];
        params[0] = self;
        int i = 1;
        for (Object arg : args) {
            params[i++] = arg;
        }
        while (i < parameters.size()) {
            ParameterInfo p = parameters.get(i);
            String name = p.getParameter().getName();
            if (kw.containsKey(name)) {
                params[i] = kw.get(name);
            } else {
                if (!p.isOptional()) {
                    throw new IllegalArgumentException(String.format("Method %s get too few args: %s, kwargs: %s",
                            toString(), new Tuple<>(args), kw));
                }
                params[i] = p.getDefaultValue();
            }
            i++;
        }
        return params;
    }

    Object[] getArgs(Object[] args, @Nullable Kwargs kwargs) throws IllegalArgumentException {
        if (args.length >= parameters.size()) {
            return args;
        }
        Map<String, Object> kw = kwargs == null ? Collections.emptyMap() : kwargs;
        Object[] params = new Object[parameters.size()];
        System.arraycopy(args, 0, params, 0, args.length);
        for (int i = args.length; i < parameters.size(); i++) {
            ParameterInfo p = parameters.get(i);
            String name = p.getParameter().getName();
            if (kw.containsKey(name)) {
                params[i] = kw.get(name);
            } else {
                if (!p.isOptional()) {
                    throw new IllegalArgumentException(
                            String.format("Method %s.%s(%s) get too few args: %s, kwargs: %s", meta.name(),
                                    method.getName(), parameters, new Tuple<>(args), kw));
                }
                params[i] = p.getDefaultValue();
            }
        }
        return params;
    }

    public boolean isParameterMatch(Object[] args, @Nullable Kwargs kwargs) {
        Map<String, Object> kw = kwargs == null ? Collections.emptyMap() : kwargs;
        if (args.length > parameters.size()) {
            return false;// too much args
        }
        for (int i = args.length; i < parameters.size(); i++) {
            ParameterInfo p = parameters.get(i);
            String name = p.parameter.getName();
            if (!p.isOptional() && !kw.containsKey(name)) {
                return false;// parameter is not optional and not in args and kwargs
            }
            if (kw.containsKey(name) && !TypeUtils.isInstance(p.parameter.getType(), kw.get(name))) {
                return false;// value type not match
            }
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Parameter p = parameters.get(i).getParameter();
            String name = p.getName();
            if (kw.containsKey(name)) {
                return false;// both args and kwargs has the parameter
            }
            if (arg != null && !TypeUtils.isInstance(p.getType(), arg)) {
                return false;// value type not match
            }
        }

        return true;
    }

    public boolean isParameterMatch(Class<?>[] paras) {
        if (parameters.size() != paras.length)
            return false;
        for (int i = 0; i < paras.length; i++) {
            if (parameters.get(i).getParameter().getType() != paras[i])
                return false;
        }
        return true;
    }

    protected BaseModel getProxy() {
        if (proxy == null) {
            Class<?> clazz = method.getDeclaringClass();
            Enhancer e = new Enhancer();
            e.setSuperclass(clazz);
            e.setCallback(ModelInterceptor.Interceptor);
            proxy = (BaseModel) e.create();
            proxy.meta = meta;
        }
        return proxy;
    }

    @Override
    public String toString() {
        return String.format("%s::%s(%s)", meta.name(), method.getName(), parameters);
    }

    static class ModelInterceptor implements MethodInterceptor {
        public static final ModelInterceptor Interceptor = new ModelInterceptor();

        @Override
        public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Boolean callingThis = MethodInfo.callingThis.get();
            if (callingThis != null && callingThis) {
                MethodInfo.callingThis.set(false);
            } else {
                BaseModel model = (BaseModel) target;
                MetaModel meta = model.meta;
                MethodInfo m = meta.findOverrideMethod(method);
                if (m != null) {
                    return m.invokeThis(args);
                }
            }
            Object result = proxy.invokeSuper(target, args);
            return result;
        }
    }

    public class ParameterInfo {
        private Parameter parameter;
        private boolean optional;
        private Object defaultValue;

        public ParameterInfo(Parameter p) {
            parameter = p;
            Default $default = p.getAnnotation(Default.class);
            if ($default != null) {
                optional = true;
                String str = $default.value();
                if (!str.isEmpty()) {
                    defaultValue = TypeUtils.parse(str, p.getType());
                }
            }
        }

        public Parameter getParameter() {
            return parameter;
        }

        public boolean isOptional() {
            return optional;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String toString() {
            String str = parameter.getType().getName() + " " + parameter.getName();
            if (optional) {
                if (defaultValue == null) {
                    str += "=null";
                } else {
                    str += "=" + defaultValue;
                }
            }
            return str;
        }
    }

    public class Returns {
        api.returns spec;
        String model;
        Function<RecordSet, Object> downgrade;
        Function<RecordSet, Object> upgrade;

        public String model() {
            return model;
        }

        public Function<RecordSet, Object> downgrade() {
            return downgrade;
        }

        public Function<RecordSet, Object> upgrade() {
            return upgrade;
        }

        public Returns(api.returns spec) {
            model = spec.value();
        }
    }
}
