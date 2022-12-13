package org.jdoo.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.jdoo.Manifest;
import org.jdoo.Model;
import org.jdoo.Records;
import org.jdoo.Model.Service;
import org.jdoo.exceptions.ModelException;
import org.jdoo.exceptions.TypeException;
import org.jdoo.services.MethodService;
import org.jdoo.utils.StringUtils;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * 模型建构
 * 
 * @author lrz
 */
public class ModelBuilder {
    private Logger logger = LoggerFactory.getLogger(BaseModel.class);

    private static ModelBuilder instance;

    public static void setBuilder(ModelBuilder builder) {
        instance = builder;
    }

    public static ModelBuilder getBuilder() {
        if (instance == null) {
            instance = new ModelBuilder();
        }
        return instance;
    }

    /**
     * 构建基模型，所有模型都是继承自基模型，基模型提供基础CURD方法
     * 
     * @param reg
     * @return
     */
    public MetaModel buildBaseModel(Registry registry) {
        MetaModel model = new MetaModel();
        model.module = Constants.BASE;
        MetaModel base = toMeta(new BaseModel(), model.module);
        model.name = Constants.BASE;
        model.setBases(Arrays.asList(base));
        buildModelAttributes(registry, model);
        model.registry = registry;
        registry.put(Constants.BASE, model);
        return model;
    }

