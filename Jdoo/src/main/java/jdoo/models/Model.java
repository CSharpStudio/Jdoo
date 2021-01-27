package jdoo.models;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.util.StringUtils;

import jdoo.tools.Default;
import jdoo.tools.Dict;
import jdoo.tools.IdValues;
import jdoo.tools.Tuple;
import jdoo.tools.Tuple2;
import jdoo.tools.Utils;
import jdoo.data.Cursor;
import jdoo.apis.Cache;
import jdoo.apis.Environment;
import jdoo.apis.api;
import jdoo.data.AsIs;
import jdoo.exceptions.MissingErrorException;
import jdoo.exceptions.ModelException;
import jdoo.modules.Registry;

public class Model {
    MetaModel meta;
    static List<String> LOG_ACCESS_COLUMNS = Arrays.asList("create_uid", "create_date", "write_uid", "write_date");
    Map<Field, Object> _field_computed = new HashMap<>();

    protected boolean _auto = false; // don't create any database backend
    protected boolean _register = false; // not visible in ORM registry
    protected boolean _abstract = true; // whether model is abstract
    protected boolean _transient = false; // whether model is transient

    protected String _name; // the model name
    protected String _description; // the model's informal name
    protected boolean _custom = false; // should be True for custom models only

    protected String _inherit; // Python-inherited models ('model' or ['model'])
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

    protected Self _create(Self self, Collection<Dict> data_list) {
        List<String> ids = new ArrayList<>();
        List<Field> other_fields = new ArrayList<>();
        // List<String> translated_fields = new ArrayList<>();
        Cursor cr = self.env().cr();
        for (Dict data : data_list) {
            Dict stored = (Dict) data.get("stored");
            StringBuilder columns = new StringBuilder();
            StringBuilder formats = new StringBuilder();
            List<Object> values = new ArrayList<>();
            if (!stored.containsKey("id")) {
                String id = UUID.randomUUID().toString();
                append_value(columns, formats, values, "id", "%s", id);
                ids.add(id);
            } else {
                ids.add((String) stored.get("id"));
            }
            if (!stored.containsKey("create_uid")) {
                append_value(columns, formats, values, "create_uid", "%s", self.env().uid());
            }
            if (!stored.containsKey("create_date")) {
                append_value(columns, formats, values, "create_date", "%s", new AsIs("(now() at time zone 'UTC')"));
            }
            if (!stored.containsKey("write_uid")) {
                append_value(columns, formats, values, "write_uid", "%s", self.env().uid());
            }
            if (!stored.containsKey("write_date")) {
                append_value(columns, formats, values, "write_date", "%s", new AsIs("(now() at time zone 'UTC')"));
            }
            for (String name : stored.keySet()) {
                Field field = self.getField(name);
                if (field.column_type() != null) {
                    Object col_val = field.convert_to_column(stored.get(name), self, stored, true);
                    append_value(columns, formats, values, name, field.column_format, col_val);
                } else {
                    other_fields.add(field);
                }
            }
            String query = "INSERT INTO \"" + self.table() + "\" (" + columns + ") VALUES (" + formats + ")";
            cr.execute(query, values);
        }
        Self records = self.browse(ids);
        Cache cache = self.env().cache();
        for (Self record : records) {
            for (Dict data : data_list) {
                Dict stored = (Dict) data.get("stored");
                for (String fname : stored.keySet()) {
                    Field field = self.getField(fname);
                    Object value = stored.get(fname);
                    if (field instanceof One2manyField || field instanceof Many2manyField) {
                        if (cache.contains(record, field))
                            cache.remove(record, field);
                    } else {
                        Object cache_value = field.convert_to_cache(value, record, true);
                        cache.set(record, field, cache_value);
                    }
                }
            }
        }

        check_access_rule(records, "create");

        return records;
    }

    void append_value(StringBuilder columns, StringBuilder formats, List<Object> values, String name, String fmt,
            Object val) {
        if (columns.length() > 0) {
            columns.append(",");
            formats.append(",");
        }
        columns.append(name);
        formats.append(fmt);
        values.add(val);
    }

