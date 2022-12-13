package org.jdoo.base.models;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.jdoo.*;
import org.jdoo.core.Environment;
import org.jdoo.data.Cursor;
import org.jdoo.utils.ImportUtils;
import org.jdoo.utils.ManifestUtils;
import org.jdoo.utils.PathUtils;
import org.jdoo.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

@Model.Meta(name = "ir.model.data", label = "模型数据", order = "module, model, name")
@Model.Service(remove = "@all")
public class IrModelData extends Model {
    private Logger logger = LoggerFactory.getLogger(IrModelData.class);

    static Field name = Field.Char().label("扩展ID").required();
    static Field model = Field.Char().label("模型名称").required();
    static Field module = Field.Char();
    static Field res_id = Field.Char();

    public Records findRef(Records rec, String xmlid) {
        String[] parts = xmlid.split("\\.", 2);
        String sql = "SELECT id, model, res_id FROM ir_model_data WHERE module=%s AND name=%s";
        Cursor cr = rec.getEnv().getCursor();
        cr.execute(sql, Arrays.asList(parts));
        Object[] row = cr.fetchOne();
        if (row.length == 0) {
            return null;
        }
        return rec.getEnv().get((String) row[1]).browse((String) row[2]);
    }

    public void removeData(Records rec, Collection<String> modules) {
        Environment env = rec.getEnv();
        Records moduleData = rec.find(Criteria.in("module", modules), 0, null, "id desc");
        Map<String, List<String>> recordsItems = new HashMap<>();
        List<String> modelIds = new ArrayList<>();
        List<String> fieldIds = new ArrayList<>();
        List<String> selectionIds = new ArrayList<>();
        List<String> constraintIds = new ArrayList<>();
        for (Records r : moduleData) {
            String model = (String) r.get("model");
            String resId = (String) r.get("res_id");
            if ("ir.model".equals(model)) {
                modelIds.add(resId);
            } else if ("ir.model.field".equals(model)) {
                fieldIds.add(resId);
            } else if ("ir.model.field.selection".equals(model)) {
                selectionIds.add(resId);
            } else if ("ir.model.constraint".equals(model)) {
                constraintIds.add(resId);
            } else {
                List<String> lst = recordsItems.get(model);
                if (lst == null) {
                    lst = new ArrayList<>();
                    recordsItems.put(model, lst);
                }
                lst.add(resId);
            }
        }
        List<String> undeletableIds = new ArrayList<>();

        // 先删除非模型相关的记录
        for (Entry<String, List<String>> entry : recordsItems.entrySet()) {
            deleteData(env.get(entry.getKey(), entry.getValue()), moduleData, undeletableIds);
        }

        // 删除模型约束

        // 删除字段
        // deleteData(env.get("ir.model.field.selection", selectionIds), moduleData,
        // undeletableIds);
        deleteData(env.get("ir.model.field", fieldIds), moduleData, undeletableIds);
        // ir.model.relation

        deleteData(env.get("ir.model", modelIds), moduleData, undeletableIds);

        if (undeletableIds.size() > 0) {
            logger.info("ir.model.data could not be deleted in ({})", undeletableIds);
        }

        Set<String> dataIds = SetUtils.hashSet(moduleData.getIds());
        for (Records data : rec.browse(undeletableIds).exists()) {
            Records record = env.get((String) data.get("model"), (String) data.get("res_id"));
            try {
                if (record.exists().any()) {
                    dataIds.remove(data.getId());
                }
            } catch (Exception exc) {
                // 表可能已经不存在
            }
        }
        rec.browse(dataIds).delete();
    }

    public void deleteData(Records rec, Records moduleData, List<String> undeletableIds) {
        Records refData = moduleData
                .find(Criteria.equal("model", rec.getMeta().getName()).and(Criteria.in("res_id", rec.getIds())));
        Set<String> refIds = SetUtils.hashSet(refData.getIds());
        refIds.removeAll(Arrays.asList(moduleData.getIds()));
        Set<String> externalIds = moduleData.browse(refIds).stream().map(r -> (String) r.get("res_id"))
                .collect(Collectors.toSet());
        for (String id : rec.getIds()) {
            if (!externalIds.contains(id)) {
                try {
                    rec.browse(id).delete();
                } catch (Exception exc) {
                    undeletableIds.addAll(Arrays.asList(refData.getIds()));
                }
            }
        }
    }

    public void loadData(Records rec, String packageName) {
        Manifest manifest = ManifestUtils.getManifest(packageName);
        String[] files = manifest.data();
        for (String file : files) {
            if (StringUtils.isNoneBlank(file)) {
                String path = PathUtils.combine(packageName.replaceAll("\\.", "/"), file);
                ClassLoader loader = ClassUtils.getDefaultClassLoader();
                if (loader != null) {
                    InputStream input = loader.getResourceAsStream(path);
                    if (input != null) {
                        String pathLower = path.toLowerCase();
                        if (pathLower.endsWith(".xml")) {
                            ImportUtils.importXml(input, manifest.name(), rec.getEnv(), (m, e) -> {
                                logger.warn(m, e);
                            });
                        } else if (pathLower.endsWith(".csv")) {
                        }
                    } else {
                        logger.warn("找不到文件:" + file);
                    }
                }
            }
        }
    }
}
