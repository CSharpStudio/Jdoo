package org.jdoo.base.models;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jdoo.*;
import org.jdoo.core.Constants;
import org.jdoo.data.Cursor;
import org.jdoo.tenants.Tenant;
import org.jdoo.tenants.TenantService;
import org.jdoo.util.KvMap;
import org.jdoo.utils.IoUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

/**
 * 模块, 模块是打包发布的最小单位，一个应用包含一个或多个模块，模块之间会有依赖关系，在安装模块时，自动安装依赖
 *
 * @author
 */
@Model.UniqueConstraint(name = "unique_name", fields = "name")
@Model.Meta(name = "ir.module", label = "模块", present = { "label", "name" }, presentFormat = "{label} ({name})")
public class IrModule extends Model {
	static Field name = Field.Char().label("名称").help("模块的名称").required();
	static Field package_info = Field.Char().label("源码包");
	static Field label = Field.Char().label("标题").required();
	static Field author = Field.Char().label("作者").help("版权所有者");
	static Field state = Field.Selection().label("状态").selection(new HashMap<String, String>() {
		{
			put("uninstallable", "不可安装");
			put("installable", "可安装");
			put("installed", "已安装");
			put("removing", "待卸载");
			put("upgradable", "可更新");
			put("installing", "安装中");
		}
	}).defaultValue("uninstallable");
	static Field image = Field.Image().label("图标").attachment(false);
	static Field latest_version = Field.Char().label("最新版本");
	static Field description = Field.Char().label("说明").help("详细描述模块的功能");
	static Field application = Field.Boolean().label("是否应用");
	static Field license = Field.Char().label("授权");
	static Field model_ids = Field.One2many("ir.model", "module_id");
	static Field category_id = Field.Many2one("ir.module.category").label("分类");
	static Field dependency_ids = Field.One2many("ir.module.dependency", "module_id");

	// #region get/set
	public static String getName(Records rec) {
		return (String) rec.get(name);
	}

	public static void setName(Records rec, String value) {
		rec.set(name, value);
	}

	public static String getPackageInfo(Records rec) {
		return (String) rec.get(package_info);
	}

	public static void setPackageInfo(Records rec, String value) {
		rec.set(package_info, value);
	}

	public static String getLabel(Records rec) {
		return (String) rec.get(label);
	}

	public static void setLabel(Records rec, String value) {
		rec.set(label, value);
	}

	public static String getAuthor(Records rec) {
		return (String) rec.get(author);
	}

	public static void setAuthor(Records rec, String value) {
		rec.set(author, value);
	}

	public static String getState(Records rec) {
		return (String) rec.get(state);
	}

	public static void setState(Records rec, String value) {
		rec.set(state, value);
	}

	public static byte[] getImage(Records rec) {
		return (byte[]) rec.get(image);
	}

	public static void setImage(Records rec, byte[] value) {
		rec.set(image, value);
	}

	public static String getLatestVersion(Records rec) {
		return (String) rec.get(latest_version);
	}

	public static void setLatestVersion(Records rec, String value) {
		rec.set(latest_version, value);
	}

	public static String getDescription(Records rec) {
		return (String) rec.get(description);
	}

	public static void setDescription(Records rec, String value) {
		rec.set(description, value);
	}

	public static Boolean isApplication(Records rec) {
		return (Boolean) rec.get(application);
	}

	public static void setApplication(Records rec, Boolean value) {
		rec.set(application, value);
	}

	public static String getLicense(Records rec) {
		return (String) rec.get(license);
	}

	public static void setLicense(Records rec, String value) {
		rec.set(license, value);
	}

	public static Records getModelIds(Records rec) {
		return (Records) rec.get(model_ids);
	}

	public static void setModelIds(Records rec, Records value) {
		rec.set(model_ids, value);
	}

	public static Records getCategoryId(Records rec) {
		return (Records) rec.get(category_id);
	}

	public static void setCategoryId(Records rec, Records value) {
		rec.set(category_id, value);
	}

	public static Records getDependencyIds(Records rec) {
		return (Records) rec.get(dependency_ids);
	}

	public static void setDependencyIds(Records rec, Records value) {
		rec.set(dependency_ids, value);
	}

	// #endregion

