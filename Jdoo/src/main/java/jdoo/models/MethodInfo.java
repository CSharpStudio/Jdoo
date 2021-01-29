package jdoo.models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class MethodInfo {
    private Method method;
    private MetaModel meta;
    private BaseModel instance;

    public MethodInfo(MetaModel meta, Method method) {
        this.method = method;
        this.meta = meta;
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

    public Object invoke(Object[] args)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        BaseModel obj = getInstance();
        return method.invoke(obj, args);
    }

    protected BaseModel getInstance() {
        if (instance == null) {
            Class<?> clazz = method.getDeclaringClass();
            Enhancer e = new Enhancer();
            e.setSuperclass(clazz);
            e.setCallback(ModelInterceptor.Interceptor);
            instance = (BaseModel) e.create();
            instance.meta = meta;
        }
        return instance;
    }

    static class ModelInterceptor implements MethodInterceptor {
        public static final ModelInterceptor Interceptor = new ModelInterceptor();

        @Override
        public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            BaseModel model = (BaseModel) target;
            MetaModel meta = model.meta;
            MethodInfo m = meta.findOverrideMethod(method);
            if (m != null) {
                return m.invoke(args);
            }
            Object result = proxy.invokeSuper(target, args);
            return result;
        }
    }
}
