package org.jdoo.base.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jdoo.*;
import org.jdoo.core.Constants;
import org.jdoo.core.Loader;
import org.jdoo.data.Cursor;
import org.jdoo.tenants.Tenant;
import org.jdoo.tenants.TenantService;
import org.jdoo.util.KvMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

/**
 * 模块
 *
 * @author
 */
@Model.UniqueConstraint(name = "unique_name", fields = "name")
@Model.Meta(name = "ir.module", description = "模块")
public class IrModule extends Model {
	static Field name = Field.Char().label("名称").help("模块的名称").required();
	static Field package_info = Field.Char().label("源码包");
	static Field summary = Field.Char().label("摘要");
	static Field author = Field.Char().label("作者").help("版权所有者");
	static Field state = Field.Selection().label("状态").selection(Selection.value(new HashMap<String, String>() {
		{
			put("installed", "已安装");
			put("installable", "可安装");
		}
	}));
	static Field icon = Field.Char().label("图标");
	static Field latest_version = Field.Char().label("最新版本");
	static Field description = Field.Char().label("说明").help("详细描述模块的功能");
	static Field application = Field.Boolean().label("是否应用");
	static Field license = Field.Char().label("授权");
	static Field model_ids = Field.One2many("ir.model", "module_id");
	static Field category_id = Field.Many2one("ir.module.category").label("分类");
	static Field dependency_ids = Field.One2many("ir.module.dependency", "module_id");

	@Model.ServiceMethod(label = "更新模块清单")
	public Object updateModules(Records rec) {
		Map<String, Manifest> addons = scanAddons("**/addons");
		Records modules = rec.find(rec.criteria("[]"), 0, 0, "");
		Map<String, Records> nameModules = new HashMap<>(modules.size());
		for (Records module : modules) {
			nameModules.put((String) module.get("name"), module);
		}
		List<KvMap> toCreate = new ArrayList<>();
		Map<Records, KvMap> toUpdate = new HashMap<>(addons.size());
		Map<String, String[]> moduleDepends = new HashMap<>(addons.size());
		for (Entry<String, Manifest> entry : addons.entrySet()) {
			Manifest manifest = entry.getValue();
			String name = manifest.name();
			KvMap data = new KvMap(16);
			data.put("name", name);
			data.put("package_info", entry.getKey());
			data.put("summary", manifest.summary());
			data.put("description", manifest.description());
			data.put("author", manifest.author());
			data.put("latest_version", manifest.version());
			String category = manifest.category();
			if (StringUtils.isNotBlank(category)) {
				Records cate = rec.getEnv().get("ir.module.category")
						.find(rec.criteria("name", "=", category), 0, 1, "");
				if (!cate.any()) {
					cate = cate.create(new KvMap(1).set("name", category));
				}
				data.put("category_id", cate.getId());
			}
			moduleDepends.put(name, manifest.depends());
			data.put("license", manifest.license());
			data.put("application", manifest.application());
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
		List<KvMap> dependsDatas = new ArrayList<>();
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
					Class<?> packageInfo = resolver.getClassLoader().loadClass(am.getClassName());
					Manifest manifest = packageInfo.getAnnotation(Manifest.class);
					result.put(packageInfo.getPackage().getName(), manifest);
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

	@Model.ServiceMethod(label = "安装")
	public Object install(Records rec) {
		for (Records addon : rec) {
			if ("installable".equals(addon.get("state"))) {
				addon.set("state", "installing");
			}
			installDepends(addon);
		}

		/** 重置租户 */
		Tenant tenant = rec.getEnv().getRegistry().getTenant();
		Tenant newTenant = new Tenant(tenant.getKey(), tenant.getName(), tenant.getProperties());
		TenantService.register(newTenant);

		return Action.js("top.window.location.reload()");
	}

	@Model.ServiceMethod(label = "重启租户")
	public Object resetTenant(Records rec) {
		Tenant tenant = rec.getEnv().getRegistry().getTenant();
		Tenant newTenant = new Tenant(tenant.getKey(), tenant.getName(), tenant.getProperties());
		TenantService.register(newTenant);

		return Action.js("top.window.location.reload()");
	}

	@Model.ServiceMethod(label = "更新应用数据")
	public Object updateData(Records rec) {
		String pkg = (String) rec.get("package_info");
		Loader.getLoader().loadData(rec.getEnv(), pkg);
		return Action.reload("更新成功");
	}
}