    /**
     * 构建模型，基础java类定义的模型，生成元模型{@link MetaModel}，注册到{@link Registry}中
     * 
     * @param registry
     * @param clazz    模型类型，必需是{@link BaseModel}的子类
     * @return
     */
    public MetaModel buildModel(Registry registry, Class<?> clazz, String module) {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            throw new ModelException("模型[" + clazz.getName() + "]不能声明为抽象类");
        }
        try {
            BaseModel model = (BaseModel) clazz.getDeclaredConstructor().newInstance();
            MetaModel meta = buildModel(registry, model, module);
            registry.put(meta.getName(), meta);
            return meta;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ModelException("构建模型[" + clazz.getName() + "]失败", e);
        }
    }

    public MetaModel buildModel(Registry registry, BaseModel cls, String module) {
        // 计算parents和name,
        // 1：inherit为空时，是新模型定义；2：inherit包含name时，是扩展；3：inherit不包含name时，是继承
        List<String> parents = cls.getRefector().getInherit();
        String name = cls.getRefector().getName();
        if (StringUtils.isBlank(name)) {
            name = parents.size() == 1 ? parents.get(0) : cls.getClass().getName();
        }
        // 所有模型都继承自base
        if (name != Constants.BASE) {
            parents.add(Constants.BASE);
        }

        MetaModel meta;
        if (parents.contains(name)) {
            // 扩展注册表中的模型
            if (!registry.map.containsKey(name)) {
                throw new TypeException(String.format("class[%s]定义的模型[%s]不存在于注册表", cls.getClass(), name));
            }
            meta = registry.get(name);
            buildModelCheckBase(meta, cls);
        } else {
            meta = new MetaModel();
            meta.name = name;
            meta.isAbstract = cls.isAbstract;
            meta.auto = cls.isAuto;
            meta.isTransient = cls.isTransient;
            meta.module = module;
            meta.custom = cls.custom;
        }
        // 构建模型的bases
        List<MetaModel> bases = new ArrayList<MetaModel>();
        bases.add(toMeta(cls, module));
        for (String parent : parents) {
            if (!registry.contains(parent)) {
                throw new TypeException(String.format("class[%s]定义的继承模型[%s]不存在于注册表", cls.getClass(), parent));
            }
            MetaModel parentMeta = registry.get(parent);
            if (parent == name) {
                for (MetaModel base : parentMeta.getBases()) {
                    // 放到列表的最后
                    bases.remove(base);
                    bases.add(base);
                }
            } else {
                buildModelCheckParent(meta, cls, parentMeta);
                bases.remove(parentMeta);
                bases.add(parentMeta);
                parentMeta.inheritChildren.add(name);
            }
        }
        meta.setBases(bases);
        buildModelAttributes(registry, meta);
        meta.registry = registry;
        registry.put(name, meta);
        return meta;
    }

    private void buildModelCheckBase(MetaModel meta, BaseModel model) {
        if (meta.isAbstract && !model.isAbstract) {
            throw new TypeException(String.format("class[%s]把抽象模型[%S]转成非抽象模型。应该继承AbstractModel或者setName设置新的名称",
                    model.getClass().getName(), meta.name));
        }
        if (meta.isTransient != model.isTransient) {
            if (meta.isTransient) {
                throw new TypeException(
                        String.format("class[%s]把瞬间模型[%s]转成非瞬间模型。应该继承TransientModel或者setName设置新的名称",
                                model.getClass().getName(), meta.name));
            }
            throw new TypeException(String.format("class[%s]把非瞬间模型[%s]转成瞬间模型。应该继承Model或者setName设置新的名称",
                    model.getClass().getName(), meta.name));
        }
    }

    private void buildModelCheckParent(MetaModel meta, BaseModel model, MetaModel parentMeta) {
        if (meta.isAbstract && !parentMeta.isAbstract) {
            throw new TypeException(String.format("class[%s]中，抽象模型[%s]不能继承自非抽象模型[%s]", model.getClass().getName(),
                    meta.name, parentMeta.name));
        }
    }

    @SuppressWarnings("unchecked")
    private void buildModelAttributes(Registry registry, MetaModel meta) {
        meta.description = meta.name;
        meta.label = meta.name;
        meta.table = meta.name.replace('.', '_');
        meta.logAccess = meta.auto;
        meta.methods = new HashMap<>(16);
        meta.services = new HashMap<>(16);
        meta.uniques = new HashMap<>(16);
        meta.constrains = new HashMap<>(16);
        List<MetaModel> mro = meta.getMro();
        for (int i = mro.size() - 1; i > 0; i--) {
            MetaModel base = mro.get(i);
            if (base.registry == null) {
                meta.label = (String) base.args.getOrDefault(Constants.LABEL, meta.label);
                meta.description = (String) base.args.getOrDefault(Constants.DESCRIPTION, meta.description);
                meta.authModel = (String) base.args.getOrDefault(Constants.AUTH_MODEL, meta.authModel);
                meta.table = (String) base.args.getOrDefault(Constants.TABLE, meta.table);
                meta.logAccess = (Boolean) base.args.getOrDefault(Constants.LOG_ACCESS, meta.logAccess);
                meta.present = (String[]) base.args.getOrDefault(Constants.PRESENT, meta.present);
                meta.presentFormat = (String) base.args.getOrDefault(Constants.PRESENT_FORMAT, meta.presentFormat);
                meta.order = (String) base.args.getOrDefault(Constants.ORDER, meta.order);
                // 先移除
                for (Service svc : (Service[]) base.args.get(Constants.SERVICE_KEY)) {
                    String[] removes = svc.remove();
                    if (removes.length > 0) {
                        for (String remove : removes) {
                            if ("@all".equals(remove)) {
                                meta.services.clear();
                            } else {
                                meta.services.remove(remove);
                            }
                        }
                    }
                }
                // 再添加
                for (Service svc : (Service[]) base.args.get(Constants.SERVICE_KEY)) {
                    if (svc.remove().length == 0) {
                        updateServices(meta, svc);
                    }
                }
                for (Method method : (List<Method>) base.args.get(Constants.METHOD_KEY)) {
                    updateMethods(meta, method);
                    Model.Constrains constrains = method.getAnnotation(Model.Constrains.class);
                    if (constrains != null) {
                        String methodName = method.getName();
                        meta.constrains.put(methodName, new MethodConstrains(methodName, constrains.value()));
                    }
                    Model.ServiceMethod service = method.getAnnotation(Model.ServiceMethod.class);
                    if (service != null) {
                        String serviceName = service.name();
                        if (StringUtils.isBlank(serviceName)) {
                            serviceName = method.getName();
                        }
                        meta.services.put(serviceName, new MethodService(serviceName, service, method));
                    }
                }
                for (UniqueConstraint uc : (List<UniqueConstraint>) base.args.get(Constants.UNIQUE_KEY)) {
                    meta.uniques.put(uc.getName(), uc);
                }
            }
        }
        // recompute attributes of _inherit_children models
        for (String childName : meta.inheritChildren) {
            MetaModel childMeta = registry.get(childName);
            childMeta.resetMro();
            buildModelAttributes(registry, childMeta);
        }
    }

    private void updateMethods(MetaModel meta, Method method) {
        String methodName = method.getName();
        List<MetaMethod> methods = meta.methods.get(methodName);
        if (methods == null) {
            methods = new ArrayList<>();
            meta.methods.put(methodName, methods);
        }
        methods.add(0, new MetaMethod(method));
    }

    private void updateServices(MetaModel meta, Service svc) {
        Class<?> c = svc.type();
        if (BaseService.class.isAssignableFrom(svc.type())) {
            try {
                BaseService bs = (BaseService) c.getConstructor().newInstance();
                bs.setName(svc.name());
                bs.setLabel(svc.label());
                bs.setAuth(svc.auth());
                bs.setDescription(svc.description());
                meta.services.put(svc.name(), bs);
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn(String.format("创建模型[%s]类型为[%s]的服务[%s]失败", meta.name, svc.name(), c.getName()), e);
            }
        }
    }

    private MetaModel toMeta(BaseModel model, String module) {
        MetaModel meta = new MetaModel();
        meta.args = new HashMap<String, Object>(model.getRefector().getArgs());
        Class<?> clazz = model.getClass();
        meta.name = clazz.getName();
        // methods
        List<Method> methods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                Class<?>[] types = method.getParameterTypes();
                if (types.length > 0 && types[0] == Records.class) {
                    methods.add(method);
                }
            }
        }
        meta.args.put(Constants.METHOD_KEY, methods);
        // fields
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (MetaField.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    MetaField f = (MetaField) field.get(null);
                    String key = field.getName();
                    f.setName(key);
                    f.setModule(module);
                    meta.fields.put(key, f);
                } catch (Exception e) {
                    throw new ModelException(
                            String.format("模型[%s]读取Field[%s]失败:%s", getClass().getName(), field.getName(),
                                    e.getMessage()));
                }
            }
        }
        // services
        Service[] services = clazz.getAnnotationsByType(Model.Service.class);
        for (Service svc : services) {
            String[] remove = svc.remove();
            if (remove.length == 0) {
                if (!BaseService.class.isAssignableFrom(svc.type())) {
                    logger.warn(String.format("class[%s]声明的服务[%s]类型[%s]无效,应为BaseService的子类", clazz.getName(),
                            svc.name(), svc.type().getName()));
                }
            }
        }
        meta.args.put(Constants.SERVICE_KEY, services);
        // unique constraint
        Model.UniqueConstraint[] uniqueConstraints = clazz.getAnnotationsByType(Model.UniqueConstraint.class);
        List<UniqueConstraint> uniques = new ArrayList<>();
        for (Model.UniqueConstraint uc : uniqueConstraints) {
            uniques.add(new UniqueConstraint(uc.name(), uc.fields(), uc.message(), module));
        }
        meta.args.put(Constants.UNIQUE_KEY, uniques);

        return meta;
    }

    public Collection<String> buildModule(Registry registry, Manifest manifest) {
        List<String> models = new ArrayList<>();
        for (Class<?> clazz : manifest.models()) {
            MetaModel meta = buildModel(registry, clazz, manifest.name());
            models.add(meta.getName());
            models.addAll(meta.inheritChildren);
        }
        return models;
    }
}
