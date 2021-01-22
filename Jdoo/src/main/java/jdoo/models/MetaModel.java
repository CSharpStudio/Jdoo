package jdoo.models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.el.MethodNotFoundException;

import org.apache.tomcat.util.buf.StringUtils;

import jdoo.exceptions.ModelException;
import jdoo.apis.Environment;

public class MetaModel {
    private HashMap<String, Field> fields;
    private HashMap<String, MethodInfo> keyMethods;
    private HashMap<String, List<MethodInfo>> nameMethods;
    boolean _auto = false; // don't create any database backend

    public boolean auto() {
        return _auto;
    }

    boolean _register = false; // not visible in ORM registry

    public boolean register() {
        return _register;
    }

    boolean _abstract = true; // whether model is abstract

    public boolean _abstract() {
        return _abstract;
    }

    boolean _transient = false; // whether model is transient

    public boolean _transient() {
        return _transient;
    }

    String _name; // the model name
    String _description; // the model's informal name

    public String description() {
        return _description;
    }

    boolean _custom = false; // should be True for custom models only

    public boolean custom() {
        return _custom;
    }

    String _inherit; // Python-inherited models ('model' or ['model'])

    public String inherit() {
        return _inherit;
    }

    String[] _inherits; // inherited models {'parent_model': 'm2o_field'}

    public String[] inherits() {
        return _inherits;
    }

    String _table; // SQL table name used by model

    public String table() {
        return _table;
    }

    String[] _sql_constraints; // SQL constraints [(name, sql_def, message)]

    public String[] _sql_constraints() {
        return _sql_constraints;
    }

    String _rec_name; // field to use for labeling records

    public String rec_name() {
        return _rec_name;
    }

    String _order = "id"; // default order for searching results

    public String order() {
        return _order;
    }

    String _parent_name = "parent_id"; // the many2one field used as parent field

    public String parent_name() {
        return _parent_name;
    }

    boolean _parent_store = false; // set to True to compute parent_path field

    public boolean parent_store() {
        return _parent_store;
    }

    String _date_name = "date"; // field to use for default calendar view

    public String date_name() {
        return _date_name;
    }

    String _fold_name = "fold"; // field to determine folded groups in kanban views

    public String fold_name() {
        return _fold_name;
    }

    boolean _needaction = false; // whether the model supports "need actions" (see mail)

    public boolean needaction() {
        return _needaction;
    }

    boolean _translate = true; // False disables translations export for this model

    public boolean translate() {
        return _translate;
    }

    boolean _check_company_auto = false;

    public boolean check_company_auto() {
        return _check_company_auto;
    }

    boolean _log_access = true;

    public boolean log_access() {
        return _log_access;
    }

    public MetaModel(String name) {
        _name = name;
    }

    public MetaModel(Model m) {
        _auto = m._auto;
        _register = m._register;
        _abstract = m._abstract;
        _transient = m._transient;
        _name = m._name;
        _description = m._description;
        _custom = m._custom;
        _inherit = m._inherit;
        _inherits = m._inherits;
        _table = m._table;
        _sql_constraints = m._sql_constraints;
        _rec_name = m._rec_name;
        _order = m._order;
        _parent_name = m._parent_name;
        _parent_store = m._parent_store;
        _date_name = m._date_name;
        _fold_name = m._fold_name;
        _needaction = m._needaction;
        _translate = m._translate;
        _check_company_auto = m._check_company_auto;
        _log_access = m._log_access;
    }

    public String getName() {
        return _name;
    }

    public void setFields(Collection<Field> fields) {
        this.fields = new HashMap<String, Field>();
        for (Field field : fields)
            this.fields.put(field.getName(), field);
    }

    public Collection<Field> getFields() {
        if (fields == null) {
            fields = new HashMap<String, Field>();
        }
        return fields.values();
    }

    public Field findField(String field) {
        if (fields != null && fields.containsKey(field))
            return fields.get(field);
        return null;
    }

    public Field getField(String field) {
        Field f = findField(field);
        if (f == null)
            throw new ModelException("Model:" + _name + " has no field:" + field);
        return f;
    }

    public Collection<MethodInfo> getMethods() {
        if (keyMethods == null) {
            keyMethods = new HashMap<String, MethodInfo>();
        }
        return keyMethods.values();
    }

    public HashMap<String, List<MethodInfo>> getNameMethods() {
        return nameMethods;
    }

    public void setMethods(Collection<MethodInfo> methods) {
        nameMethods = new HashMap<String, List<MethodInfo>>();
        keyMethods = new HashMap<String, MethodInfo>();
        for (MethodInfo method : methods) {
            String name = method.getMethod().getName();
            List<MethodInfo> methodMetas;
            if (nameMethods.containsKey(name)) {
                methodMetas = nameMethods.get(name);
            } else {
                methodMetas = new ArrayList<MethodInfo>();
                nameMethods.put(name, methodMetas);
            }
            methodMetas.add(method);

            String key = getMethodKey(method);
            if (keyMethods.containsKey(key)) {
                keyMethods.replace(key, method);
            } else {
                keyMethods.put(key, method);
            }
        }
    }

    public InvokeResult tryInvoke(String method, Object[] args) {
        for (MethodInfo meta : getMethods()) {
            Method m = meta.getMethod();
            Class<?>[] parameters = m.getParameterTypes();
            if (m.getName() == method && parameters.length == args.length) {
                // TODO check parameter types match the args types
                Object result;
                try {
                    result = meta.invoke(args);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new InvokeResult(false, e);
                }
                return new InvokeResult(true, result);
            }
        }
        return new InvokeResult(false, "method not found");
    }

    public Object invoke(String method, Object[] args)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        for (MethodInfo meta : getMethods()) {
            Method m = meta.getMethod();
            Class<?>[] parameters = m.getParameterTypes();
            if (m.getName() == method && parameters.length == args.length) {
                // TODO check parameter types match the args types
                return meta.invoke(args);
            }
        }
        StringBuilder sb = new StringBuilder();
        StringUtils.join(args, ',', o -> o == null ? "null" : o.getClass().getName(), sb);
        throw new MethodNotFoundException("method:" + method + "(" + sb.toString() + ") not found");
    }

    public MethodInfo findOverrideMethod(Method method) {
        String key = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> type = method.getDeclaringClass();
        if (nameMethods.containsKey(key)) {
            List<MethodInfo> methods = nameMethods.get(key);
            for (int i = methods.size() - 1; i > 0; i--) {
                MethodInfo meta = methods.get(i);
                Method m = meta.getMethod();
                Class<?>[] parameters = m.getParameterTypes();
                if (isParameterMatch(parameterTypes, parameters)) {
                    if (m.getDeclaringClass() == type) {
                        return null;
                    }
                    return meta;
                }
            }
        }
        return null;
    }

    boolean isParameterMatch(Class<?>[] x, Class<?>[] y) {
        if (x.length != y.length)
            return false;
        for (int i = 0; i < x.length; i++) {
            if (x[i] != y[i])
                return false;
        }
        return true;
    }

    public Self browse(Environment env, List<String> ids, List<String> prefetchIds) {
        Self m = new Self(this, env);
        m.ids = ids;
        m.prefetchIds = prefetchIds;
        return m;
    }

    private String getMethodKey(MethodInfo method) {
        Class<?>[] types = method.getMethod().getParameterTypes();
        String key = method.getMethod().getName();
        key += "(";
        boolean isFirst = true;
        for (Class<?> t : types) {
            if (isFirst) {
                isFirst = false;
            } else {
                key += ",";
            }
            key += t.getSimpleName();
        }
        key += ")";
        return key;
    }
}
