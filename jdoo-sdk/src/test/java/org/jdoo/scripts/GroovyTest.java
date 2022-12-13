package org.jdoo.scripts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.jdoo.Records;
import org.jdoo.models.TestResUser;
import org.jdoo.core.Environment;
import org.jdoo.core.ModelBuilder;
import org.jdoo.core.Registry;
import org.jdoo.data.Cursor;
import org.jdoo.data.TestCursor;
import org.jdoo.models.TestModel;

import org.junit.jupiter.api.Test;

public class GroovyTest {
    @Test
    public void testScriptEval() {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");
        Bindings bindings = new SimpleBindings();
        bindings.put("age", 22);
        Object value;
        try {
            groovy.lang.Closure<?> x = (groovy.lang.Closure<?>) engine.eval(
                    "{{r -> if(r > 5){ 'abc' } else { 'efg' }}}",
                    new SimpleBindings());
            System.out.println(x.call(1));
            System.out.println(x.call(6));
            value = engine.eval("if(age < 18){'未成年'}else{'成年'}", bindings);
            assertEquals(value, "成年");
        } catch (ScriptException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testCompute() throws ScriptException {

        Registry reg = new Registry(null);
        Cursor cr = TestCursor.open();

        ModelBuilder builder = new ModelBuilder();
        builder.buildBaseModel(reg);
        builder.buildModel(reg, TestResUser.class, "");
        builder.buildModel(reg, TestModel.class, "");
        reg.setupModels(cr);
        //reg.initModels(cr, reg.getModels().keySet(), true);

        Environment env = new Environment(reg, cr, "", new HashMap<>());

        Records rec = env.get("test.model").browse("01leptos19h4w");
        System.out.println(rec);

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");
        Bindings bindings = new SimpleBindings();
        bindings.put("age", 22);
        Object value = engine.eval("{r -> 'abc'}", bindings);
        // Object value = engine.eval("if(age < 18){'未成年'}else{'成年'}", bindings);
        System.out.println(value);
        cr.close();
    }
}
