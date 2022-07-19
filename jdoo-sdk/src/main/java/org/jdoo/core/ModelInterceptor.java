package org.jdoo.core;

import java.lang.reflect.Method;

import org.jdoo.Records;
import org.springframework.core.NamedThreadLocal;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ModelInterceptor implements MethodInterceptor {
    public static final ModelInterceptor INTERCEPTOR = new ModelInterceptor();

    public final static NamedThreadLocal<Boolean> INVOKE_CURRENT = new NamedThreadLocal<>("invoke current method");
    
    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if (INVOKE_CURRENT.get() != null) {
            INVOKE_CURRENT.remove();
            return proxy.invokeSuper(target, args);
        }
        if (args.length > 0 && args[0] instanceof Records) {
            MetaModel meta = ((Records) args[0]).getMeta();
            MetaMethod m = meta.findOverrideMethod(method);
            if (m != null) {                    
                INVOKE_CURRENT.set(true);
                return m.invoke(args);
            }
        }
        Object result = proxy.invokeSuper(target, args);
        return result;
    }
}