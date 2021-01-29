package jdoo.modules;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jdoo.models.BaseModel;
import jdoo.models.MetaModel;
import jdoo.models.MethodInfo;
import jdoo.models.Self;
import jdoo.tools.Dict;
import jdoo.init;
import jdoo.apis.Environment;
import jdoo.data.Cursor;
import jdoo.exceptions.JdooException;
import jdoo.exceptions.ModelException;

public class Registry {
    HashMap<String, MetaModel> map = new HashMap<String, MetaModel>();
    String tenant;

    public String tenant() {
        return tenant;
    }

    public Registry(String tenant) {
        this.tenant = tenant;
        registerBase();
    }

    public MetaModel get(String model) {
        MetaModel m = map.get(model);
        if (m == null)
            throw new ModelException("model:'" + model + "' is not registered");
        return m;
    }

    public boolean contains(String model) {
        return map.containsKey(model);
    }

    void registerBase() {
        Class<?> clazz = BaseModel.class;
        MetaModel base = new MetaModel("base");
        ArrayList<MethodInfo> methodMetas = new ArrayList<MethodInfo>();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            methodMetas.add(new MethodInfo(base, method));
        }
        base.setMethods(methodMetas);
        map.put("base", base);
    }

    public void register(Class<?> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers()))
            throw new ModelException("Model:" + clazz.getName() + " cannot be abstract class");
        try {
            BaseModel model = (BaseModel) clazz.getDeclaredConstructor().newInstance();
            MetaModel meta = model._build_model(this);
            map.put(meta.getName(), meta);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JdooException("register class " + clazz.getName() + " failed", e);
        }
    }

    public void setup_models(Cursor cr) {
        Environment env = Environment.create(this, cr, init.SUPERUSER_ID, new Dict(), true);

        List<Self> models = new ArrayList<>();
        for (MetaModel m : map.values()) {
            Self self = env.get(m.getName());
            models.add(self);
            self.call("_prepare_setup");
        }
        for (Self self : models) {
            self.call("_setup_base");
        }
        for (Self self : models) {
            self.call("_setup_fields");
        }
        for (Self self : models) {
            self.call("_setup_complete");
        }
    }

    public void init_models(Cursor cr) {
        Environment env = Environment.create(this, cr, init.SUPERUSER_ID, new Dict(), true);
        List<Self> models = new ArrayList<>();
        for (MetaModel m : map.values()) {
            Self self = env.get(m.getName());
            models.add(self);
            self.call("_auto_init");
            self.call("init");
        }
        env.get("base").call("flush");
    }
}