    @SuppressWarnings("unchecked")
    public Self create(Self self, Object values) {
        List<Map<String, Object>> vals_list = new ArrayList<Map<String, Object>>();
        if (values instanceof Map<?, ?>) {
            vals_list.add((Map<String, Object>) values);
        } else if (values instanceof Collection<?>) {
            vals_list.addAll((Collection<Map<String, Object>>) values);
        }
        if (vals_list.isEmpty())
            return self.browse();
        self = self.browse();
        check_access_rights(self, "create");
        Set<String> bad_names = new HashSet<String>();
        Collections.addAll(bad_names, "id", "parent_path");
        if (self.getMeta().log_access()) {
            bad_names.addAll(LOG_ACCESS_COLUMNS);
        }
        List<Dict> data_list = new ArrayList<Dict>();
        Set<Field> inversed_fields = new HashSet<Field>();
        for (Map<String, Object> vals : vals_list) {
            vals = _add_missing_default_values(self, vals);
            Dict data = new Dict();
            Dict stored = new Dict(), inversed = new Dict();
            Map<String, Map<String, Object>> inherited = new HashMap<String, Map<String, Object>>();
            Set<Object> protected_ = new HashSet<Object>();
            data.put("stored", stored);
            data.put("inversed", inversed);
            data.put("inherited", inherited);
            data.put("protected", protected_);

            for (String key : vals.keySet()) {
                if (bad_names.contains(key))
                    continue;
                Object val = vals.get(key);
                Field field = self.getMeta().findField(key);
                if (field == null) {
                    System.out.printf("%s.create() with unknown fields: %s", self.getName(), key);
                    continue;
                }
                if (field.company_dependent()) {

                }
                if (field.store()) {
                    stored.put(key, val);
                }
                if (field.inherited) {
                    if (inherited.containsKey(field.related_field.model_name())) {
                        inherited.get(field.related_field.model_name()).put(key, val);
                    } else {
                        Map<String, Object> m = new Dict().set(key, val);
                        inherited.put(field.related_field.model_name(), m);
                    }
                } else if (StringUtils.hasText(field.inverse)) {
                    inversed.put(key, val);
                    inversed_fields.add(field);
                }
                if (StringUtils.hasText(field.compute) && !field.readonly()) {
                    protected_.add(_field_computed.getOrDefault(field, Arrays.asList(field)));
                }
            }
            data_list.add(data);
        }

        Self records = _create(self, data_list);

        return records;
    }

    Map<String, Object> _add_missing_default_values(Self self, Map<String, Object> vals) {
        List<String> missing_defaults = new ArrayList<String>();
        for (Field field : self.getFields()) {
            if (!vals.containsKey(field.getName())) {
                missing_defaults.add(field.getName());
            }
        }
        if (missing_defaults.isEmpty())
            return vals;
        Dict defaults = default_get(self, missing_defaults);
        for (String name : defaults.keySet()) {
            Object value = defaults.get(name);
            Field field = self.getField(name);
            if (field instanceof Many2manyField && value instanceof List && !((List<?>) value).isEmpty()
                    && ((List<?>) value).get(0) instanceof String) {
                defaults.put(name, Arrays.asList(new Tuple<>(6, 0, value)));
            } else if (field instanceof One2manyField && value instanceof List && !((List<?>) value).isEmpty()
                    && ((List<?>) value).get(0) instanceof Map) {
                List<Tuple<Object>> values = new ArrayList<>();
                for (Object m : (List<?>) value) {
                    values.add(new Tuple<>(0, 0, m));
                }
                defaults.put(name, values);
            }
        }
        defaults.update(vals);
        return defaults;

    }

