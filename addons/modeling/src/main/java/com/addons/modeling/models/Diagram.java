package com.addons.modeling.models;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jdoo.*;
import org.jdoo.core.Constants;
import org.jdoo.core.Environment;
import org.jdoo.core.MetaField;
import org.jdoo.data.Cursor;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.exceptions.ValueException;
import org.jdoo.fields.RelationalMultiField;
import org.jdoo.util.KvMap;
import org.jdoo.utils.ManifestUtils;
import org.jdoo.utils.StringUtils;

@Model.Meta(name = "modeling.diagram", label = "模型图")
public class Diagram extends Model {
    static Field name = Field.Char().label("名称");
    static Field module_id = Field.Many2one("ir.module").label("模块");
    static Field shape_ids = Field.One2many("modeling.diagram_shape", "diagram_id");
    static Field width = Field.Integer().label("宽").defaultValue(2000);
    static Field height = Field.Integer().label("高").defaultValue(1600);

    @Model.ServiceMethod(auth = "read", records = false)
    public List<Map<String, Object>> loadModels(Records rec, String moduleId, List<String> fields) {
        return rec.getEnv().get("modeling.model").search(fields, Criteria.equal("module_id", moduleId));
    }

    @Model.ServiceMethod(auth = "update")
    public String addModel(Records rec, Map<String, Object> values) {
        rec.ensureOne();
        Object x = values.remove("x");
        Object y = values.remove("y");
        Object bgcolor = values.remove("bgcolor");
        values.put("module_id", rec.getRel("module_id").getId());
        Records model = rec.getEnv().get("modeling.model").create(values);
        rec.getEnv().get("modeling.diagram_shape").create(new KvMap().set("diagram_id", rec.getId())
                .set("model_id", model.getId()).set("x", x).set("y", y));
        return model.getId();
    }

    @Model.ServiceMethod(auth = "update")
    public Map<String, Object> linkModel(Records rec, String modelId, String x, String y) {
        rec.ensureOne();
        rec.getEnv().get("modeling.diagram_shape").create(new KvMap().set("diagram_id", rec.getId())
                .set("model_id", modelId).set("x", x).set("y", y));
        Records model = rec.getEnv().get("modeling.model", modelId);
        List<String> modelFields = model.getMeta().getFields().values().stream()
                .filter(f -> !f.isAuto() && !(f instanceof RelationalMultiField)).map(f -> f.getName())
                .collect(Collectors.toList());
        Map<String, Object> result = model.read(modelFields).get(0);
        Records fields = model.getRel("field_ids");
        List<String> fnames = fields.getMeta().getFields().values().stream().filter(f -> !f.isAuto())
                .map(f -> f.getName()).collect(Collectors.toList());
        result.put("x", x);
        result.put("y", y);
        result.put("fields", fields.read(fnames));
        return result;
    }

    @Model.ServiceMethod(auth = "update")
    public void unlinkModel(Records rec, String modelId) {
        rec.ensureOne();
        rec.getEnv().get("modeling.diagram_shape")
                .find(Criteria.equal("diagram_id", rec.getId()).and(Criteria.equal("model_id", modelId))).delete();
    }

    @Model.ServiceMethod(auth = "update", records = false)
    public void updateModel(Records rec, Map<String, Object> values) {
        String modelId = (String) values.remove("id");
        rec.getEnv().get("modeling.model", modelId).update(values);
    }

    @Model.ServiceMethod(auth = "update", records = false)
    public String addModelField(Records rec, Map<String, Object> values) {
        return rec.getEnv().get("modeling.model.field").create(values).getId();
    }

    @Model.ServiceMethod(auth = "update")
    public void updateModelPosition(Records rec, String modelId, String x, String y) {
        rec.ensureOne();
        rec.getEnv().get("modeling.diagram_shape")
                .find(Criteria.equal("model_id", modelId).and(Criteria.equal("diagram_id", rec.getId())))
                .update(new KvMap().set("x", x).set("y", y));
    }

    @Model.ServiceMethod(auth = "update", records = false)
    public void deleteModel(Records rec, String modelId) {
        rec.getEnv().get("modeling.model", modelId).delete();
    }

    @Model.ServiceMethod(auth = "update", records = false)
    public String addModule(Records rec, Map<String, Object> values) {
        Records module = rec.getEnv().get("ir.module").create(values);
        return module.getId();
    }

    @Model.ServiceMethod(auth = "update", records = false)
    public void updateModule(Records rec, Map<String, Object> values) {
        String moduleId = (String) values.remove("id");
        rec.getEnv().get("ir.module", moduleId).update(values);
    }

