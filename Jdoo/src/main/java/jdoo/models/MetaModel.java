package jdoo.models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.el.MethodNotFoundException;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import jdoo.exceptions.AccessErrorException;
import jdoo.exceptions.InvocationException;
import jdoo.exceptions.ModelException;
import jdoo.exceptions.TypeErrorException;
import jdoo.exceptions.ValidationErrorException;
import jdoo.modules.Registry;
import jdoo.tools.Collector;
import jdoo.util.Kwargs;
import jdoo.util.Tuple;
import jdoo.util.Utils;
import jdoo.apis.Environment;
import jdoo.data.Cursor;

public class MetaModel {
    protected static List<String> LOG_ACCESS_COLUMNS = Arrays.asList("create_uid", "create_date", "write_uid",
            "write_date");
    List<Field> $fields;

    private List<String> _inherits_children;
    private Set<String> _inherit_children;
    Tuple<MetaModel> _bases;
    private String _module;
    String _original_module;
    Map<String, Field> _fields;
    private Map<String, MethodInfo> keyMethods;
    private Map<String, List<MethodInfo>> nameMethods;
    /** don't create any database backend */
    protected boolean _auto = false;
    /** not visible in ORM registry */
    protected boolean _register = false;
    /** whether model is abstract */
    protected boolean _abstract = true;
    /** whether model is transient */
    protected boolean _transient = false;
    /** the model name */
    protected String _name;
    /** the model's informal name */
    protected String _description;
    /** should be True for custom models only */
    protected boolean _custom = false;
    /** jdoo-inherited models ('model' or ['model']) */
    protected Object _inherit;
    /** inherited models {'parent_model': 'm2o_field'} */
    protected Map<String, String> _inherits = Collections.emptyMap();

    /** SQL table name used by model */
    protected String _table;
    /** SQL constraints [(name, sql_def, message)] */
    protected Collection<Tuple<String>> _sql_constraints = Collections.emptyList();
    /** field to use for labeling records */
    protected String _rec_name;
    /** default order for searching results */
    protected String _order = "id";
    /** the many2one field used as parent field */
    protected String _parent_name = "parent_id";
    /** set to True to compute parent_path field */
    protected boolean _parent_store = false;
    /** field to use for default calendar view */
    protected String _date_name = "date";
    /** field to determine folded groups in kanban views */
    protected String _fold_name = "fold";

    /** whether the model supports "need actions" (see mail) */
    protected boolean _needaction = false;
    /** false disables translations export for this model */
    protected boolean _translate = true;
    protected boolean _check_company_auto = false;
    protected Boolean _log_access;
    static final String CONCURRENCY_CHECK_FIELD = "__last_update";
    boolean _setup_done;
    Map<Field, List<Field>> _field_computed;
    Collector _field_inverses;

    Registry pool;

    public boolean parent_store() {
        return _parent_store;
    }

    public String parent_name() {
        return _parent_name;
    }

    public String name() {
        return _name;
    }

    public Collector field_inverses() {
        return _field_inverses;
    }

    public String description() {
        return _description;
    }

    public String table() {
        return _table;
    }

    public String rec_name() {
        return _rec_name;
    }

    public boolean log_access() {
        if (_log_access == null) {
            return false;
        }
        return _log_access;
    }

    public Registry pool() {
        return pool;
    }

    public Map<String, String> inherits() {
        return _inherits;
    }

    public boolean $abstract() {
        return _abstract;
    }

    public boolean is_transient() {
        return _transient;
    }

    protected MetaModel() {

    }

    public MetaModel(String name, String module) {
        _name = name;
        _register = false;
        _original_module = module;
        _inherit_children = new TreeSet<>();
        _inherits_children = new ArrayList<>();
        _fields = new TreeMap<>();
        $fields = new ArrayList<>();
    }

    public Collection<Field> getFields() {
        if (_fields == null) {
            return Collections.emptyList();
        }
        return _fields.values();
    }