	@Model.ServiceMethod(label = "更新模块清单")
	public Object updateModules(Records rec) {
		Map<String, Manifest> addons = scanAddons("**/addons");
		Records modules = rec.find(rec.criteria("[]"), 0, 0, "");
		Map<String, Records> nameModules = new HashMap<>(modules.size());
		for (Records module : modules) {
			nameModules.put((String) module.get("name"), module);
		}
		List<Map<String, Object>> toCreate = new ArrayList<>();
		Map<Records, KvMap> toUpdate = new HashMap<>(addons.size());
		Map<String, String[]> moduleDepends = new HashMap<>(addons.size());
		for (Entry<String, Manifest> entry : addons.entrySet()) {
			Manifest manifest = entry.getValue();
			KvMap data = toMap(rec, entry.getKey(), manifest);
			String name = manifest.name();
			moduleDepends.put(name, manifest.depends());
			if (nameModules.containsKey(name)) {
				toUpdate.put(nameModules.get(name), data);
			} else {
				if (manifest.autoInstall()) {
					data.put("state", "installed");
				} else {
					data.put("state", "installable");
				}
				toCreate.add(data);
			}
		}
		List<Map<String, Object>> dependsDatas = new ArrayList<>();
		if (!toCreate.isEmpty()) {
			Records newModules = rec.createBatch(toCreate);
			for (Records module : newModules) {
				dependsDatas.addAll(getDepends(module, moduleDepends));
			}
		}
		Cursor cr = rec.getEnv().getCursor();
		for (Entry<Records, KvMap> entry : toUpdate.entrySet()) {
			Records module = entry.getKey();
			module.update(entry.getValue());
			cr.execute("DELETE FROM ir_module_dependency WHERE module_id=%s", Arrays.asList(module.getId()));
			dependsDatas.addAll(getDepends(module, moduleDepends));
		}
		rec.getEnv().get("ir.module.dependency").createBatch(dependsDatas);
		return Action.reload("更新成功");
	}

	KvMap toMap(Records rec, String packageInfo, Manifest manifest) {
		String name = manifest.name();
		KvMap data = new KvMap(16);
		data.put("name", name);
		data.put("package_info", packageInfo);
		data.put("label", manifest.label());
		data.put("description", manifest.description());
		data.put("author", manifest.author());
		data.put("latest_version", manifest.version());
		String icon = manifest.icon();
		ClassLoader loader = ClassUtils.getDefaultClassLoader();
		if (StringUtils.isEmpty(icon)) {
			icon = packageInfo.replaceAll("\\.", "/") + "/statics/icon.png";
		}
		if (loader != null) {
			InputStream input = loader.getResourceAsStream(icon);
			if (input == null) {
				icon = "org/jdoo/base/statics/icon.png";
				input = loader.getResourceAsStream(icon);
			}
			data.put("image", IoUtils.toByteArray(input));
		}
		String category = manifest.category();
		if (StringUtils.isNotBlank(category)) {
			Records cate = rec.getEnv().get("ir.module.category")
					.find(rec.criteria("name", "=", category), 0, 1, "");
			if (!cate.any()) {
				cate = cate.create(new KvMap(1).set("name", category));
			}
			data.put("category_id", cate.getId());
		}
		data.put("license", manifest.license());
		data.put("application", manifest.application());
		return data;
	}

	List<KvMap> getDepends(Records module, Map<String, String[]> moduleDepends) {
		List<KvMap> result = new ArrayList<>();
		String name = (String) module.get("name");
		String[] depends = moduleDepends.get(name);
		if (depends != null) {
			for (String depend : depends) {
				KvMap d = new KvMap(2);
				d.put("name", depend);
				d.put("module_id", module.getId());
				result.add(d);
			}
		}
		return result;
	}

