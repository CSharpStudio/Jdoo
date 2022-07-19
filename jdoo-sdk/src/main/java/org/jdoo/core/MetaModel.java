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
import java.util.function.Supplier;

import org.jdoo.Callable;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 模型元数据
 * 
 * @author lrz
 */
public class MetaModel {
    private Logger logger = LogManager.getLogger(BaseModel.class);

    Map<String, MetaField> fields = new LinkedHashMap<>();
    Map<String, List<MetaMethod>> methods;
    Map<String, Object> args = new HashMap<>();
    List<MetaModel> bases = new ArrayList<>();
    List<MetaModel> mro;
    Registry registry;
    Set<String> inheritChildren = new TreeSet<>();
    Map<String, BaseService> services;
    Map<String, UniqueConstraint> uniques;
    Map<String, MethodConstrains> constrains;
    boolean custom;

    boolean auto;
    /** whether model is abstract */
    boolean isAbstract = true;
    /** whether model is transient */
    boolean isTransient = false;
    /** the model name */
    String name;
    /** label for model */
    String label;
    /** the model's informal name */
    String description;
    /** inherited model */
    String inherit;
    /** SQL table name used by model */
    String table;
    /** field to use for labeling records */
    String[] present;
    String presentFormat;
    /** default order for searching results */
    String order = "id";
    /** the many2one field used as parent field */
    String parentName = "parent_id";
    /** set to True to compute parent_path field */
    boolean parentStore = false;
    boolean logAccess = true;
    boolean setupDone;
    String module;

    public Map<String, MetaField> getFields() {
        return fields;
    }

    public Collection<UniqueConstraint> getUniques() {
        return uniques.values();
    }

    public Collection<MethodConstrains> getConstrains() {
        return constrains.values();
    }

    public Registry getRegistry() {
        return registry;
    }

    public boolean isCustom() {
        return custom;
    }

    public String getTable() {
        return table;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public String getDescription() {
        return description;
    }

    public String[] getPresent() {
        if (present == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return present;
    }

    public String getPresentFormat() {
        return presentFormat;
    }

    public String getOrder() {
        return order;
    }

    public boolean isLogAccess() {
        return logAccess;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public String getModule() {
        return module;
    }

    public Records browse(Environment env, String[] ids, Supplier<String[]> prefetchIds) {
        Records rec = new Records(env, this, ids, prefetchIds);
        return rec;
    }

    public List<MetaModel> getBases() {
        return bases;
    }

    public void setBases(List<MetaModel> bases) {
        this.bases = bases;
        mro = Mro.calculate(this, bases);
    }

    public List<MetaModel> resetMro() {
        mro = Mro.calculate(this, bases);
        return mro;
    }

    public List<MetaModel> getMro() {
        if (mro == null) {
            mro = Mro.calculate(this, bases);
        }
        return mro;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public BaseService findService(String service) {
        return services.get(service);
    }

    public Map<String, BaseService> getService() {
        return services;
    }

    public MetaField findField(String field) {
        return fields.get(field);
    }

    public MetaField getField(String field) {
        MetaField f = findField(field);
        if (f == null) {
            throw new ModelException("模型:" + name + " 没定义字段:" + field);
        }
        return f;
    }

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

        addUniqueConstraints(cr);
    }

    void addUniqueConstraints(Cursor cr) {
        SqlDialect sd = cr.getSqlDialect();
        for (UniqueConstraint uc : uniques.values()) {
            try {
                sd.addUniqueConstraint(cr, getTable(), uc.getName(), uc.getFields());
            } catch (Exception exc) {
                exc.printStackTrace();
                logger.warn("模型{}添加唯一约束{}失败:{}", getName(), uc.name, exc.getMessage());
            }
        }
    }

    //
    // setup
    //

    void setupComplete(Environment env) {

        // _init_constraints_onchanges
    }

    void setupFields(Environment env) {
        for (MetaField field : fields.values()) {
            field.setupFull(this, env);
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
        if (!env.getRegistry().initModels.isEmpty()) {
            env.get("ir.model.field").call("addManualFields", this);
        }
        // TODO add manual fields

        setupDone = true;
        if (getPresent().length > 0) {
            for (String fname : getPresent()) {
                assert fields.containsKey(fname)
                        : String.format("模型[%s]的present=%s无效", getName(), fname);
            }
        } else if (fields.containsKey(Constants.NAME)) {
            present = new String[] { Constants.NAME };
        }
    }

    void addMagicFields() {
        addField(Constants.ID, new IdField().automatic(true));

        addField(Constants.PRESENT,
                Field.Char().label("呈现").compute(Callable.method("computePresent")).automatic(true));
        // add display name
        if (isLogAccess()) {
            if (!fields.containsKey(Constants.CREATE_UID)) {
                addField(Constants.CREATE_UID,
                        new Many2oneField(Constants.USER).label("创建人").automatic(true).readonly());
            }
            if (!fields.containsKey(Constants.CREATE_DATE)) {
                addField(Constants.CREATE_DATE, new DateTimeField().label("创建时间").automatic(true).readonly());
            }
            if (!fields.containsKey(Constants.UPDATE_UID)) {
                addField(Constants.UPDATE_UID,
                        new Many2oneField(Constants.USER).label("修改人").automatic(true).readonly());
            }
            if (!fields.containsKey(Constants.UPDATE_DATE)) {
                addField(Constants.UPDATE_DATE, new DateTimeField().label("修改时间").automatic(true).readonly());
            }
        }
        // TODO last_modified
    }

    public void addField(String name, MetaField field) {
        fields.put(name, field);
        field.setupBase(this, name);
    }

    @Override
    public String toString() {
        return String.format("MetaModel(%s)", getName());
    }
}