    public Field findField(String field) {
        if (_fields != null && field != null && _fields.containsKey(field))
            return _fields.get(field);
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

    public MethodInfo findMethod(String method) {
        List<MethodInfo> methods = nameMethods.get(method);
        if (methods != null && !methods.isEmpty()) {
            return methods.get(methods.size() - 1);
        }
        return null;
    }

    private Map<String, List<MethodInfo>> getNameMethods() {
        if (nameMethods == null) {
            return Collections.emptyMap();
        }
        return nameMethods;
    }

    private void setMethods(Collection<MethodInfo> methods) {
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

    public Object callSuper(String method, Object... args) {
        if (nameMethods.containsKey(method)) {
            List<MethodInfo> methods = nameMethods.get(method);
            Class<?>[] parameterTypes = null;
            Class<?> clazz = getClass();
            for (int i = methods.size() - 1; i >= 0; i--) {
                MethodInfo meta = methods.get(i);
                if (meta.getMethod().getDeclaringClass() == clazz && meta.isParameterMatch(args, null)) {
                    parameterTypes = meta.getMethod().getParameterTypes();// find the parameterTypes of the matched
                                                                          // method in current class first
                    continue;
                }
                if (parameterTypes != null && meta.isParameterMatch(parameterTypes)) {
                    try {
                        return meta.invokeThis(args);
                    } catch (Exception e) {
                        throw new InvocationException("invoke method:'" + method + "' failed", e);
                    }
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        org.apache.tomcat.util.buf.StringUtils.join(args, ',', o -> o == null ? "null" : o.getClass().getName(), sb);
        throw new MethodNotFoundException("call super method:" + method + "(" + sb.toString() + ") not found");
    }

    public Object invoke(String method, Object[] args, @Nullable Kwargs kwargs) {
        if (nameMethods.containsKey(method)) {
            List<MethodInfo> methods = nameMethods.get(method);
            for (int i = methods.size() - 1; i >= 0; i--) {
                MethodInfo meta = methods.get(i);
                if (meta.isParameterMatch(args, kwargs)) {
                    try {
                        return meta.invoke(args, kwargs);
                    } catch (Exception e) {
                        throw new InvocationException("invoke method:'" + method + "' failed", e);
                    }
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        org.apache.tomcat.util.buf.StringUtils.join(args, ',', o -> o == null ? "null" : o.getClass().getName(), sb);
        throw new MethodNotFoundException("method:" + method + "(" + sb.toString() + ") not found");
    }

    public Object invoke(String method, Object[] args) {
        return invoke(method, args, null);
    }

    public MethodInfo findOverrideMethod(Method method) {
        String key = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> type = method.getDeclaringClass();
        if (nameMethods.containsKey(key)) {
            List<MethodInfo> methods = nameMethods.get(key);
            for (int i = methods.size() - 1; i >= 0; i--) {
                MethodInfo meta = methods.get(i);
                if (meta.isParameterMatch(parameterTypes)) {
                    if (meta.getMethod().getDeclaringClass() == type) {
                        return null;
                    }
                    return meta;
                }
            }
        }
        return null;
    }

    public RecordSet browse(Environment env, Collection<?> ids, Collection<?> prefetchIds) {
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

    private static MetaModel newInstance(Class<?> clazz) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        MetaModel cls = (MetaModel) clazz.getDeclaredConstructor().newInstance();
        if (cls.$fields == null) {
            cls.$fields = new ArrayList<>();
        }
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Field.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            Field f = (Field) field.get(null);
            f.name = field.getName();
            if (f.name.startsWith("$")) {
                f.name = f.name.substring(1);
            }
            cls.$fields.add(f);
        }
        return cls;
    }

    /**
     * Instantiate a given model in the registry.
     * 
     * This method creates or extends a "registry" class for the given model. This
     * "registry" class carries inferred model metadata, and inherits (in the Python
     * sense) from all classes that define the model, and possibly other registry
     * classes.
     * 
     * <pre>
     *In the simplest case, the model's registry class inherits from cls and
     *the other classes that define the model in a flat hierarchy. The
     *registry contains the instance ``model`` (on the left). Its class,
     *``ModelClass``, carries inferred metadata that is shared between all
     *the model's instances for this registry only.
     *  class A1(Model):                          Model
     *      _name = 'a'                           / | \
     *                                           A3 A2 A1
     *  class A2(Model):                          \ | /
     *      _inherit = 'a'                      ModelClass
     *                                            /   \
     *  class A3(Model):                      model   recordset
     *      _inherit = 'a'
     *When a model is extended by '_inherit', its base classes are modified
     *to include the current class and the other inherited model classes.
     *Note that we actually inherit from other ``ModelClass``, so that
     *extensions to an inherited model are immediately visible in the
     *current model class, like in the following example:
     *  class A1(Model):
     *      _name = 'a'                           Model
     *                                           / / \ \
     *  class B1(Model):                        / A2 A1 \
     *      _name = 'b'                        /   \ /   \
     *                                        B2  ModelA  B1
     *  class B2(Model):                       \    |    /
     *      _name = 'b'                         \   |   /
     *      _inherit = ['a', 'b']                \  |  /
     *                                            ModelB
     *  class A2(Model):
     *      _inherit = 'a'
     * </pre>
     */
    public static MetaModel _build_model(Class<?> clazz, String module, Registry pool, Cursor cr)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        MetaModel cls = newInstance(clazz);
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
        if (StringUtils.hasText(cls._name)) {
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
            _build_model_check_base(ModelClass, cls);
        } else {
            ModelClass = new MetaModel(name, cls._module);
        }
        List<MetaModel> bases = Utils.asList(cls);
        for (String parent : parents) {
            if (!pool.contains(parent)) {
                throw new TypeErrorException(
                        String.format("Model %s inherits from non-existing model %s.", name, parent));
            }
            MetaModel parent_class = pool.get(parent);
            if (parent.equals(name)) {
                for (MetaModel base : parent_class._bases) {
                    if (bases.contains(base)) {
                        bases.remove(base);
                    }
                    bases.add(base);
                }
            } else {
                if (parents.contains(name)) {
                    _build_model_check_parent(ModelClass, cls, parent_class);
                } else {
                    _build_model_check_parent(cls, cls, parent_class);
                }
                bases.remove(parent_class);
                bases.add(parent_class);
                parent_class._inherit_children.add(name);
            }
        }
        // ModelClass.__bases__ = tuple(bases)
        set_bases(ModelClass, bases);

        List<MethodInfo> method_list = new ArrayList<>();
        for (int i = ModelClass._bases.size() - 1; i >= 0; i--) {
            MetaModel base = ModelClass._bases.get(i);
            for (List<MethodInfo> values : base.getNameMethods().values()) {
                for (MethodInfo v : values) {
                    method_list.add(new MethodInfo(ModelClass, v.getMethod()));
                }
            }
        }
        if (method_list.isEmpty()) {
            for (Method method : BaseModel.class.getDeclaredMethods()) {
                if (Modifier.isPrivate(method.getModifiers())) {
                    continue;
                }
                method_list.add(new MethodInfo(ModelClass, method));
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                continue;
            }
            method_list.add(new MethodInfo(ModelClass, method));
        }
        ModelClass.setMethods(method_list);

        _build_model_attributes(ModelClass, pool);

        Util.check_pg_name(ModelClass._table);

        if (ModelClass._transient) {
            assert ModelClass._log_access : "TransientModels must have log_access turned on, "
                    + "in order to implement their access rights policy";
        }

        // link the class to the registry, and update the registry
        ModelClass.pool = pool;
        pool.put(name, ModelClass);

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

    static void _build_model_check_base(MetaModel model_class, MetaModel cls) {
        if (model_class._abstract && !cls._abstract) {
            throw new TypeErrorException(String.format(
                    "%s transforms the abstract model %s into a non-abstract model. "
                            + "That class should either inherit from AbstractModel, or set a different '_name'.",
                    cls, model_class._name));
        }
        if (model_class._transient != cls._transient) {
            if (model_class._transient) {
                throw new TypeErrorException(String.format(
                        "%s transforms the transient model %s into a non-transient model. "
                                + "That class should either inherit from TransientModel, or set a different '_name'.",
                        cls, model_class._name));
            } else {
                throw new TypeErrorException(String.format(
                        "%s transforms the model %s into a transient model. "
                                + "That class should either inherit from Model, or set a different '_name'.",
                        cls, model_class._name));
            }
        }
    }

    static void set_bases(MetaModel cls, List<MetaModel> bases) {
        cls._bases = Tuple.fromCollection(bases);
        if (bases.size() > 0) {
            MetaModel base = bases.get(0);
            cls._auto = base._auto;
            // cls._register = base._register;
            cls._abstract = base._abstract;
            cls._transient = base._transient;
            // cls._name = base._name;
            cls._description = base._description;
            cls._custom = base._custom;
            cls._inherit = base._inherit;
            cls._inherits = base._inherits;
            cls._table = base._table;
            cls._sql_constraints = base._sql_constraints;
            cls._rec_name = base._rec_name;
            cls._order = base._order;
            cls._parent_name = base._parent_name;
            cls._parent_store = base._parent_store;
            cls._date_name = base._date_name;
            cls._fold_name = base._fold_name;
            cls._needaction = base._needaction;
            cls._translate = base._translate;
            cls._check_company_auto = base._check_company_auto;
        }
    }

    static void _build_model_attributes(MetaModel cls, Registry pool) {
        cls._description = cls._name;
        cls._table = cls._name.replace('.', '_');
        cls._log_access = cls._auto;
        cls._inherits = new HashMap<>();
        Map<String, Tuple<String>> _sql_constraints = new HashMap<>();
        for (int i = cls._bases.size() - 1; i >= 0; i--) {
            MetaModel base = cls._bases.get(i);
            if (base.pool == null) {
                if (StringUtils.hasText(base._description)) {
                    cls._description = base._description;
                }
                if (StringUtils.hasText(base._table)) {
                    cls._table = base._table;
                }
                if (base._log_access != null) {
                    cls._log_access = base._log_access;
                }
            }
            Utils.update(cls._inherits, base._inherits);
            for (Tuple<String> cons : base._sql_constraints) {
                _sql_constraints.put(cons.get(0), cons);
            }
        }
        cls._sql_constraints = _sql_constraints.values();
        for (String parent_name : cls._inherits.keySet()) {
            pool.get(parent_name)._inherits_children.add(cls._name);
        }
        for (String child_name : cls._inherit_children) {
            MetaModel child_class = pool.get(child_name);
            _build_model_attributes(child_class, pool);
        }
    }

    static final Pattern regex_private = Pattern.compile("^(_.*|init)$");

    public static void check_method_name(String name) {
        if (regex_private.matcher(name).matches()) {
            throw new AccessErrorException(
                    String.format("Private methods (such as %s) cannot be called remotely.", name));
        }
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