    public Dict default_get(Self self, Collection<String> fields_list) {
        Dict defaults = new Dict();
        Map<String, Object> ir_defaults = self.env("ir.default").call(new TypeReference<Map<String, Object>>() {
        }, "get_model_defaults", self.getName(), false);

        Map<String, List<String>> parent_fields = new HashMap<String, List<String>>();

        for (String name : fields_list) {
            String key = "default_" + name;
            if (self.context().containsKey(key)) {
                defaults.put(name, self.context(key));
                continue;
            }
            if (ir_defaults.containsKey(name)) {
                defaults.put(name, ir_defaults.get(name));
                continue;
            }
            Field field = self.getField(name);
            if (field.default_ != null || field.default_value != null) {
                defaults.put(name, field.get_default_value(self));
                continue;
            }
            if (field.inherited) {
                field = field.related_field;
                if (parent_fields.containsKey(field.model_name())) {
                    List<String> names = new ArrayList<String>();
                    names.add(field.getName());
                    parent_fields.put(field.model_name(), names);
                } else {
                    List<String> names = parent_fields.get(field.model_name());
                    names.add(field.getName());
                }
            }
        }

        for (String fname : defaults.keySet()) {
            Field field = self.getMeta().findField(fname);
            if (field != null) {
                Object value = field.convert_to_cache(defaults.get(fname), self, false);
                defaults.put(fname, field.convert_to_write(value, self));
            }
        }

        for (String model : parent_fields.keySet()) {
            defaults.update(self.env(model).call(Dict.class, "default_get", parent_fields.get(model)));
        }

        return defaults;
    }

    public Tuple<String> name_create(Self self, String name) {
        String rec_name = self.getMeta().rec_name();
        if (StringUtils.hasText(rec_name)) {
            Self record = create(self, new Dict().set(rec_name, name));
            return name_get(record).get(0);
        }
        return null;
    }

    public List<Tuple<String>> name_get(Self self) {
        List<Tuple<String>> result = new ArrayList<>();
        String rec_name = self.getMeta().rec_name();
        Field field = self.getMeta().findField(rec_name);
        if (field != null) {
            for (Self record : self) {
                Tuple<String> tuple = new Tuple<>(record.id(),
                        field.convert_to_display_name(record.get(field), record));
                result.add(tuple);
            }
        } else {
            for (Self record : self) {
                Tuple<String> tuple = new Tuple<>(record.id(), String.format("%s.%s", record.getName(), record.id()));
                result.add(tuple);
            }
        }
        return result;
    }

    protected void _read(Self self, Collection<Field> fields) {
        if (fields.isEmpty())
            return;
        check_access_rights(self, "read", true);
        List<String> fnames = new ArrayList<String>();
        List<String> field_names = new ArrayList<String>();
        for (Field field : fields) {
            fnames.add(field.getName());
            if (field.store()) {
                field_names.add(field.getName());
            }
        }
        flush(self, fnames, self);
        Environment env = self.env();
        Cursor cr = env.cr();
        String select_clause = org.apache.tomcat.util.buf.StringUtils.join(field_names, ',');
        String from_clause = self.table();
        String where_clause = "id=any(%s)";
        String query_str = String.format("SELECT %s FROM %s WHERE %s", select_clause, from_clause, where_clause);
        List<Tuple<?>> result = new ArrayList<>();
        for (Tuple<?> sub_ids : cr.split_for_in_conditions(self.ids())) {
            cr.execute(query_str, sub_ids);
            result.addAll(cr.fetchall());
        }

    }

    public boolean write(Self self, Map<String, Object> vals) {
        if (!self.hasId()) {
            return true;
        }
        check_access_rights(self, "write");
        check_field_access_rights(self, "write", vals.keySet());
        check_access_rule(self, "write");

        // for (String fname : vals.keySet()) {
        // Field field = self.getField(fname);
        // }

        if (self.getMeta().log_access()) {
            IdValues towrite = self.env().all().towrite(self.getName());
            for (Self record : self) {
                towrite.set(record.id(), "write_uid", self.env().uid());
                towrite.set(record.id(), "write_date", false);
            }
            self.env().cache().invalidate(Arrays.asList(new Tuple2<>(self.getField("write_date"), self.ids()),
                    new Tuple2<>(self.getField("write_uid"), self.ids())));
        }

        for (String fname : vals.keySet()) {
            Field field = self.getField(fname);
            field.write(self, vals.get(fname));
        }
        // self.modified(vals)

        return true;
    }