	static final String CLASSPATH_ALL_URL_PREFIX = "classpath*:";
	static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	/**
	 * 扫描包中的{@link Manifest}，
	 * 一个jar包里可以定义多个{@link Manifest}，
	 * 但为了最小粒度部署，建议一个jar包只定义一个{@link Manifest}
	 * 
	 * @param basePackage
	 * @return
	 */
	public Map<String, Manifest> scanAddons(String basePackage) {
		Map<String, Manifest> result = new HashMap<>(16);
		Manifest base = Package.getPackage(Constants.BASE_PACKAGE).getAnnotation(Manifest.class);
		result.put(Constants.BASE_PACKAGE, base);
		try {
			String packageSearchPath = CLASSPATH_ALL_URL_PREFIX +
					ClassUtils.convertClassNameToResourcePath(basePackage) + '/' + DEFAULT_RESOURCE_PATTERN;
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources(packageSearchPath);
			SimpleMetadataReaderFactory readerFactory = new SimpleMetadataReaderFactory();
			for (Resource resource : resources) {
				MetadataReader reader = readerFactory.getMetadataReader(resource);
				AnnotationMetadata am = reader.getAnnotationMetadata();
				if (am.isAnnotated(Manifest.class.getName())) {
					ClassLoader loader = resolver.getClassLoader();
					if (loader != null) {
						Class<?> packageInfo = loader.loadClass(am.getClassName());
						Manifest manifest = packageInfo.getAnnotation(Manifest.class);
						result.put(packageInfo.getPackage().getName(), manifest);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	void installDepends(Records addon) {
		Records deps = (Records) addon.get("dependency_ids");
		List<String> depends = deps.stream().map(d -> (String) d.get("name")).collect(Collectors.toList());
		Records apps = addon.getEnv().get("ir.module").find(Criteria.in("name", depends), 0, -1, null);
		for (Records app : apps) {
			if ("installable".equals(app.get("state"))) {
				app.set("state", "installing");
			}
			installDepends(app);
		}
	}

	/**
	 * 模型安装，在安装模块时，自动安装依赖
	 * 
	 * @param rec
	 * @return
	 */
	@Model.ServiceMethod(label = "安装应用", doc = "安装应用，并安装相关依赖")
	public Object install(Records rec) {
		for (Records addon : rec) {
			if ("installable".equals(addon.get("state"))) {
				addon.set("state", "installing");
			}
			installDepends(addon);
		}
		rec.getEnv().getCursor().commit();

		/** 重置租户 */
		return resetTenant(rec);
	}

	/**
	 * 重启租户，重启后将重新构建模型和加载数据
	 * 
	 * @param rec
	 * @return
	 */
	@Model.ServiceMethod(label = "重启租户")
	public Object resetTenant(Records rec) {
		Tenant tenant = rec.getEnv().getRegistry().getTenant();
		Tenant newTenant = new Tenant(tenant.getKey(), tenant.getName(), tenant.getProperties());
		newTenant.getRegistry();
		TenantService.register(newTenant);
		tenant.getDatabase().close();
		return Action.js("top.window.location.reload()");
	}

	/**
	 * 更新{@link Manifest#data()}中指定的xml文件声明的数据
	 * 
	 * @param rec
	 * @return
	 */
	@Model.ServiceMethod(label = "更新应用数据")
	public Object updateData(Records rec) {
		String pkg = (String) rec.get("package_info");
		rec.getEnv().get("ir.model.data").call("loadData", pkg);
		return Action.reload("更新成功");
	}

	@Model.ServiceMethod(label = "卸载应用", doc = "卸载应用，并卸载相关依赖")
	public Object uninstall(Records rec) {
		for (Records addon : rec) {
			if ("installed".equals(addon.get("state"))) {
				addon.set("state", "removing");
				uninstallDependedBy(addon);
			}
		}
		flush(rec);
		Cursor cr = rec.getEnv().getCursor();
		cr.execute("SELECT name FROM ir_module WHERE state='removing'");
		List<String> removing = cr.fetchAll().stream().map(row -> (String) row[0]).collect(Collectors.toList());
		rec.getEnv().get("ir.model.data").call("removeData", removing);
		cr.commit();

		/** 重置租户 */
		return resetTenant(rec);
	}

	Records getDependedBy(Records rec) {
		return rec.find(
				Criteria.in("dependency_ids", Arrays.asList(rec.getIds())).and(Criteria.equal("state", "installed")));
	}

	void uninstallDependedBy(Records rec) {
		Records dependBy = rec.find(
				Criteria.equal("dependency_ids.name", rec.get("name")).and(Criteria.equal("state", "installed")));
		for (Records module : dependBy) {
			if ("installed".equals(module.get("state"))) {
				module.set("state", "removing");
				uninstallDependedBy(module);
			}
		}
	}
}
