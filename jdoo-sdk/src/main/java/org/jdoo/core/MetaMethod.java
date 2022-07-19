package org.jdoo.core;

import java.lang.reflect.Method;
import java.util.HashMap;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.jdoo.Records;
import org.jdoo.exceptions.ModelException;

import org.springframework.core.NamedThreadLocal;

/**
 * 方法元数据
 * 
 * @author lrz
 */
public class MetaMethod {
    private Method method;
    static HashMap<Class<?>, BaseModel> instances = new HashMap<>();

    public MetaMethod(Method method) {
        this.method = method;
    }

    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }

    public boolean isParameterMatch(Class<?>[] y) {
        Class<?>[] x = method.getParameterTypes();
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; i++) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    public Object invoke(Object[] args) {
        BaseModel obj = getInstance();
        try {
            return method.invoke(obj, args);
        } catch (Exception exc) {
            // Throwable cause = ThrowableUtils.getCause(exc);
            // if (cause instanceof SQLException && args.length > 1 && args[0] instanceof
            // Records) {
            // cause.printStackTrace();
            // Records rec = (Records) args[0];
            // SqlDialect sd = rec.getEnv().getCursor().getSqlDialect();
            // String constraint = sd.getConstraint((SQLException) cause);
            // if (rec.getMeta().uniques.containsKey(constraint)) {
            // throw new UserException(rec.getMeta().uniques.get(constraint).getMessage());
            // }
            // }
            throw new ModelException(
                    String.format("方法[%s.%s]执行失败", method.getDeclaringClass().getName(), method.getName()), exc);
        }
    }

    protected BaseModel getInstance() {
        Class<?> clazz = method.getDeclaringClass();
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Enhancer e = new Enhancer();
        e.setSuperclass(clazz);
        e.setCallback(ModelInterceptor.INTERCEPTOR);
        BaseModel instance = (BaseModel) e.create();
        instances.put(clazz, instance);
        return instance;
    }    
}