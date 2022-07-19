package org.jdoo;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.jdoo.exceptions.CallException;

import groovy.lang.Closure;

/**
 * 可执行方法
 * 
 * @author lrz
 */
public interface Callable {
    /**
     * 调用返回结果
     * 
     * @param rec
     * @return
     */
    Object call(Records rec);

    /**
     * 方法
     * 
     * @param method
     * @return
     */
    public static Callable method(String method) {
        return new MethodCallable(method);
    }

    /**
     * 脚本
     * 
     * @param script
     * @return
     */
    public static Callable script(String script) {
        return new ScriptCallable(script);
    }
}

class MethodCallable implements Callable {
    String method;

    public MethodCallable(String method) {
        this.method = method;
    }

    @Override
    public Object call(Records rec) {
        return rec.call(method);
    }

}

class ScriptCallable implements Callable {
    String script;
    Closure<?> closure;

    public ScriptCallable(String script) {
        this.script = "{" + script + "}";
    }

    Closure<?> getClosure() {
        if (closure == null) {
            try {
                ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
                closure = (Closure<?>) engine.eval(script, new SimpleBindings());
            } catch (Exception e) {
                throw new CallException(String.format("脚本[%s]执行失败", script), e);
            }
        }
        return closure;
    }

    @Override
    public Object call(Records rec) {
        return getClosure().call(rec);
    }
}