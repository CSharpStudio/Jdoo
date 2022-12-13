package org.jdoo.core;

import java.lang.reflect.Method;
import java.util.HashMap;

import net.sf.cglib.proxy.Enhancer;
import org.jdoo.exceptions.ModelException;


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