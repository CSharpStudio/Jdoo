package org.jdoo;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.jdoo.exceptions.CallException;

import groovy.lang.Closure;

/**
 * 默认值
 * 
 * @author lrz
 */
public interface Default {
    /**
     * 调用返回默认值
     * 
     * @param rec
     * @return
     */
    Object call(Records rec);

    /**
     * 方法返回默认值
     * 
     * @param method
     * @return
     */
    public static Default method(String method) {
        return new MethodDefault(method);
    }

    /**
     * 脚本返回默认值
     * 
     * @param script
     * @return
     */
    public static Default script(String script) {
        return new ScriptDefault(script);
    }

    /**
     * 常量默认值
     * 
     * @param value
     * @return
     */
    public static Default value(Object value) {
        return new ValueDefault(value);
    }
}

class MethodDefault implements Default {
    String method;

    public MethodDefault(String method) {
        this.method = method;
    }

    @Override
    public Object call(Records rec) {
        return rec.call(method);
    }

    @Override
    public String toString() {
        return "@method:" + method;
    }
}

class ScriptDefault implements Default {
    String script;
    Closure<?> closure;

    public ScriptDefault(String script) {
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

    @Override
    public String toString() {
        return "@script:" + script;
    }
}

class ValueDefault implements Default {
    Object value;

    public ValueDefault(Object value) {
        this.value = value;
    }

    @Override
    public Object call(Records rec) {
        return value;
    }

    @Override
    public String toString() {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}