    protected boolean _write(Self self, Map<String, Object> vals) {
        if (!self.hasId())
            return true;
        Cursor cr = self.env().cr();
        List<String> columns = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();
        for (Entry<String, Object> e : vals.entrySet()) {
            String name = e.getKey();
            Object val = e.getValue();
            if (self.getMeta().log_access() && LOG_ACCESS_COLUMNS.contains(name) && val == null)
                continue;
            Field field = self.getField(name);
            assert field.store();
            columns.add(String.format("\"%s\"=%s", name, field.column_format));
            params.add(val);
        }

        if (self.getMeta().log_access()) {
            if (vals.get("write_uid") == null) {
                columns.add("\"write_uid\"=%s");
                params.add(self.env().uid());
            }
            if (vals.get("write_uid") == null) {
                columns.add("\"write_uid\"=%s");
                params.add(new AsIs("(now() at time zone 'UTC')"));
            }
        }

        if (!columns.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            org.apache.tomcat.util.buf.StringUtils.join(columns.toArray(new String[0]), ',', sb);
            String query = String.format("UPDATE \"%s\" SET %s WHERE id IN %%s", self.table(), sb.toString());
            for (Tuple<?> sub_ids : cr.split_for_in_conditions(Arrays.asList(self.ids))) {
                List<Object> p = new ArrayList<>();
                p.addAll(params);
                p.addAll(Arrays.asList(sub_ids));
                cr.execute(query, p);
                if (cr.rowcount() != sub_ids.size())
                    throw new MissingErrorException(String.format(
                            "One of the records you are trying to modify has already been deleted (Document type: %s).",
                            self.getMeta().description()));
            }
        }

        return true;
    }

    public void flush(Self self, @Default Collection<String> fnames, @Default Self records) {
        HashMap<String, IdValues> towrite = self.env().all().towrite();
        if (fnames == null) {
            recompute(self, null, null);
            Object[] keys = towrite.keySet().toArray();
            for (Object key : keys) {
                IdValues vals = towrite.remove(key);
                flush_process(self.env((String) key), vals);
            }
        } else {
            recompute(self, fnames, records);

            if (records != null) {
                Object to = towrite.get(self.getName());
                if (to == null) {
                    return;
                }
                Dict dict = (Dict) to;
                boolean hasToWrite = false;
                for (Self record : records) {
                    Dict kv = (Dict) dict.get(record.id());
                    for (String f : fnames) {
                        if (kv.containsKey(f)) {
                            hasToWrite = true;
                            break;
                        }
                    }
                    if (hasToWrite) {
                        break;
                    }
                }
                if (!hasToWrite) {
                    return;
                }
            }

            HashMap<String, List<Field>> model_fields = new HashMap<String, List<Field>>();
            for (String fname : fnames) {
                Field field = self.getField(fname);
                add_model_field(model_fields, field);
                if (field.related_field != null) {
                    add_model_field(model_fields, field.related_field);
                }
            }
            for (String model_name : model_fields.keySet()) {
                List<Field> fields = model_fields.get(model_name);
                flush_model(self, model_name, towrite, fields);
            }
        }
    }

    void flush_model(Self self, String model_name, Map<String, IdValues> towrite, List<Field> fields) {
        Collection<HashMap<String, Object>> vals = towrite.get(model_name).values();
        for (Field field : fields) {
            for (HashMap<String, Object> v : vals) {
                if (v.containsKey(field.getName())) {
                    IdValues id_vals = towrite.remove(model_name);
                    flush_process(self.env(model_name), id_vals);
                    return;
                }
            }
        }
    }

    void add_model_field(Map<String, List<Field>> model_fields, Field field) {
        if (!model_fields.containsKey(field.model_name())) {
            List<Field> list = Arrays.asList(field);
            model_fields.put(field.model_name(), list);
        } else {
            List<Field> list = (List<Field>) model_fields.get(field.model_name());
            list.add(field);
        }
    }

    public void recompute(Self self, @Default Collection<String> fnames, @Default Self records) {
        if (fnames == null) {

        } else {
            List<Field> fields = new ArrayList<Field>();
            boolean any = false;
            for (String fname : fnames) {
                Field field = self.getField(fname);
                if (records != null) {
                    // if (records & self.env.records_to_compute(field)) {
                    // any = true;
                    // break;
                    // }
                }
                fields.add(field);
            }
            if (!any)
                return;

            for (Field field : fields)
                recompute_process(self, field);
        }
    }

    private void recompute_process(Self self, Field field) {

    }

    private void flush_process(Self model, IdValues id_values) {
        for (String id : id_values.ids()) {
            Self recs = model.browse(id);
            try {
                _write(recs, id_values.get(id));
            } catch (Exception exc) {

            }
        }
    }