    @Model.ServiceMethod(auth = "update", records = false)
    public void deleteModule(Records rec, String moduleId) {
        rec.getEnv().get("ir.module", moduleId).delete();
    }

    @Model.ServiceMethod(auth = "read")
    public List<Map<String, Object>> load(Records rec) {
        rec.ensureOne();
        Environment env = rec.getEnv();
        Cursor cr = env.getCursor();
        String f1 = env.getRegistry().get("modeling.diagram_shape").getFields().values().stream()
                .filter(f -> !f.isAuto() && !(f instanceof RelationalMultiField)).map(f -> "a." + cr.quote(f.getName()))
                .collect(Collectors.joining(","));
        String f2 = env.getRegistry().get("modeling.model").getFields().values().stream()
                .filter(f -> !f.isAuto() && !(f instanceof RelationalMultiField)).map(f -> "b." + cr.quote(f.getName()))
                .collect(Collectors.joining(","));
        String sql = String.format(
                "SELECT b.id,%s,%s FROM modeling_diagram_shape a JOIN modeling_model b on a.model_id=b.id WHERE a.diagram_id = %%s",
                f1, f2);
        cr.execute(sql, Arrays.asList(rec.getId()));
        List<Map<String, Object>> result = cr.fetchMapAll();
        List<String> modelIds = result.stream().map(m -> (String) m.get("id")).collect(Collectors.toList());
        Records fields = env.get("modeling.model.field").find(Criteria.in("model_id", modelIds));
        List<String> fnames = fields.getMeta().getFields().values().stream().filter(f -> !f.isAuto())
                .map(f -> f.getName()).collect(Collectors.toList());
        List<Map<String, Object>> fieldRows = fields.read(fnames);
        Map<String, List<Map<String, Object>>> modelField = new HashMap<>();
        for (Map<String, Object> row : fieldRows) {
            String modelId = (String) row.get("model_id");
            List<Map<String, Object>> list = modelField.get(modelId);
            if (list == null) {
                list = new ArrayList<>();
                modelField.put(modelId, list);
            }
            list.add(row);
        }
        result.forEach(r -> {
            String modelId = (String) r.get("id");
            r.put("fields", modelField.get(modelId));
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    @Model.ServiceMethod(auth = "read", records = false)
    public List<Map<String, String>> getCode(Records rec, List<String> modelIds) {
        return (List<Map<String, String>>) rec.getEnv().get("modeling.model", modelIds).call("getCode");
    }

    @Model.ServiceMethod(auth = "update")
    public void reflect(Records rec) {
        rec.ensureOne();
        String moduleId = rec.getRel("module_id").getId();
        String pkg = (String) rec.getRel("module_id").get("package_info");
        if (StringUtils.isEmpty(pkg)) {
            throw new ValidationException(rec.l10n("模块%s未维护源码包", rec.getRel("module_id").get("name")));
        }
        Manifest manifest = null;
        try {
            manifest = ManifestUtils.getManifest(pkg);
        } catch (ValueException e) {
            throw new ValidationException(rec.l10n("读取源码包%s失败", pkg));
        }
        Records model = rec.getEnv().get("modeling.model");
        Records shape = rec.getEnv().get("modeling.diagram_shape");
        int dx = 50;
        int dy = 50;
        int x = 50;
        int y = 50;
        for (Class<?> clazz : manifest.models()) {
            Model.Meta meta = clazz.getAnnotation(Model.Meta.class);
            String name = meta.name();
            if (StringUtils.isEmpty(name)) {
                String[] inherits = meta.inherit();
                if (inherits.length > 0) {
                    name = inherits[0];
                }
            }
            Records exists = model.find(
                    Criteria.equal("model", name).and(Criteria.equal("module_id", moduleId)));
            boolean createShape = true;
            if (exists.any()) {
                updateModel(exists, meta, clazz);
                Records s = shape.find(
                        Criteria.equal("model_id", exists.getId()).and(Criteria.equal("diagram_id", rec.getId())));
                if (s.any()) {
                    createShape = false;
                }
            } else {
                exists = createModel(model, name, moduleId, meta, clazz);
            }
            if (createShape) {
                shape.create(new KvMap().set("model_id", exists.getId()).set("diagram_id", rec.getId()).set("x", x)
                        .set("y", y));
                x += 300;
                if (x > 1800) {
                    x = dx;
                    y += dx + 300;
                }
                if (y > 1200) {
                    dx += 50;
                    dy += 50;
                    x = dx;
                    y = dy;
                }
            }
        }
    }

    Records createModel(Records model, String name, String moduleId, Model.Meta meta, Class<?> clazz) {
        String label = meta.label();
        if (StringUtils.isEmpty(label)) {
            label = clazz.getSimpleName();
        }
        Records result = model.create(new KvMap().set("name", label)
                .set("model", name)
                .set("description", meta.description())
                .set("inherit", String.join(",", meta.inherit()))
                .set("table", meta.table())
                .set("order", meta.order())
                .set("present_fields", String.join(",", meta.present()))
                .set("present_format", meta.presentFormat())
                .set("is_transient", TransientModel.class.isAssignableFrom(clazz))
                .set("is_abstract", AbstractModel.class.isAssignableFrom(clazz))
                .set("model_class", clazz.getSimpleName())
                .set("module_id", moduleId));

        Records field = model.getEnv().get("modeling.model.field");
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        for (java.lang.reflect.Field f : fields) {
            if (MetaField.class.isAssignableFrom(f.getType()) && Modifier.isStatic(f.getModifiers())) {
                try {
                    f.setAccessible(true);
                    MetaField mf = (MetaField) f.get(null);
                    createField(field, f.getName(), mf, result.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO log the error
                    // String.format("模型[%s]读取Field[%s]失败:%s", getClass().getName(), f.getName(),
                    // e.getMessage());
                }
            }
        }
        return result;
    }

    void updateModel(Records model, Model.Meta meta, Class<?> clazz) {
        String name = meta.label();
        if (StringUtils.isEmpty(name)) {
            name = clazz.getSimpleName();
        }
        model.set("name", name);
        model.set("description", meta.description());
        model.set("inherit", String.join(",", meta.inherit()));
        model.set("table", meta.table());
        model.set("order", meta.order());
        model.set("present_fields", String.join(",", meta.present()));
        model.set("present_format", meta.presentFormat());
        model.set("is_transient", TransientModel.class.isAssignableFrom(clazz));
        model.set("is_abstract", AbstractModel.class.isAssignableFrom(clazz));
        model.set("model_class", clazz.getSimpleName());

        Records field = model.getEnv().get("modeling.model.field");
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        for (java.lang.reflect.Field f : fields) {
            if (MetaField.class.isAssignableFrom(f.getType()) && Modifier.isStatic(f.getModifiers())) {
                try {
                    f.setAccessible(true);
                    MetaField mf = (MetaField) f.get(null);
                    Records exists = field
                            .find(Criteria.equal("name", f.getName()).and(Criteria.equal("model_id", model.getId())));
                    if (exists.any()) {
                        updateField(exists, mf);
                    } else {
                        createField(field, f.getName(), mf, model.getId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO log the error
                    // String.format("模型[%s]读取Field[%s]失败:%s", getClass().getName(), f.getName(),
                    // e.getMessage());
                }
            }
        }
    }

    Records createField(Records field, String name, MetaField meta, String modelId) {
        Set<String> fnames = field.getMeta().getFields().values().stream().filter(f -> !f.isAuto())
                .map(f -> f.getName()).collect(Collectors.toSet());
        Map<String, Object> map = getFieldArgs(meta, fnames);
        map.put("name", name);
        map.put("model_id", modelId);
        map.put("field_type", meta.getType());
        return field.create(map);
    }

    void updateField(Records field, MetaField meta) {
        Set<String> fnames = field.getMeta().getFields().values().stream().filter(f -> !f.isAuto())
                .map(f -> f.getName()).collect(Collectors.toSet());
        Map<String, Object> map = getFieldArgs(meta, fnames);
        map.put("field_type", meta.getType());
        field.update(map);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> getFieldArgs(MetaField meta, Set<String> fnames) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Entry<String, Object> e : meta.getArgs().entrySet()) {
            if (Constants.RELATED.equals(e.getKey())) {
                map.put(Constants.RELATED, String.join(".", (String[]) e.getValue()));
            } else if (Constants.DEPENDS.equals(e.getKey())) {
                map.put(Constants.DEPENDS, String.join(",", (List<String>) e.getValue()));
            } else if ("ondelete".equals(e.getKey())) {
                map.put("on_delete", e.getValue().toString());
            } else if ("comodelName".equals(e.getKey())) {
                map.put("relation", e.getValue());
            } else if ("defaultValue".equals(e.getKey())) {
                Default dv = (Default) e.getValue();
                map.put("default_value", dv.toString());
            } else if ("compute".equals(e.getKey())) {
                Callable c = (Callable) e.getValue();
                map.put("compute", c.toString());
            } else if ("relation".equals(e.getKey())) {
                map.put("relation_table", e.getValue());
            } else if ("inverseName".equals(e.getKey())) {
                map.put("relation_field", e.getValue());
            } else if (fnames.contains(e.getKey())) {
                Object val = e.getValue();
                if (val instanceof Boolean) {
                    val = val.toString();
                }
                map.put(e.getKey(), val);
            }
        }
        return map;
    }
}
