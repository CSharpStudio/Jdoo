package jdoo.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import jdoo.util.Default;
import jdoo.util.Dict;
import jdoo.util.Linq;
import jdoo.util.Pair;
import jdoo.tools.Collector;
import jdoo.tools.IdValues;
import jdoo.tools.Sql;
import jdoo.util.Tuple;
import jdoo.data.Cursor;
import jdoo.apis.Cache;
import jdoo.apis.Environment;
import jdoo.data.AsIs;
import jdoo.exceptions.MissingErrorException;
import jdoo.models.MetaField.Slots;
import jdoo.models._fields.BooleanField;
import jdoo.models._fields.Many2manyField;
import jdoo.models._fields.One2manyField;
import jdoo.apis.api;

public class BaseModel extends MetaModel {
    private static Logger _logger = LogManager.getLogger(BaseModel.class);
    MetaModel meta;
    static List<String> LOG_ACCESS_COLUMNS = Arrays.asList("create_uid", "create_date", "write_uid", "write_date");
    Map<Field, Object> _field_computed = new HashMap<>();

    protected RecordSet _create(RecordSet self, Collection<Dict> data_list) {
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
        RecordSet records = self.browse(ids);
        Cache cache = self.env().cache();
        for (RecordSet record : records) {
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
    public RecordSet create(RecordSet self, Object values) {
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
        if (self.type().log_access()) {
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
                Field field = self.type().findField(key);
                if (field == null) {
                    System.out.printf("%s.create() with unknown fields: %s", self.name(), key);
                    continue;
                }
                if (field.company_dependent()) {

                }
                if (field.store()) {
                    stored.put(key, val);
                }
                if (field.inherited()) {
                    if (inherited.containsKey(field.related_field().model_name())) {
                        inherited.get(field.related_field().model_name()).put(key, val);
                    } else {
                        Map<String, Object> m = new Dict().set(key, val);
                        inherited.put(field.related_field().model_name(), m);
                    }
                } else if (StringUtils.hasText(field.inverse())) {
                    inversed.put(key, val);
                    inversed_fields.add(field);
                }
                if (StringUtils.hasText(field.compute()) && !field.readonly()) {
                    protected_.add(_field_computed.getOrDefault(field, Arrays.asList(field)));
                }
            }
            data_list.add(data);
        }

        RecordSet records = _create(self, data_list);

        return records;
    }

    Map<String, Object> _add_missing_default_values(RecordSet self, Map<String, Object> vals) {
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
        defaults.putAll(vals);
        return defaults;

    }

    public Dict default_get(RecordSet self, Collection<String> fields_list) {
        Dict defaults = new Dict();
        Map<String, Object> ir_defaults = self.env("ir.default").call(new TypeReference<Map<String, Object>>() {
        }, "get_model_defaults", self.name(), false);

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
            Field field = null;
            try {
                field = self.getField(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (field.$default() != null) {
                defaults.put(name, field.$default().apply(self));
                continue;
            }
            if (field.inherited()) {
                field = field.related_field();
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
            Field field = self.type().findField(fname);
            if (field != null) {
                Object value = field.convert_to_cache(defaults.get(fname), self, false);
                defaults.put(fname, field.convert_to_write(value, self));
            }
        }

        for (String model : parent_fields.keySet()) {
            defaults.putAll(self.env(model).call(Dict.class, "default_get", parent_fields.get(model)));
        }

        return defaults;
    }

    public Tuple<String> name_create(RecordSet self, String name) {
        String rec_name = self.type().rec_name();
        if (StringUtils.hasText(rec_name)) {
            RecordSet record = create(self, new Dict().set(rec_name, name));
            return name_get(record).get(0);
        }
        return null;
    }

    public List<Tuple<String>> name_get(RecordSet self) {
        List<Tuple<String>> result = new ArrayList<>();
        String rec_name = self.type().rec_name();
        Field field = self.type().findField(rec_name);
        if (field != null) {
            for (RecordSet record : self) {
                Tuple<String> tuple = new Tuple<>(record.id(),
                        field.convert_to_display_name(record.get(field), record));
                result.add(tuple);
            }
        } else {
            for (RecordSet record : self) {
                Tuple<String> tuple = new Tuple<>(record.id(), String.format("%s.%s", record.name(), record.id()));
                result.add(tuple);
            }
        }
        return result;
    }

    protected void _read(RecordSet self, Collection<Field> fields) {
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

        List<Field> fields_pre = new ArrayList<>();
        for (String name : field_names) {
            Field field = self.getField(name);
            if (!"id".equals(field.name)) {
                fields_pre.add(field);
            }
        }

        Environment env = self.env();
        Cursor cr = env.cr();
        String select_clause = org.apache.tomcat.util.buf.StringUtils.join(field_names, ',');
        String from_clause = self.table();
        String where_clause = "id in %s";
        String query_str = String.format("SELECT %s FROM %s WHERE %s", select_clause, from_clause, where_clause);
        List<Tuple<?>> result = new ArrayList<>();
        for (Tuple<?> sub_ids : cr.split_for_in_conditions(self.ids())) {
            cr.execute(query_str, Tuple.of(sub_ids));
            result.addAll(cr.fetchall());
        }
        RecordSet fetched;
        if (!result.isEmpty()) {
            // for (type var : iterable) {

            // }
        } else {
            fetched = self.browse();
        }
    }

    public boolean write(RecordSet self, Map<String, Object> vals) {
        if (!self.hasId()) {
            return true;
        }
        check_access_rights(self, "write");
        check_field_access_rights(self, "write", vals.keySet());
        check_access_rule(self, "write");

        // for (String fname : vals.keySet()) {
        // Field field = self.getField(fname);
        // }

        if (self.type().log_access()) {
            IdValues towrite = self.env().all().towrite(self.name());
            for (RecordSet record : self) {
                towrite.set(record.id(), "write_uid", self.env().uid());
                towrite.set(record.id(), "write_date", null);
            }
            self.env().cache().invalidate(Arrays.asList(new Pair<>(self.getField("write_date"), self.ids()),
                    new Pair<>(self.getField("write_uid"), self.ids())));
        }

        for (String fname : vals.keySet()) {
            Field field = self.getField(fname);
            field.write(self, vals.get(fname));
        }
        // self.modified(vals)

        return true;
    }

    protected boolean _write(RecordSet self, Map<String, Object> vals) {
        if (!self.hasId())
            return true;
        Cursor cr = self.env().cr();
        List<String> columns = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();
        for (Entry<String, Object> e : vals.entrySet()) {
            String name = e.getKey();
            Object val = e.getValue();
            if (self.type().log_access() && LOG_ACCESS_COLUMNS.contains(name) && val == null)
                continue;
            Field field = self.getField(name);
            assert field.store();
            columns.add(String.format("\"%s\"=%s", name, field.column_format));
            params.add(val);
        }

        if (self.type().log_access()) {
            if (vals.get("write_uid") == null) {
                columns.add("\"write_uid\"=%s");
                params.add(self.env().uid());
            }
            if (vals.get("write_date") == null) {
                columns.add("\"write_date\"=%s");
                params.add(new AsIs("(now() at time zone 'UTC')"));
            }
        }

        if (!columns.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            org.apache.tomcat.util.buf.StringUtils.join(columns.toArray(new String[0]), ',', sb);
            String query = String.format("UPDATE \"%s\" SET %s WHERE id IN %%s", self.table(), sb.toString());
            for (Tuple<?> sub_ids : cr.split_for_in_conditions(self.ids)) {
                List<Object> p = new ArrayList<>();
                p.addAll(params);
                p.addAll(Arrays.asList(sub_ids));
                cr.execute(query, p);
                if (cr.rowcount() != sub_ids.size())
                    throw new MissingErrorException(String.format(
                            "One of the records you are trying to modify has already been deleted (Document type: %s).",
                            self.type().description()));
            }
        }

        return true;
    }

    public void flush(RecordSet self) {
        flush(self, null, null);
    }

    public void flush(RecordSet self, Collection<String> fnames) {
        flush(self, fnames, null);
    }

    public void flush(RecordSet self, @Default Collection<String> fnames, @Default RecordSet records) {
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
                Object to = towrite.get(self.name());
                if (to == null) {
                    return;
                }
                Dict dict = (Dict) to;
                boolean hasToWrite = false;
                for (RecordSet record : records) {
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
                if (field.related_field() != null) {
                    add_model_field(model_fields, field.related_field());
                }
            }
            for (String model_name : model_fields.keySet()) {
                List<Field> fields = model_fields.get(model_name);
                flush_model(self, model_name, towrite, fields);
            }
        }
    }

    void flush_model(RecordSet self, String model_name, Map<String, IdValues> towrite, List<Field> fields) {
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

    public void recompute(RecordSet self, @Default Collection<String> fnames, @Default RecordSet records) {
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

    private void recompute_process(RecordSet self, Field field) {

    }

    private void flush_process(RecordSet model, IdValues id_values) {
        for (String id : id_values.ids()) {
            RecordSet recs = model.browse(id);
            try {
                _write(recs, id_values.get(id));
            } catch (Exception exc) {

            }
        }
    }

    public List<Dict> read(RecordSet self, Collection<String> fields) {
        Collection<Field> _fields = check_field_access_rights(self, "read", fields);
        List<Field> stored_fields = new ArrayList<Field>();
        for (Field field : _fields) {
            if (field.store()) {
                stored_fields.add(field);
            } else if (StringUtils.hasText(field.compute())) {
                for (String dotname : field.depends()) {
                    Field f = self.getField(dotname.split(".")[0]);
                    if (f.prefetch() && (!StringUtils.hasText(f.groups()) || user_has_group(self, f.groups()))) {
                        stored_fields.add(f);
                    }
                }
            }
        }
        _read(self, _fields);
        List<Dict> result = new ArrayList<Dict>();
        for (RecordSet record : self) {
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

    public Collection<Field> check_field_access_rights(RecordSet self, String operatoin, Collection<String> fields) {
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

    public void check_access_rule(RecordSet self, String operation) {
        if (self.env().su())
            return;

    }

    public boolean check_access_rights(RecordSet self, String operation) {
        return check_access_rights(self, operation, true);
    }

    public boolean check_access_rights(RecordSet self, String operation, boolean raise_exception) {
        return self.env("ir.model.access").call(boolean.class, "check", self.name(), operation, raise_exception);
    }

    private boolean valid(RecordSet self, Field field) {
        if (StringUtils.hasText(field.groups())) {
            return user_has_group(self, field.groups());
        }

        return true;
    }

    public boolean user_has_group(RecordSet self, String groups) {
        return true;
    }

    public RecordSet exists(RecordSet self) {
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

    public void modified(RecordSet self, Collection<String> fnames, @Default("false") boolean create) {

    }

    public void _fetch_field(RecordSet self, Field field) {
        check_field_access_rights(self, "read", Arrays.asList(field.getName()));
        List<Field> fields = new ArrayList<>();
        if (self.context().get("prefetch_fields", true) && field.prefetch()) {
            for (Field f : self.getFields()) {
                if (f.prefetch() && !(StringUtils.hasText(f.groups()) && !user_has_group(self, f.groups()))
                        && !(StringUtils.hasText(f.compute()) && self.env().records_to_compute(f).hasId())) {
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

    public void _prepare_setup(RecordSet self) {
        MetaModel cls = self.type();
        cls._setup_done = false;
    }

    public void _setup_base(RecordSet self) {
        MetaModel cls = self.type();
        if (cls._setup_done) {
            return;
        }
        // 1. determine the proper fields of the model: the fields defined on the
        // class and magic fields, not the inherited or custom ones
        for (Field field : Linq.orderBy(cls.$fields, f -> f._sequence)) {
            if (!field.automatic() && !field.manual() && !field.inherited()) {
                _add_field(self, field.name, field);
            }
        }
        _add_magic_fields(self);

        // # 2. add manual fields
        // if self.pool._init_modules:
        // self.env['ir.model.fields']._add_manual_fields(self)

        // 3. make sure that parent models determine their own fields, then add
        // inherited fields to cls
        _inherits_check(self);
        for (String parent : self.type()._inherits.keySet()) {
            self.env(parent).call("_setup_base");
        }
        _add_inherited_fields(self);

        // 4. initialize more field metadata
        cls._field_computed = new Dict(); // fields computed with the same method
        cls._field_inverses = new Collector(); // inverse fields for related fields

        cls._setup_done = true;

        // 5. determine and validate rec_name
        if (StringUtils.hasText(cls._rec_name)) {
            assert cls._fields.containsKey(cls._rec_name)
                    : String.format("Invalid rec_name %s for model %s", cls._rec_name, cls._name);
        } else if (cls._fields.containsKey("name")) {
            cls._rec_name = "name";
        } else if (cls._fields.containsKey("x_name")) {
            cls._rec_name = "x_name";
        }
    }

    void _add_inherited_fields(RecordSet self) {
        Map<String, Field> fields = new HashMap<>();
        for (Entry<String, String> entry : self.type().inherits().entrySet()) {
            String parent_model = entry.getKey();
            String parent_field = entry.getValue();
            RecordSet parent = self.env(parent_model);
            for (Field field : parent.getFields()) {
                fields.put(field.name, field.$new(f -> {
                    f.setattr(Slots.inherited, true);
                    f.setattr(Slots.inherited_field, field);
                    f.setattr(Slots.related, new Tuple<>(parent_field, field.name));
                    f.setattr(Slots.compute_sudo, false);
                    f.setattr(Slots.copy, field.copy());
                    f.setattr(Slots.readonly, field.readonly());
                }));
            }
        }
        for (Entry<String, Field> entry : fields.entrySet()) {
            String name = entry.getKey();
            Field field = entry.getValue();
            if (!self.hasField(name)) {
                _add_field(self, name, field);
            }
        }
    }

    void _inherits_check(RecordSet self) {

    }

    void _add_field(RecordSet self, String name, Field field) {
        MetaModel cls = self.type();
        field.name = name;
        field.model_name = cls.name();
        cls._fields.put(name, field);
        field.setup_base(self, name);
    }

    void _add_magic_fields(RecordSet self) {
        java.util.function.BiConsumer<String, Field> add = (name, field) -> {
            if (!self.hasField(name)) {
                _add_field(self, name, field);
            }
        };
        _add_field(self, "id", fields.Id().automatic(true));
        add.accept("display_name",
                fields.Char().string("Display Name").automatic(true).compute("_compute_display_name"));

        String last_modified_name;
        if (self.type().log_access()) {
            add.accept("create_uid", fields.Many2one("res.users").string("Created by").automatic(true).readonly(true));
            add.accept("create_date", fields.Datetime().string("Created on").automatic(true).readonly(true));
            add.accept("write_uid",
                    fields.Many2one("res.users").string("Last Updated by").automatic(true).readonly(true));
            add.accept("write_date", fields.Datetime().string("Last Updated on").automatic(true).readonly(true));
            last_modified_name = "compute_concurrency_field_with_access";
        } else {
            last_modified_name = "compute_concurrency_field";
        }
        // this field must override any other column or field
        _add_field(self, CONCURRENCY_CHECK_FIELD, fields.Datetime().string("Last Modified on")
                .compute(last_modified_name).compute_sudo(false).automatic(true));
    }

    public void _setup_fields(RecordSet self) {

    }

    public void _setup_complete(RecordSet self) {

    }

    public void _auto_init(RecordSet self) {
        Cursor cr = self.env().cr();
        boolean must_create_table = !Sql.table_exists(cr, self.table());
        if (self.type()._auto) {
            if (must_create_table) {
                Sql.create_model_table(cr, self.table(), self.type().description());
            }
            // _check_removed_columns(self)
            Map<String, Dict> columns = Sql.table_columns(cr, self.table());
            List<Field> fields_to_compute = new ArrayList<>();
            for (Field field : self.getFields()) {
                if (!field.store()) {
                    continue;
                }
                boolean $new = field.update_db(self, columns);
                if ($new && StringUtils.hasText(field.compute())) {
                    fields_to_compute.add(field);
                }
            }

            // _add_sql_constraints(self);
        }
    }

    public void init(RecordSet self) {

    }

    public void _init_column(RecordSet self, String column_name) {
        Field field = self.getField(column_name);
        Object value = null;
        if (field.$default() != null) {
            value = field.$default().apply(self);
            value = field.convert_to_write(value, self);
            value = field.convert_to_column(value, self, null, true);
        }
        boolean necessary = (field instanceof BooleanField) ? value != null : (Boolean) value;
        if (necessary) {
            _logger.debug("Table '{}': setting default value of new column {} to {}", self.table(), column_name, value);
            String query = String.format("UPDATE \"%s\" SET \"%s\"=%s WHERE \"%s\" IS NULL", self.table(), column_name,
                    field.column_format, column_name);
            self.env().cr().execute(query, new Tuple<>(value));
        }
    }

    public boolean _table_has_rows(RecordSet self) {
        Cursor cr = self.env().cr();
        cr.execute(String.format("SELECT 1 FROM \"%s\" LIMIT 1", self.table()));
        return cr.rowcount() > 0;
    }

    public void compute_concurrency_field(RecordSet self) {
        for (RecordSet record : self) {
            record.set(CONCURRENCY_CHECK_FIELD, new Date());
        }
    }

    @api.depends({ "create_date", "write_date" })
    public void compute_concurrency_field_with_access(RecordSet self) {
        for (RecordSet record : self) {
            Object date = record.get("write_date");
            if (date == null) {
                date = record.get("create_date");
            }
            if (date == null) {
                date = new Date();
            }
            record.set(CONCURRENCY_CHECK_FIELD, date);
        }
    }
}