    public List<Dict> read(Self self, Collection<String> fields) {
        Collection<Field> _fields = check_field_access_rights(self, "read", fields);
        List<Field> stored_fields = new ArrayList<Field>();
        for (Field field : _fields) {
            if (field.store()) {
                stored_fields.add(field);
            } else if (StringUtils.hasText(field.compute)) {
                for (String dotname : field.depends) {
                    Field f = self.getField(dotname.split(".")[0]);
                    if (f.prefetch && (!StringUtils.hasText(f.groups) || user_has_group(self, f.groups))) {
                        stored_fields.add(f);
                    }
                }
            }
        }
        _read(self, _fields);
        List<Dict> result = new ArrayList<Dict>();
        for (Self record : self) {
            Dict dict = new Dict();
            dict.put("id", record.id());
            boolean error = false;
            for (Field field : _fields) {
                try {
                    dict.put(field.getName(), field.convert_to_read(record.get(field), record));
                } catch (Exception e) {
                    error = true;
                    break;
                }
            }
            if (!error)
                result.add(dict);
        }
        return result;
    }

    public Collection<Field> check_field_access_rights(Self self, String operatoin, Collection<String> fields) {
        if (self.env().su()) {
            if (fields != null && fields.size() > 0) {
                ArrayList<Field> result = new ArrayList<Field>();
                for (String fname : fields) {
                    result.add(self.getField(fname));
                }
                return result;
            }
            return self.getFields();
        }
        ArrayList<Field> result = new ArrayList<Field>();
        if (fields == null || fields.size() == 0) {
            for (Field field : self.getFields()) {
                if (valid(self, field)) {
                    result.add(field);
                }
            }
        } else {
            ArrayList<String> invalid_fields = new ArrayList<String>();
            for (String fname : fields) {
                Field field = self.getField(fname);
                if (valid(self, field)) {
                    result.add(self.getField(fname));
                } else {
                    invalid_fields.add(fname);
                }
            }
            if (invalid_fields.size() > 0) {

            }
        }
        return result;
    }

    public void check_access_rule(Self self, String operation) {
        if (self.env().su())
            return;

    }

    public boolean check_access_rights(Self self, String operation) {
        return check_access_rights(self, operation, true);
    }

    public boolean check_access_rights(Self self, String operation, boolean raise_exception) {
        return self.env("ir.model.access").call(boolean.class, "check", self.getName(), operation, raise_exception);
    }

    private boolean valid(Self self, Field field) {
        if (StringUtils.hasText(field.groups)) {
            return user_has_group(self, field.groups);
        }

        return true;
    }

    public boolean user_has_group(Self self, String groups) {
        return true;
    }

