package jdoo.modules;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import jdoo.models.MetaModel;
import jdoo.models.MethodInfo;
import jdoo.models.Model;
import jdoo.exceptions.JdooException;
import jdoo.exceptions.ModelException;

public class Registry {
    HashMap<String, MetaModel> map = new HashMap<String, MetaModel>();

    public Registry() {
        registerBase();
    }

    public MetaModel get(String model) {
        MetaModel m = map.get(model);
        if (m == null)
            throw new ModelException("model:'" + model + "' is not registered");
        return m;
    }

    public boolean contains(String model){
        return map.containsKey(model);
    }

    void registerBase() {
        Class<?> clazz = Model.class;
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
            Model model = (Model) clazz.getDeclaredConstructor().newInstance();
            MetaModel meta = model._build_model(this);            
            map.put(meta.getName(), meta);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JdooException("register class " + clazz.getName() + " failed", e);
        }
    }
}
