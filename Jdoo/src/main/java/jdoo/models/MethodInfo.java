package jdoo.models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import jdoo.exceptions.InvocationException;
import jdoo.util.Default;
import jdoo.util.TypeUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class MethodInfo {
    static ThreadLocal<Boolean> callingThis = new ThreadLocal<>();
    private Method method;
    private MetaModel meta;
    private BaseModel proxy;
    private List<ParameterInfo> parameters;

    public MethodInfo(MetaModel meta, Method method) {
        this.method = method;
        this.meta = meta;
        parameters = new ArrayList<>();
        for (Parameter p : method.getParameters()) {
            parameters.add(new ParameterInfo(p));
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

    public Object invoke(Object[] args)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        BaseModel obj = getProxy();
        args = getArgs(args);
        return method.invoke(obj, args);
    }

    public Object invokeThis(Object[] args)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        MethodInfo.callingThis.set(true);
        return invoke(args);
    }

    Object[] getArgs(Object[] args) throws IllegalArgumentException {
        if (args.length > parameters.size()) {
            return args;
        }
        Object[] params = new Object[parameters.size()];
        System.arraycopy(args, 0, params, 0, args.length);
        for (int i = args.length; i < parameters.size(); i++) {
            ParameterInfo p = parameters.get(i);
            if (!p.isOptional()) {
                throw new IllegalArgumentException(String.format("Method %s.%s(%s) get %s args", meta.name(),
                        method.getName(), parameters, args.length));
            }
            params[i] = p.getDefaultValue();
        }
        return params;
    }

    public boolean isParameterMatch(Object[] args) {
        if (args.length > parameters.size()) {
            return false;
        }
        for (int i = args.length; i < parameters.size(); i++) {
            if (!parameters.get(i).isOptional()) {
                return false;
            }
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg != null && !TypeUtils.isInstance(parameters.get(i).getParameter().getType(), arg)) {
                return false;
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
}
