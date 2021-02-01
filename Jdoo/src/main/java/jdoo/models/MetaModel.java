package jdoo.models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.el.MethodNotFoundException;

import org.springframework.util.StringUtils;

import jdoo.exceptions.ModelException;
import jdoo.exceptions.TypeErrorException;
import jdoo.exceptions.ValidationErrorException;
import jdoo.modules.Registry;
import jdoo.util.Tuple;
import jdoo.apis.Environment;
import jdoo.data.Cursor;

public class MetaModel {
    protected static List<String> LOG_ACCESS_COLUMNS = Arrays.asList("create_uid", "create_date", "write_uid",
            "write_date");
    private HashMap<String, Field> fields;
    private HashMap<String, MethodInfo> keyMethods;
    private HashMap<String, List<MethodInfo>> nameMethods;

    protected boolean _auto = false; // don't create any database backend
    protected boolean _register = false; // not visible in ORM registry
    protected boolean _abstract = true; // whether model is abstract
    protected boolean _transient = false; // whether model is transient

    protected String _name; // the model name
    protected String _description; // the model's informal name
    protected boolean _custom = false; // should be True for custom models only

    protected Object _inherit; // Python-inherited models ('model' or ['model'])
    protected String[] _inherits; // inherited models {'parent_model': 'm2o_field'}

    protected String _table; // SQL table name used by model
    protected String[] _sql_constraints; // SQL constraints [(name, sql_def, message)]

    protected String _rec_name; // field to use for labeling records
    protected String _order = "id"; // default order for searching results
    protected String _parent_name = "parent_id"; // the many2one field used as parent field
    protected boolean _parent_store = false; // set to True to compute parent_path field
    protected String _date_name = "date"; // field to use for default calendar view
    protected String _fold_name = "fold"; // field to determine folded groups in kanban views

    protected boolean _needaction = false; // whether the model supports "need actions" (see mail)
    protected boolean _translate = true; // False disables translations export for this model
    protected boolean _check_company_auto = false;
    protected boolean _log_access = true;

    public boolean auto() {
        return _auto;
    }

    public boolean register() {
        return _register;
    }

    public boolean _abstract() {
        return _abstract;
    }

    public boolean _transient() {
        return _transient;
    }

    public String name() {
        return _name;
    }

    public String description() {
        return _description;
    }

    public boolean custom() {
        return _custom;
    }

    public String[] inherits() {
        if (_inherits == null)
            return new String[0];
        return _inherits;
    }

    public String table() {
        return _table;
    }

    public String[] _sql_constraints() {
        return _sql_constraints;
    }

    public String rec_name() {
        return _rec_name;
    }

    public String order() {
        return _order;
    }

    public String parent_name() {
        return _parent_name;
    }

    public boolean parent_store() {
        return _parent_store;
    }

    public String date_name() {
        return _date_name;
    }

    public String fold_name() {
        return _fold_name;
    }

    public boolean needaction() {
        return _needaction;
    }

    public boolean translate() {
        return _translate;
    }

    public boolean check_company_auto() {
        return _check_company_auto;
    }

    public boolean log_access() {
        return _log_access;
    }

    List<String> _inherits_children;

    List<String> _inherit_children;

    Tuple<MetaModel> _bases;

    String _module;

    String _original_module;

    List<String> inherits_children() {
        if (_inherits_children == null) {
            _inherits_children = new ArrayList<>();
        }
        return _inherits_children;
    }

    Tuple<MetaModel> _bases() {
        if (_bases == null) {
            _bases = Tuple.emptyTuple();
        }
        return _bases;
    }

    protected MetaModel() {

    }

    public MetaModel(String name) {
        _name = name;
    }

    public MetaModel(String name, String module) {
        _name = name;
        _register = false;
        _original_module = module;
    }

    public MetaModel(MetaModel m) {
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
        org.apache.tomcat.util.buf.StringUtils.join(args, ',', o -> o == null ? "null" : o.getClass().getName(), sb);
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

    public RecordSet browse(Environment env, Collection<String> ids, Collection<String> prefetchIds) {
        RecordSet m = new RecordSet(this, env);
        m.ids = Tuple.fromCollection(ids);
        m.prefetchIds = Tuple.fromCollection(prefetchIds);
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

    public void init(Registry pool, Cursor cr) {

    }

    @SuppressWarnings("unchecked")
    public static MetaModel _build_model(Class<?> clazz, String module, Registry pool, Cursor cr)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        MetaModel cls = (MetaModel) clazz.getDeclaredConstructor().newInstance();
        cls._module = module;
        List<String> parents = new ArrayList<>();
        if (cls._inherit instanceof String) {
            parents.add((String) cls._inherit);
        } else if (cls._inherit instanceof Collection<?>) {
            parents.addAll((Collection<String>) cls._inherit);
        } else if (cls._inherit instanceof String[]) {
            parents.addAll(Arrays.asList((String[]) cls._inherit));
        }
        String name;
        if (!StringUtils.hasText(cls._name)) {
            name = cls._name;
        } else if (parents.size() == 1) {
            name = parents.get(0);
        } else {
            name = clazz.getName();
        }
        if (name != "base") {
            parents.add("base");
        }
        MetaModel ModelClass;
        if (parents.contains(name)) {
            if (!pool.contains(name)) {
                throw new TypeErrorException(String.format("Model %s does not exist in registry.", name));
            }
            ModelClass = pool.get(name);
            ModelClass._build_model_check_base(cls);
        } else {
            ModelClass = new MetaModel(name, cls._module);
        }
        List<MetaModel> bases = Arrays.asList(cls);
        for (String parent : parents) {
            if (!pool.contains(parent)) {
                throw new TypeErrorException(
                        String.format("Model %s inherits from non-existing model %s.", name, parent));
            }
            MetaModel parent_class = pool.get(parent);
            if (parent.equals(name)) {
                for (MetaModel base : parent_class._bases()) {
                    if (bases.contains(base)) {
                        bases.remove(base);
                    }
                    bases.add(base);
                }
            } else {
                if (parents.contains(name)) {
                    _build_model_check_parent(ModelClass, cls, parent_class);
                }else{
                    _build_model_check_parent(cls, cls, parent_class);
                }
                if (bases.contains(parent_class)) {
                    bases.remove(parent_class);
                }
                bases.add(parent_class);
                parent_class.inherits_children().add(name);
            }
        }
        ModelClass._bases = Tuple.fromCollection(bases);
        ModelClass._build_model_attributes(pool);

        Util.check_pg_name(ModelClass._table);

        ModelClass.init(pool, cr);

        return ModelClass;
    }

    static void _build_model_check_parent(MetaModel model_class, MetaModel cls, MetaModel parent_class) {
        if (model_class._abstract && !parent_class._abstract) {
            throw new TypeErrorException(
                    String.format("In %s, the abstract model %s cannot inherit from the non-abstract model %s.", cls,
                            model_class._name, parent_class._name));
        }
    }

    public void _build_model_check_base(MetaModel cls) {

    }

    public void _build_model_attributes(Registry pool) {

    }

    static class Util {
        static final Pattern pattern = Pattern.compile("^[a-z_][a-z0-9_$]*$", Pattern.CASE_INSENSITIVE);

        // final String pattern="^[a-z_][a-z0-9_$]*$";
        public static void check_pg_name(String name) {
            if (!pattern.matcher(name).matches()) {
                throw new ValidationErrorException(String.format("Invalid characters in table name %s", name));
            }
            if (name.length() > 63) {
                throw new ValidationErrorException(String.format("Table name %s is too long", name));
            }
        }
    }
}