    public MetaModel _build_model(Registry registry) {
        if (!StringUtils.hasText(_name)) {
            _name = _inherit;
        }
        Class<?> clazz = getClass();
        if (!StringUtils.hasText(_name))
            throw new ModelException("Model:" + clazz.getName() + " has not set name or inherit");
        MetaModel meta;
        if (registry.contains(_name)) {
            meta = registry.get(_name);
        } else {
            meta = new MetaModel(this);
        }
        ArrayList<String> inherits = new ArrayList<String>();
        if (StringUtils.hasText(_inherit)) {
            inherits.add(_inherit);
        }
        if (_inherits != null) {
            for (String x : _inherits) {
                inherits.add(x);
            }
        }
        if (inherits.isEmpty()) {
            inherits.add("base");
        }
        HashMap<String, Field> fieldMetas = new HashMap<String, Field>();
        ArrayList<MethodInfo> methodMetas = new ArrayList<MethodInfo>();

        for (String base : inherits) {
            if (!registry.contains(base))
                throw new ModelException(
                        "Inherit model:" + base + "defind in class:" + clazz.getName() + " has not be registered");
            MetaModel parent = registry.get(base);
            for (Field f : parent.getFields()) {
                if (!LOG_ACCESS_COLUMNS.contains(f.getName())) {
                    fieldMetas.putIfAbsent(f.getName(), f);
                }
            }
            for (List<MethodInfo> values : parent.getNameMethods().values()) {
                for (MethodInfo v : values) {
                    methodMetas.add(new MethodInfo(meta, v.getMethod()));
                }
            }
        }

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            methodMetas.add(new MethodInfo(meta, method));
        }
        meta.setMethods(methodMetas);

        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers()) || !Field.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Field f = (Field) field.get(null);
                String key = field.getName();
                f.setName(key);
                if (StringUtils.hasText(f.compute)) {
                    Method compute = clazz.getMethod(f.compute, Self.class);
                    api.depends depends = compute.getAnnotation(api.depends.class);
                    if (depends != null) {
                        f.set_depends(depends.value());
                    }
                }
                if (fieldMetas.containsKey(key)) {
                    Field old = fieldMetas.get(key);
                    Field new_ = update(old, f);
                    new_.setMeta(meta);
                    fieldMetas.replace(key, new_);
                } else {
                    f.setMeta(meta);
                    fieldMetas.put(key, f);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new ModelException("Model:" + getClass().getName() + " register field " + field.getName()
                        + " error:" + e.getMessage());
            }
        }
        if (meta._log_access) {
            Field create_uid = jdoo.models.fields.Many2one("res.users").string("Created by").automatic(true)
                    .readonly(true);
            create_uid.setName("create_uid");
            create_uid.setMeta(meta);
            fieldMetas.put("create_uid", create_uid);
            Field create_date = jdoo.models.fields.DateTime().string("Created on").automatic(true).readonly(true);
            create_date.setName("create_date");
            create_date.setMeta(meta);
            fieldMetas.put("create_date", create_date);
            Field write_uid = jdoo.models.fields.Many2one("res.users").string("Last Updated by").automatic(true)
                    .readonly(true);
            write_uid.setName("write_uid");
            write_uid.setMeta(meta);
            fieldMetas.put("write_uid", write_uid);
            Field write_date = jdoo.models.fields.DateTime().string("Last Updated on").automatic(true).readonly(true);
            write_date.setName("write_date");
            write_date.setMeta(meta);
            fieldMetas.put("write_date", write_date);
        }
        if (StringUtils.hasText(meta._rec_name)) {
            assert fieldMetas.containsKey(meta._rec_name)
                    : String.format("Invalid rec_name %s for model %s", meta._rec_name, meta._name);
        } else if (fieldMetas.containsKey("name")) {
            meta._rec_name = "name";
        } else if (fieldMetas.containsKey("x_name")) {
            meta._rec_name = "x_name";
        }
        meta.setFields(fieldMetas.values());
        return meta;
    }

    private Field update(Field oldField, Field newField) {
        return newField;
    }

    public Self exists(Self self) {
        // TODO
        // ids, new_ids = [], []
        // for i in self._ids:
        // (ids if isinstance(i, int) else new_ids).append(i)
        // if not ids:
        // return self
        // query = """SELECT id FROM "%s" WHERE id IN %%s""" % self._table
        // self._cr.execute(query, [tuple(ids)])
        // ids = [r[0] for r in self._cr.fetchall()]
        // return self.browse(ids + new_ids)
        return self.browse();
    }

    public void modified(Self self, Collection<String> fnames, @Default("false") boolean create) {

    }

    public void _fetch_field(Self self, Field field) {
        check_field_access_rights(self, "read", Arrays.asList(field.getName()));
        List<Field> fields = new ArrayList<>();
        if (Utils.Maps.get(self.context(), "prefetch_fields", true) && field.prefetch()) {
            for (Field f : self.getFields()) {
                if (f.prefetch() && !(StringUtils.hasText(f.groups) && !user_has_group(self, f.groups))
                        && !(StringUtils.hasText(f.compute) && self.env().records_to_compute(f).hasId())) {
                    fields.add(f);
                }
            }
            if (!fields.contains(field)) {
                fields.add(field);
                self = self.subtract(self.env().records_to_compute(field));
            }
        } else {
            fields.add(field);
        }
        _read(self, fields);
    }

    public void _prepare_setup(Self self) {

    }

    public void _setup_base(Self self) {
        // _inherits_check(self);
        for (String parent : self.getMeta().inherits()) {
            self.env(parent).call("_setup_base");
        }
        // _add_inherited_fields(self);
    }

    public void _setup_fields(Self self) {

    }

    public void _setup_complete(Self self) {

    }
}
