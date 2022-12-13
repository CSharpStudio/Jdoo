package org.jdoo.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.jdoo.Field;
import org.jdoo.Records;
import org.jdoo.data.Cursor;
import org.jdoo.data.DbColumn;
import org.jdoo.data.SqlDialect;
import org.jdoo.exceptions.ModelException;
import org.jdoo.fields.DateTimeField;
import org.jdoo.fields.IdField;
import org.jdoo.fields.Many2oneField;
import org.jdoo.utils.ArrayUtils;
import org.jdoo.utils.StringUtils;

/**
 * 模型元数据
 * 
 * @author lrz
 */
public class MetaModel {
    /** 模型的字段 */
    Map<String, MetaField> fields = new LinkedHashMap<>();
    /** 模型的方法 */
    Map<String, List<MetaMethod>> methods;
    /** 参数，构建时使用 */
    Map<String, Object> args = new HashMap<>();
    /** 父模型 */
    List<MetaModel> bases = new ArrayList<>();
    /** Method resolve order */
    List<MetaModel> mro;
    /** 注册表 */
    Registry registry;
    /** 继承当前模型的子模型 */
    Set<String> inheritChildren = new TreeSet<>();
    /** 服务 */
    Map<String, BaseService> services;
    /** 唯一约束 */
    Map<String, UniqueConstraint> uniques;
    /** 方法约束 */
    Map<String, MethodConstrains> constrains;
    /** 是否自定义的模型 */
    boolean custom;
    /** 需要授权的字段 */
    List<String> authFields = new ArrayList<>();

    /**
     * 是否自动创建数据库表，如果设置false，可以重写{@link BaseModel#init(Records)}方法手动创建数据库表。
     * {@link Model}默认是true, {@link AbstractModel}默认是false。
     */
    boolean auto;
    /** 模型是否抽象的 */
    boolean isAbstract = true;
    /** 模型是否瞬态的 */
    boolean isTransient = false;
    /** 模型名称，使用 . 分隔模块命名空间 */
    String name;
    /** 模型的标题 */
    String label;
    /** 模型的描述 */
    String description;
    /** 授权模型，使用此模型的读取权限（权限码read） */
    String authModel;
    /** 数据库表名 */
    String table;
    /** 记录集的展示字段 */
    String[] present;
    /** 记录集展示字段的格式化，如{name}-{description} */
    String presentFormat;
    /** 查询的默认排序 */
    String order;
    /** 是否记录创建人、修改人等信息 */
    boolean logAccess = true;
    /** 设置完成标记，避免重复设置 */
    boolean setupDone;
    /** 模块 */
    String module;
    /** 委托继承，{comodel:many2one} */
    Map<String, String> delegates = new LinkedHashMap<>();

    public Map<String, String> getDelegates() {
        return delegates;
    }

    /**
     * 获取模型定义的所有字段
     * 
     * @return map
     */
    public Map<String, MetaField> getFields() {
        return fields;
    }

    /**
     * 查询指定名称的字段
     * 
     * @param field 字段名称
     * @return 没找到返回null
     */
    public MetaField findField(String field) {
        return fields.get(field);
    }

    /**
     * 获取指定名称的字段
     * 
     * @param field 字段名称
     * @exception ModelException 字段不存在
     * @return 字段元数据
     */
    public MetaField getField(String field) {
        MetaField f = findField(field);
        if (f == null) {
            throw new ModelException("模型:" + name + " 没定义字段:" + field);
        }
        return f;
    }

    /**
     * 获取需要授权的字段
     * 
     * @return
     */
    public List<String> getAuthFields() {
        return authFields;
    }

    /**
     * 设置需要授权的字段
     * 
     * @param fields
     */
    public void setAuthFields(List<String> fields) {
        authFields = fields;
    }

    /**
     * 获取唯一约束的定义
     * 
     * @return
     */
    public Collection<UniqueConstraint> getUniques() {
        return uniques.values();
    }

    /**
     * 获取方法约束的定义
     * 
     * @return
     */
    public Collection<MethodConstrains> getConstrains() {
        return constrains.values();
    }

    /**
     * 获取模型注册表
     * 
     * @return
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * 是否自定义模型，从ir.model创建的模型
     * 
     * @return
     */
    public boolean isCustom() {
        return custom;
    }

    /**
     * 获取数据表名
     * 
     * @return
     */
    public String getTable() {
        return table;
    }

    /**
     * 是否抽象模型
     * 
     * @return
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    /**
     * 获取模型描述
     * 
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取授权模型，设置授权模型后，使用授权模型的读取权限（权限码read）
     * 
     * @return 没指定返回null
     */
    public String getAuthModel() {
        return authModel;
    }

    /**
     * 获取记录展示的字段名
     * 
     * @return
     */
    public String[] getPresent() {
        if (present == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return present;
    }

    /**
     * 获取记录展示的格式
     * 
     * @return
     */
    public String getPresentFormat() {
        return presentFormat;
    }

    /**
     * 获取排序
     * 
     * @return
     */
    public String getOrder() {
        return order;
    }

    /**
     * 是否记录操作日志（create_uid,create_date,update_uid,update_date）
     * 
     * @return
     */
    public boolean isLogAccess() {
        return logAccess;
    }

    /**
     * 是否瞬时模型
     * 
     * @return
     */
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * 获取定义模型的模块
     * 
     * @return
     */
    public String getModule() {
        return module;
    }

    /**
     * 实例化模型的记录集
     * 
     * @param env         环境上下文
     * @param ids         数据集的id
     * @param prefetchIds 预加载的id
     * @return
     */
    public Records browse(Environment env, String[] ids, Supplier<String[]> prefetchIds) {
        Records rec = new Records(env, this, ids, prefetchIds);
        return rec;
    }

    /**
     * 获取模型的基模型
     * 
     * @return
     */
    public List<MetaModel> getBases() {
        return bases;
    }

    /**
     * 设置基模型
     * 
     * @param bases
     */
    public void setBases(List<MetaModel> bases) {
        this.bases = bases;
        mro = Mro.calculate(this, bases);
    }

    /**
     * 重新计算MRO
     * 
     * @return
     */
    public List<MetaModel> resetMro() {
        mro = Mro.calculate(this, bases);
        return mro;
    }

    /**
     * 获取MRO（method resolve order）
     * 
     * @return
     */
    public List<MetaModel> getMro() {
        if (mro == null) {
            mro = Mro.calculate(this, bases);
        }
        return mro;
    }

    /**
     * 获取模型的名称，名称是模型的唯一标识，可根据模型名称在模型注册表中找到模型的元数据。
     * 模型可以定义不同的class代码中，根据名称计算继承扩展，当继承与名称相同时，定义为模型的扩展。
     * 模型的多个扩展会全并计算到同一个模型元数据中。
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 模型的标题
     * 
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * 查找指定名称的服务
     * 
     * @param service 服务名
     * @return 找不到时返回null
     */
    public BaseService findService(String service) {
        return services.get(service);
    }

    /**
     * 获取所有服务
     * 
     * @return map
     */
    public Map<String, BaseService> getService() {
        return services;
    }

    /**
     * 执行模型的方法
     * 
     * @param method 方法名
     * @param args   参数
     * @return
     */
    public Object invoke(String method, Object[] args) {
        if (methods.containsKey(method)) {
            for (MetaMethod metaMethod : methods.get(method)) {
                ModelInterceptor.INVOKE_CURRENT.set(true);
                return metaMethod.invoke(args);
            }
        }
        StringBuilder sb = new StringBuilder();
        StringUtils.join(args, ',', o -> o == null ? "null" : o.getClass().getName(), sb);
        throw new ModelException("模型[" + getName() + "]找不到方法:" + method + "(" + sb.toString() + ")");
    }

    /**
     * 执行父方法
     * 
     * @param current
     * @param method
     * @param args
     * @return
     */
    public Object invokeSupper(Class<?> current, String method, Object[] args) {
        if (methods.containsKey(method)) {
            boolean found = false;
            for (MetaMethod metaMethod : methods.get(method)) {
                if (found) {
                    ModelInterceptor.INVOKE_CURRENT.set(true);
                    return metaMethod.invoke(args);
                }
                if (metaMethod.getDeclaringClass() == current) {
                    found = true;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        StringUtils.join(args, ',', o -> o == null ? "null" : o.getClass().getName(), sb);
        throw new ModelException("模型[" + getName() + "]找不到方法:" + method + "(" + sb.toString() + ")");
    }

    /**
     * 查找重写的方法
     * 
     * @param method 方法
     * @return
     */
    public MetaMethod findOverrideMethod(Method method) {
        String key = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> type = method.getDeclaringClass();
        if (methods.containsKey(key)) {
            for (MetaMethod metaMethod : methods.get(key)) {
                if (metaMethod.isParameterMatch(parameterTypes)) {
                    if (metaMethod.getDeclaringClass() == type) {
                        return null;
                    }
                    return metaMethod;
                }
            }
        }
        return null;
    }

    void autoInit(Environment env) {
        Records rec = env.get(getName());
        Cursor cr = env.getCursor();
        SqlDialect sql = cr.getSqlDialect();
        if (auto) {
            boolean mustCreateTable = !sql.tableExists(cr, getTable());
            if (mustCreateTable) {
                sql.createModelTable(cr, getTable(), getDescription());
            }
            Map<String, DbColumn> columns = sql.tableColumns(cr, getTable());
            for (MetaField field : fields.values()) {
                if (!field.isStore()) {
                    continue;
                }
                field.updateDb(rec, columns);
            }
        }

        rec.call(Constants.INIT);
    }

    //
    // setup
    //

    void setupComplete(Environment env) {

        // _init_constraints_onchanges
    }

    void setupFields() {
        for (MetaField field : fields.values()) {
            field.setup(this);
        }
    }

    void setupBase(Environment env) {
        if (setupDone) {
            return;
        }
        fields.clear();
        for (MetaModel cls : getMro()) {
            for (String fname : cls.fields.keySet()) {
                if (!fields.containsKey(fname)) {
                    MetaField field = cls.fields.get(fname);
                    if (!(Boolean) field.args.getOrDefault("automatic", false)) {
                        addField(fname, field.newInstance());
                    }
                }
            }
        }
        addMagicFields();
        if (!env.getRegistry().modules.isEmpty()) {
            env.get("ir.model.field").call("addManualFields", this);
        }
        initDelegates();
        for (String parent : delegates.keySet()) {
            env.getRegistry().get(parent).setupBase(env);
        }
        addDelegateFields(env);
        setupDone = true;
        if (getPresent().length > 0) {
            for (String fname : getPresent()) {
                assert fields.containsKey(fname)
                        : String.format("模型[%s]的present=%s无效", getName(), fname);
            }
        } else if (fields.containsKey(Constants.NAME)) {
            present = new String[] { Constants.NAME };
        }
        if (StringUtils.isEmpty(order)) {
            order = isLogAccess() ? Constants.UPDATE_DATE + " DESC" : Constants.ID + " DESC";
        }
    }

    void addDelegateFields(Environment env) {
        if (isAbstract || delegates.size() == 0) {
            return;
        }
        Registry reg = env.getRegistry();
        for (Entry<String, String> delegate : delegates.entrySet()) {
            for (Entry<String, MetaField> entry : reg.get(delegate.getKey()).getFields().entrySet()) {
                if (!fields.containsKey(entry.getKey())) {
                    MetaField field = entry.getValue();
                    MetaField newField = field.newInstance();
                    newField.inherited = true;
                    newField.inheritedField = field;
                    newField.args.put("related", new String[] { delegate.getValue(), field.getName() });
                    newField.args.put("readonly", field.readonly);
                    newField.args.put("copy", field.copy);
                    addField(field.getName(), newField);
                }
            }
        }
    }

    void initDelegates() {
        for (MetaField field : fields.values()) {
            if (field instanceof Many2oneField) {
                Many2oneField m2o = (Many2oneField) field;
                if (m2o.isDelegate()) {
                    delegates.put(m2o.getComodel(), m2o.getName());
                }
            }
        }
    }

    void addMagicFields() {
        addField(Constants.ID, new IdField().automatic(true).setModule(module));

        addField(Constants.PRESENT,
                Field.Char().label("呈现").search("searchPresent").compute("computePresent").automatic(true)
                        .setModule(module));
        // add display name
        if (isLogAccess()) {
            if (!fields.containsKey(Constants.CREATE_UID)) {
                addField(Constants.CREATE_UID,
                        new Many2oneField(Constants.USER).label("创建人").automatic(true).readonly().setModule(module));
            }
            if (!fields.containsKey(Constants.CREATE_DATE)) {
                addField(Constants.CREATE_DATE,
                        new DateTimeField().label("创建时间").automatic(true).readonly().setModule(module));
            }
            if (!fields.containsKey(Constants.UPDATE_UID)) {
                addField(Constants.UPDATE_UID,
                        new Many2oneField(Constants.USER).label("修改人").automatic(true).readonly().setModule(module));
            }
            if (!fields.containsKey(Constants.UPDATE_DATE)) {
                addField(Constants.UPDATE_DATE,
                        new DateTimeField().label("修改时间").automatic(true).readonly().setModule(module));
            }
        }
        // TODO last_modified
    }

    /**
     * 添加字段
     * 
     * @param name
     * @param field
     */
    public void addField(String name, MetaField field) {
        fields.put(name, field);
        field.setName(this, name);
    }

    @Override
    public String toString() {
        return String.format("MetaModel(%s)", getName());
    }
}
