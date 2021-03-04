package jdoo.models;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import jdoo.util.Default;
import jdoo.util.DefaultDict;
import jdoo.util.Kvalues;
import jdoo.util.Pair;
import jdoo.tools.Collector;
import jdoo.tools.IdValues;
import jdoo.tools.Sql;
import jdoo.tools.Tools;
import jdoo.util.Tuple;
import jdoo.util.Utils;
import jdoo.data.Cursor;
import jdoo.init;
import jdoo.apis.Cache;
import jdoo.apis.Environment;
import jdoo.data.AsIs;
import jdoo.exceptions.AccessErrorException;
import jdoo.exceptions.MissingErrorException;
import jdoo.exceptions.UserErrorException;
import jdoo.models.MetaField.Slots;
import jdoo.models._fields.BinaryField;
import jdoo.models._fields.BooleanField;
import jdoo.models._fields.Many2manyField;
import jdoo.models._fields.Many2oneField;
import jdoo.models._fields.MonetaryField;
import jdoo.models._fields.One2manyField;
import jdoo.osv.Expression;
import jdoo.osv.Query;
import jdoo.apis.api;
import jdoo.apis.Environment.Protecting;

public class BaseModel extends MetaModel {
    private static Logger _logger = LoggerFactory.getLogger(BaseModel.class);
    MetaModel meta;
    static List<String> LOG_ACCESS_COLUMNS = Arrays.asList("create_uid", "create_date", "write_uid", "write_date");

    // ---------------------------------------------------
    // query methods
    //

    protected void _read(RecordSet self, Collection<Field> fields) {
        if (fields.isEmpty())
            return;
        check_access_rights(self, "read", true);
        List<String> fnames = new ArrayList<>();
        List<Field> field_list = new ArrayList<>();
        List<Field> inherited_field_list = new ArrayList<>();
        for (Field field : fields) {
            fnames.add(field.getName());
            if (field.store()) {
                field_list.add(field);
            } else if (field.base_field().store()) {
                inherited_field_list.add(field);
            }
        }

        flush(self, fnames, self);

        List<Field> fields_pre = new ArrayList<>();
        Stream.of(field_list, inherited_field_list).flatMap(Collection<Field>::stream).forEach(field -> {
            Field base_field = field.base_field();
            if (!"id".equals(field.name) && base_field.store() && base_field.column_type() != null) {
                if (!(field.inherited() && base_field.translate())) {
                    fields_pre.add(field);
                }
            }
        });

        Environment env = self.env();
        Cursor cr = env.cr();
        Kvalues context = env.context();
        Object param_ids = new Object();
        Query query = new Query(Arrays.asList("\"" + self.table() + "\""),
                Arrays.asList("\"" + self.table() + "\".id IN %s"), Arrays.asList(param_ids));
        // todo _apply_ir_rules(self, query, "read");

        List<String> qual_names = new ArrayList<>();
        qual_names.add("id");
        for (Field field : fields_pre) {
            // todo qualify
            if (field instanceof BinaryField
                    && (context.containsKey("bin_size") || context.containsKey("bin_size_" + field.name))) {
                // todo
            } else {
                qual_names.add(field.name);
            }
        }

        String select_clause = org.apache.tomcat.util.buf.StringUtils.join(qual_names, ',');
        Query.Sql sql = query.get_sql();
        String from_clause = sql.from_clause();
        String where_clause = sql.where_clause();
        List<Object> params = sql.params();
        String query_str = String.format("SELECT %s FROM %s WHERE %s", select_clause, from_clause, where_clause);
        int param_pos = params.indexOf(param_ids);

        List<Tuple<?>> result = new ArrayList<>();
        for (Tuple<?> sub_ids : cr.split_for_in_conditions(self.ids())) {
            params.set(param_pos, sub_ids);
            cr.execute(query_str, params);
            result.addAll(cr.fetchall());
        }
        RecordSet fetched;
        if (!result.isEmpty()) {
            List<Tuple<Object>> cols = Utils.zip(result.toArray(new Tuple[0]));
            Tuple<Object> ids = cols.get(0);
            fetched = self.browse(ids);
            int i = 1;
            for (Field field : fields_pre) {
                Tuple<Object> values = cols.get(i++);
                if (context.containsKey("lang") && !field.inherited() && field.translate()) {
                    // todo translate
                }
                env.cache().update(fetched, field, values);
            }
            for (Field field : field_list) {
                if (field.column_type() == null) {
                    field.read(fetched);
                }
                if (field.deprecated()) {
                    _logger.warn("Field {} is deprecated", field);
                }
            }
        } else {
            fetched = self.browse();
        }

        RecordSet missing = self.subtract(fetched);
        if (missing.hasId()) {
            RecordSet extras = fetched.subtract(self);
            if (extras.hasId()) {
                throw new AccessErrorException(MessageFormat.format(
                        "Database fetch misses ids ({0}) and has extra ids ({1}), may be caused by a type incoherence in a previous request",
                        missing.ids, extras.ids));
            }
            RecordSet forbidden = exists(missing);
            if (forbidden.hasId()) {
                // todo raise self.env['ir.rule']._make_access_error('read', forbidden)
            }
        }
    }

    public List<Kvalues> read(RecordSet self, Collection<String> fields) {
        Collection<Field> _fields = check_field_access_rights(self, "read", fields);
        List<Field> stored_fields = new ArrayList<Field>();
        for (Field field : _fields) {
            if (field.store()) {
                stored_fields.add(field);
            } else if (StringUtils.hasText(field.compute())) {
                for (String dotname : field.depends()) {
                    Field f = self.getField(dotname.split("\\.")[0]);
                    if (f.prefetch() && (!StringUtils.hasText(f.groups()) || user_has_group(self, f.groups()))) {
                        stored_fields.add(f);
                    }
                }
            }
        }
        _read(self, _fields);
        List<Kvalues> result = new ArrayList<>();
        for (RecordSet record : self) {
            Kvalues dict = new Kvalues();
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

    // @api.returns("self");
    public Object search(RecordSet self, List<Object> args, @Default("0") int offset, @Default Integer limit,
            @Default String order, @Default("false") boolean count) {
        Object res = _search(self, args, offset, limit, order, count, null);
        return count ? res : self.browse(res);
    }

    protected Object _search(RecordSet self, List<Object> args, @Default("0") int offset, @Default Integer limit,
            @Default String order, @Default("false") boolean count, @Default String access_rights_uid) {
        RecordSet model = StringUtils.hasText(access_rights_uid) ? self.with_user(access_rights_uid) : self;
        check_access_rights(model, "read", true);
        if (Expression.is_false(args)) {
            return count ? 0 : Collections.emptyList();
        }
        // self._flush_search(args, order=order)
        Query query = _where_calc(self, args, true);
        // _apply_ir_rules(self ,query, "read");
        String order_by = _generate_order_by(self, order, query);
        Query.Sql sql = query.get_sql();
        String where_str = sql.where_clause();
        Cursor cr = self.cr();
        if (StringUtils.hasText(where_str)) {
            where_str = " WHERE " + where_str;
        }
        if (count) {
            String query_str = "SELECT count(1) FROM " + sql.from_clause() + where_str;
            cr.execute(query_str, sql.params());
            return cr.fetchone().get(0);
        } else {
            String limit_str = limit != null && limit > 0 ? " limit " + limit : "";
            String offset_str = offset > 0 ? " offset " + offset : "";
            String query_str = "SELECT \"" + self.table() + "\".id FROM " + sql.from_clause() + where_str + order_by
                    + limit_str + offset_str;
            cr.execute(query_str, sql.params());
            List<Tuple<?>> res = cr.fetchall();
            Set<Object> seen = new HashSet<>();
            for (Tuple<?> tuple : res) {
                seen.add(tuple.get(0));
            }
            return new ArrayList<>(seen);
        }
    }

    @api.model
    public List<Pair<Object, Object>> name_search(RecordSet self, @Default("") String name, @Default List<Object> args,
            @Default("ilike") String operator, @Default("100") Integer limit) {
        return _name_search(self, name, args, operator, limit, null);
    }

    @SuppressWarnings("unchecked")
    protected List<Pair<Object, Object>> _name_search(RecordSet self, @Default("") String name,
            @Default List<Object> args, @Default("ilike") String operator, @Default("100") Integer limit,
            @Default String name_get_uid) {
        if (args == null) {
            args = new ArrayList<>();
        } else {
            args = new ArrayList<>(args);
        }
        String _rec_name = self.type().rec_name();
        if (!StringUtils.hasText(_rec_name)) {
            _logger.warn("Cannot execute name_search, no _rec_name defined on {}", self.name());
        } else if (!(name == "" && operator == "ilike")) {
            args.add(new Tuple<>(_rec_name, operator, name));
        }
        String access_rights_uid = StringUtils.hasText(name_get_uid) ? name_get_uid : self.env().uid();
        Collection<Object> ids = (Collection<Object>) _search(self, args, 0, limit, null, false, access_rights_uid);
        RecordSet recs = self.browse(ids);
        return recs.with_user(access_rights_uid).name_get();
    }

    public List<Pair<Object, Object>> name_get(RecordSet self) {
        List<Pair<Object, Object>> result = new ArrayList<>();
        String rec_name = self.type().rec_name();
        Field field = self.type().findField(rec_name);
        if (field != null) {
            for (RecordSet record : self) {
                Pair<Object, Object> pair = new Pair<>(record.id(),
                        field.convert_to_display_name(record.get(field), record));
                result.add(pair);
            }
        } else {
            for (RecordSet record : self) {
                Pair<Object, Object> pair = new Pair<>(record.id(), String.format("%s.%s", record.name(), record.id()));
                result.add(pair);
            }
        }
        return result;
    }

    @api.model
    public Kvalues default_get(RecordSet self, Collection<String> fields_list) {
        // TODO self.view_init(fields_list)
        Kvalues defaults = new Kvalues();
        Map<String, Object> ir_defaults = self.env("ir.default").call(new TypeReference<Map<String, Object>>() {
        }, "get_model_defaults", self.name(), false);

        Map<String, List<String>> parent_fields = new DefaultDict<>(ArrayList::new);

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
                parent_fields.get(field.model_name()).add(field.name);
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
            defaults.putAll(self.env(model).call(Kvalues.class, "default_get", parent_fields.get(model)));
        }

        return defaults;
    }

    public RecordSet exists(RecordSet self) {
        List<Object> ids = new ArrayList<>();
        List<Object> new_ids = new ArrayList<>();
        for (Object i : self.ids) {
            if (Tools.hasId(i)) {
                ids.add(i);
            } else {
                new_ids.add(i);
            }
        }
        if (ids.isEmpty()) {
            return self;
        }
        String query = String.format("SELECT id FROM \"%s\" WHERE id IN %%s", self.table());
        self.cr().execute(query, Arrays.asList(ids));
        ids = Stream.concat(self.cr().fetchall().stream().map(p -> (Object) p.get(0)), new_ids.stream())
                .collect(Collectors.toList());
        return self.browse(ids);
    }

    // ---------------------------------------------------
    // create methods
    //

    protected RecordSet _create(RecordSet self, Collection<Kvalues> data_list) {
        List<String> ids = new ArrayList<>();
        List<Field> other_fields = new ArrayList<>();
        // List<String> translated_fields = new ArrayList<>();
        Cursor cr = self.env().cr();
        for (Kvalues data : data_list) {
            Kvalues stored = (Kvalues) data.get("stored");
            StringBuilder columns = new StringBuilder();
            StringBuilder formats = new StringBuilder();
            List<Object> values = new ArrayList<>();
            if (!stored.containsKey("id")) {
                String id = UUID.randomUUID().toString();
                _append_value(columns, formats, values, "id", "%s", id);
                ids.add(id);
            } else {
                ids.add((String) stored.get("id"));
            }
            if (!stored.containsKey("create_uid")) {
                _append_value(columns, formats, values, "create_uid", "%s", self.env().uid());
            }
            if (!stored.containsKey("create_date")) {
                _append_value(columns, formats, values, "create_date", "%s", new AsIs("(now() at time zone 'UTC')"));
            }
            if (!stored.containsKey("write_uid")) {
                _append_value(columns, formats, values, "write_uid", "%s", self.env().uid());
            }
            if (!stored.containsKey("write_date")) {
                _append_value(columns, formats, values, "write_date", "%s", new AsIs("(now() at time zone 'UTC')"));
            }
            for (String name : stored.keySet()) {
                Field field = self.getField(name);
                if (field.column_type() != null) {
                    Object col_val = field.convert_to_column(stored.get(name), self, stored, true);
                    _append_value(columns, formats, values, name, field.column_format, col_val);
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
            for (Kvalues data : data_list) {
                Kvalues stored = (Kvalues) data.get("stored");
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

    @api.model_create_multi
    @api.returns(value = "self", downgrade = RecordSetId.class)
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
        check_access_rights(self, "create", true);
        Set<String> bad_names = new HashSet<String>();
        Collections.addAll(bad_names, "id", "parent_path");
        if (self.type().log_access()) {
            bad_names.addAll(LOG_ACCESS_COLUMNS);
        }
        List<Kvalues> data_list = new ArrayList<Kvalues>();
        Set<Field> inversed_fields = new HashSet<Field>();
        for (Map<String, Object> vals : vals_list) {
            vals = _add_missing_default_values(self, vals);
            Kvalues data = new Kvalues();
            Kvalues stored = new Kvalues(), inversed = new Kvalues();
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
                        Map<String, Object> m = new Kvalues().set(key, val);
                        inherited.put(field.related_field().model_name(), m);
                    }
                } else if (field.inverse()) {
                    inversed.put(key, val);
                    inversed_fields.add(field);
                }
                if (StringUtils.hasText(field.compute()) && !field.readonly()) {
                    protected_.add(self.type()._field_computed.getOrDefault(field, Arrays.asList(field)));
                }
            }
            data_list.add(data);
        }

        RecordSet records = _create(self, data_list);

        return records;
    }

    /**
     * name_create(name) -> record
     * 
     * Create a new record by calling :meth:`create` with only one value provided:
     * the display name of the new record.
     * 
     * The new record will be initialized with any default values applicable to this
     * model, or provided through the context. The usual behavior of :meth:`create`
     * applies.
     * 
     * @param self
     * @param name display name of the record to create
     * @return
     */
    public Pair<Object, Object> name_create(RecordSet self, String name) {
        String rec_name = self.type().rec_name();
        if (StringUtils.hasText(rec_name)) {
            RecordSet record = create(self, new Kvalues(k -> k.set(rec_name, name)));
            return name_get(record).get(0);
        }
        return null;
    }

    // ---------------------------------------------------
    // write methods
    //

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

    public boolean write(RecordSet self, Map<String, Object> vals) {
        if (!self.hasId()) {
            return true;
        }
        check_access_rights(self, "write", true);
        check_field_access_rights(self, "write", vals.keySet());
        check_access_rule(self, "write");
        Environment env = self.env();

        List<String> bad_names = Utils.asList("id", "parent_path");

        if (self.type().log_access()) {
            // the superuser can set log_access fields while loading registry
            if (!(self.env().uid() == init.SUPERUSER_ID && !self.type().pool().ready())) {
                bad_names.addAll(LOG_ACCESS_COLUMNS);
            }
        }

        DefaultDict<String, List<Field>> determine_inverses = new DefaultDict<>(List.class);
        Map<Field, RecordSet> records_to_inverse = new HashMap<>();
        List<String> relational_names = new ArrayList<>();
        Set<Field> $protected = new HashSet<>();
        boolean check_company = false;
        List<Field> fields = new ArrayList<>();

        for (String fname : vals.keySet()) {
            Field field = self.getField(fname);
            fields.add(field);
            if (field.inverse()) {
                String inverse = field.getattr(MetaField.Slots.inverse).toString();
                determine_inverses.get(inverse).add(field);
            }
            if (field.relational() || self.type().field_inverses().containsKey(field)) {
                relational_names.add(fname);
            }
            if (StringUtils.hasText(field.compute()) && !field.readonly()) {
                $protected.addAll(self.type()._field_computed.getOrDefault(field, Arrays.asList(field)));
            }
            if ("company_id".equals(fname) || (field.relational() && field.check_company())) {
                check_company = true;
            }
        }

        // protect fields being written against recomputation
        try (Protecting protecting = env.protecting($protected, self)) {
            RecordSet real_recs = self.filtered("id");

            // If there are only fields that do not trigger _write (e.g. only
            // determine inverse), the below ensures that `write_date` and
            // `write_uid` are updated (`test_orm.py`, `test_write_date`)
            if (self.type().log_access()) {
                IdValues towrite = self.env().all().towrite(self.name());
                for (RecordSet record : self) {
                    towrite.set(record.id(), "write_uid", self.env().uid());
                    towrite.set(record.id(), "write_date", null);
                }
                self.env().cache().invalidate(Arrays.asList(new Pair<>(self.getField("write_date"), self.ids()),
                        new Pair<>(self.getField("write_uid"), self.ids())));
            }

            // for monetary field, their related currency field must be cached
            // before the amount so it can be rounded correctly
            fields.stream().sorted(Comparator.comparing(f -> f instanceof MonetaryField)).forEach(field -> {
                if (!bad_names.contains(field.name)) {
                    field.write(self, vals.get(field.name));
                }
            });
            modified(self, vals.keySet(), false);

            if (self.type()._parent_store && vals.containsKey(self.type()._parent_name)) {
                flush(self, Arrays.asList(self.type()._parent_name), null);
            }

            // validate non-inversed fields first
            List<String> inverse_fields = determine_inverses.values().stream().flatMap(Collection<Field>::stream)
                    .map(f -> f.name).collect(Collectors.toList());
            List<String> to_validate_fields = new ArrayList<>(vals.keySet());
            for (String f : inverse_fields) {
                to_validate_fields.remove(f);
            }
            _validate_fields(real_recs, to_validate_fields);

            for (List<Field> $fields : determine_inverses.values()) {
                try {
                    $fields.get(0).determine_inverse(real_recs);
                } catch (AccessErrorException e) {
                    if ($fields.get(0).inherited()) {
                        // todo description = self.env['ir.model']._get(self._name).name
                        String description = self.type().description();
                        throw new AccessErrorException(String.format("%s\n\nImplicitly accessed through '%s' (%s).",
                                e.getMessage(), description, self.name()));
                    }
                    throw e;
                }
            }

            // validate inversed fields
            _validate_fields(real_recs, inverse_fields);
        }

        if (check_company && self.type()._check_company_auto) {
            _check_company(self, null);
        }
        return true;
    }

    @api.model
    public void flush(RecordSet self, @Default Collection<String> fnames, @Default RecordSet records) {
        BiConsumer<RecordSet, IdValues> process = (model, id_vals) -> {
            // todo
            for (Object id : id_vals.keySet()) {
                RecordSet recs = model.browse(id);
                Map<String, Object> vals = id_vals.get(id);
                try {
                    _write(recs, vals);
                } catch (MissingErrorException exc) {
                    _write(recs.exists(), vals);
                }
            }
        };

        if (fnames == null) {
            recompute(self, null, null);
            Map<String, IdValues> towrite = self.env().all().towrite();
            Object[] keys = towrite.keySet().toArray();
            for (Object key : keys) {// todo popitem
                IdValues vals = towrite.remove(key);
                process.accept(self.env((String) key), vals);
            }
        } else {
            recompute(self, fnames, records);
            // todo
            if (records != null) {
                IdValues towrite = self.env().all().towrite(self.name());
                if (towrite == null || towrite.isEmpty()) {
                    return;
                }
                boolean hasToWrite = false;
                for (RecordSet record : records) {
                    Map<String, Object> kv = towrite.get(record.id());
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

            Map<String, List<Field>> model_fields = new HashMap<>();
            for (String fname : fnames) {
                Field field = self.getField(fname);
                Utils.setdefault(model_fields, field.model_name(), ArrayList::new).add(field);
                if (field.related_field() != null) {
                    Utils.setdefault(model_fields, field.related_field().model_name(), ArrayList::new).add(field);
                }
            }
            for (String model_name : model_fields.keySet()) {
                List<Field> fields = model_fields.get(model_name);
                Stream<String> stream = fields.stream().map(f -> f.getName());
                Map<String, IdValues> towrite = self.env().all().towrite();
                if (Utils.get(towrite, model_name, IdValues::new).values().stream()
                        .anyMatch(vals -> stream.anyMatch(field -> vals.containsKey(field)))) {
                    IdValues id_vals = towrite.remove(model_name);
                    process.accept(self.env(model_name), id_vals);
                }
            }
        }
    }

    // ---------------------------------------------------
    // methods
    //

    protected void recompute(RecordSet self, @Default Collection<String> fnames, @Default RecordSet records) {
        Consumer<Field> process = field -> {
            // todo
        };

        if (fnames == null) {
            // todo
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
                process.accept(field);
        }
    }

    protected Collection<Field> check_field_access_rights(RecordSet self, String operatoin, Collection<String> fields) {
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
        Function<Field, Boolean> valid = (field) -> {
            if (StringUtils.hasText(field.groups())) {
                return user_has_group(self, field.groups());
            }
            return true;
        };
        ArrayList<Field> result = new ArrayList<Field>();
        if (fields == null || fields.size() == 0) {
            for (Field field : self.getFields()) {
                if (valid.apply(field)) {
                    result.add(field);
                }
            }
        } else {
            ArrayList<String> invalid_fields = new ArrayList<String>();
            for (String fname : fields) {
                Field field = self.getField(fname);
                if (valid.apply(field)) {
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

    protected void check_access_rule(RecordSet self, String operation) {
        if (self.env().su())
            return;

    }

    protected boolean check_access_rights(RecordSet self, String operation, @Default("true") boolean raise_exception) {
        return self.env("ir.model.access").call(boolean.class, "check", self.name(), operation, raise_exception);
    }

    protected boolean user_has_group(RecordSet self, String groups) {
        return true;
    }

    protected void modified(RecordSet self, Collection<String> fnames, @Default("false") boolean create) {
        // todo
    }

    protected void compute_concurrency_field(RecordSet self) {
        for (RecordSet record : self) {
            record.set(CONCURRENCY_CHECK_FIELD, new Date());
        }
    }

    @api.depends({ "create_date", "write_date" })
    protected void compute_concurrency_field_with_access(RecordSet self) {
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

    // ---------------------------------------------------
    // build methods
    //

    protected void init(RecordSet self) {

    }

    protected void _init_column(RecordSet self, String column_name) {
        Field field = self.getField(column_name);
        Object value = null;
        if (field.$default() != null) {
            value = field.$default().apply(self);
            value = field.convert_to_write(value, self);
            value = field.convert_to_column(value, self, null, true);
        }
        boolean necessary = !(field instanceof BooleanField) ? value != null : Boolean.TRUE.equals(value);
        if (necessary) {
            _logger.debug("Table '{}': setting default value of new column {} to {}", self.table(), column_name, value);
            String query = String.format("UPDATE \"%s\" SET \"%s\"=%s WHERE \"%s\" IS NULL", self.table(), column_name,
                    field.column_format, column_name);
            self.env().cr().execute(query, new Tuple<>(value));
        }
    }

    protected void _prepare_setup(RecordSet self) {
        MetaModel cls = self.type();
        cls._setup_done = false;
    }

    protected void _setup_base(RecordSet self) {
        MetaModel cls = self.type();
        if (cls._setup_done) {
            return;
        }
        // 1. determine the proper fields of the model: the fields defined on the
        // class and magic fields, not the inherited or custom ones

        for (int i = cls._bases.size() - 1; i >= 0; i--) {
            MetaModel base = cls._bases.get(i);
            base.$fields.stream().sorted(Comparator.comparing(f -> f._sequence)).forEach(field -> {
                if (!field.automatic() && !field.manual() && !field.inherited()) {
                    Field new_field = self.type().findField(field.name);
                    if (new_field == null) {
                        new_field = field.$new(null);
                    } else {
                        new_field._slots.putAll(field._slots);
                    }
                    _add_field(self, field.name, new_field);
                }
            });
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
        cls._field_computed = new HashMap<>(); // fields computed with the same method
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

    protected void _setup_fields(RecordSet self) {
        MetaModel cls = self.type();
        for (Field field : cls.getFields()) {
            try {
                field.setup_full(self);
            } catch (Exception e) {
                throw e;
            }
        }
        // TODO
        DefaultDict<String, List<Field>> groups = new DefaultDict<>(ArrayList::new);
        for (Field field : cls.getFields()) {
            if (StringUtils.hasText(field.compute())) {
                List<Field> group = groups.get(field.compute());
                cls._field_computed.put(field, group);
                group.add(field);
            }
        }
        // todo check all field's compute_sudo is all the same
    }

    protected void _setup_complete(RecordSet self) {
        // TODO
    }

    protected void _auto_init(RecordSet self) {
        Cursor cr = self.env().cr();
        boolean must_create_table = !Sql.table_exists(cr, self.table());
        if (self.type()._auto) {
            if (must_create_table) {
                Sql.create_model_table(cr, self.table(), self.type().description());
            }
            // _check_removed_columns(self)
            Map<String, Kvalues> columns = Sql.table_columns(cr, self.table());
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

    protected boolean _table_has_rows(RecordSet self) {
        Cursor cr = self.env().cr();
        cr.execute(String.format("SELECT 1 FROM \"%s\" LIMIT 1", self.table()));
        return cr.rowcount() > 0;
    }

    // ---------------------------------------------------
    // help methods
    //

    protected Query _where_calc(RecordSet self, List<Object> domain, @Default("true") boolean active_test) {
        if (!domain.isEmpty()) {
            Expression e = new Expression(domain, self);
            List<String> tables = e.get_tables();
            Pair<String, List<Object>> pair = e.to_sql();
            List<String> where_clause = new ArrayList<>();
            if (StringUtils.hasText(pair.first())) {
                where_clause.add(pair.first());
            }
            List<Object> where_params = pair.second();
            return new Query(tables, where_clause, where_params);
        } else {
            return new Query(Collections.emptyList(), Collections.emptyList(),
                    Arrays.asList("\"" + self.table() + "\""));
        }
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
        Kvalues defaults = default_get(self, missing_defaults);
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

    String _generate_order_by(RecordSet self, String order_spec, Query query) {
        String order_by_clause = "";
        if (!StringUtils.hasText(order_spec)) {
            order_spec = self.type()._order;
        }
        if (StringUtils.hasText(order_spec)) {
            List<String> order_by_elements = _generate_order_by_inner(self, self.table(), order_spec, query, false,
                    null);
            if (!order_by_elements.isEmpty())
                order_by_clause = String.join(",", order_by_elements);
        }
        return StringUtils.hasText(order_by_clause) ? " ORDER BY " + order_by_clause : "";
    }

    List<String> _generate_m2o_order_by(RecordSet self, String alias, String order_field, Query query,
            @Default("false") boolean reverse_direction, @Default Set<Object> seen) {
        Field field = self.getField(order_field);
        if (field.inherited()) {
            String qualified_field = _inherits_join_calc(self, alias, order_field, query, true, false);
            String[] sp = qualified_field.replace("\"", "").split("\\.", 2);
            alias = sp[0];
            order_field = sp[1];
            field = field.base_field();
        }

        assert field instanceof Many2oneField : "Invalid field passed to _generate_m2o_order_by()";

        if (!field.store()) {
            _logger.warn(
                    "Many2one function/related fields must be stored to be used as ordering fields! Ignoring sorting for {}.{}",
                    self.name(), order_field);
            return Collections.emptyList();
        }

        RecordSet dest_model = self.env(field.comodel_name());
        String m2o_order = dest_model.type()._order;
        Tuple<String> join = new Tuple<>(alias, dest_model.table(), order_field, "id", order_field);
        String dest_alias = query.add_join(join, false, true, null, Collections.emptyList()).first();
        return _generate_order_by_inner(dest_model, dest_alias, m2o_order, query, reverse_direction, seen);
    }

    List<String> _generate_order_by_inner(RecordSet self, String alias, String order_spec, Query query,
            @Default("false") boolean reverse_direction, @Default Set<Object> seen) {
        if (seen == null) {
            seen = new HashSet<>();
        }

        _check_qorder(self, order_spec);

        List<String> order_by_elements = new ArrayList<>();
        for (String order_part : order_spec.split(",")) {
            String[] order_split = order_part.trim().split(" ");
            String order_field = order_split[0].trim();
            String order_direction = order_split.length == 2 ? order_split[1].trim().toUpperCase() : "";
            if (reverse_direction) {
                order_direction = "DESC".equals(order_direction) ? "ASC" : "DESC";
            }
            boolean do_reverse = "DESC".equals(order_direction);
            Field field = self.getField(order_field);
            if ("id".equals(order_field)) {
                order_by_elements.add(String.format("\"%s\".\"%s\" %s", alias, order_field, order_direction));
            } else {
                if (field.inherited()) {
                    field = field.base_field();
                }
                if (field.store() && field instanceof Many2oneField) {
                    Tuple<Object> key = new Tuple<>(field.model_name, field.comodel_name(), order_field);
                    if (!seen.contains(key)) {
                        seen.add(key);
                        order_by_elements
                                .addAll(_generate_m2o_order_by(self, alias, order_field, query, do_reverse, seen));
                    }
                } else if (field.store() && field.column_type() != null) {
                    String qualifield_name = _inherits_join_calc(self, alias, order_field, query, false, true);
                    if (field instanceof BooleanField) {
                        qualifield_name = String.format("COALESCE(%s, false)", qualifield_name);
                    }
                    order_by_elements.add(String.format("%s %s", qualifield_name, order_direction));
                } else {
                    _logger.warn("Model {} cannot be sorted on field {} (not a column)", self.name(), order_field);
                    continue;
                }
            }
        }
        return order_by_elements;
    }

    String _inherits_join_calc(RecordSet self, String alias, String fname, Query query,
            @Default("true") boolean implicit, @Default("false") boolean outer) {
        RecordSet model = self;
        Field field = self.getField(fname);
        while (field.inherited()) {
            RecordSet parent_model = self.env(field.related_field().model_name());
            String parent_fname = field.related().iterator().next();

            Pair<String, String> pair = query.add_join(
                    new Tuple<String>(alias, parent_model.table(), parent_fname, "id", parent_fname), implicit, outer,
                    null, Collections.emptyList());
            model = parent_model;
            alias = pair.first();
            field = field.related_field();
        }
        if (field.translate()) {
            return _generate_translated_field(model, alias, fname, query);
        } else {
            return String.format("\"%s\".\"%s\"", alias, fname);
        }
    }

    String _generate_translated_field(RecordSet self, String table_alias, String field, Query query) {
        if (StringUtils.hasText(self.env().lang())) {
            Pair<String, String> join = query.add_join(
                    new Tuple<>(table_alias, "ir_translation", "id", "res_id", field), false, true,
                    "\"{rhs}\".\"type\" = 'model' AND \"{rhs}\".\"name\" = %s AND \"{rhs}\".\"lang\" = %s AND \"{rhs}\".\"value\" != %s",
                    Arrays.asList(String.format("%s,%s", self.name(), field), self.env().lang(), ""));
            return String.format("COALESCE(\"%s\".\"%s\", \"%s\".\"%s\")", join.first(), "value", table_alias, field);
        } else {
            return String.format("\"%s\".\"%s\"", table_alias, field);
        }
    }

    static Pattern regex_order = Pattern.compile(
            "^(\\s*([a-z0-9:_]+|\"[a-z0-9:_]+\")(\\s+(desc|asc))?\\s*(,|$))+(?<!,)$", Pattern.CASE_INSENSITIVE);

    void _check_qorder(RecordSet self, String word) {
        if (!regex_order.matcher(word).matches())
            throw new UserErrorException(
                    "Invalid \"order\" specified. A valid \"order\" specification is a comma-separated list of valid field names (optionally followed by asc/desc for the direction)");
    }

    protected void _check_company(RecordSet self, @Default Collection<String> fnames) {
        // todo
    }

    protected void _validate_fields(RecordSet self, Collection<String> field_names) {
        // todo
    }

    // ---------------------------------------------------
    // static methods
    //

    static void _append_value(StringBuilder columns, StringBuilder formats, List<Object> values, String name,
            String fmt, Object val) {
        if (columns.length() > 0) {
            columns.append(",");
            formats.append(",");
        }
        columns.append(name);
        formats.append(fmt);
        values.add(val);
    }

    static void _fetch_field(RecordSet self, Field field) {
        self.call("check_field_access_rights", "read", Arrays.asList(field.getName()));
        List<Field> fields = new ArrayList<>();
        if (Boolean.TRUE.equals(self.context().getOrDefault("prefetch_fields", true)) && field.prefetch()) {
            for (Field f : self.getFields()) {
                if (f.prefetch()
                        && !(StringUtils.hasText(f.groups()) && !self.call(Boolean.class, "user_has_group", f.groups()))
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
        self.call("_read", fields);
    }

    static void _compute_field_value(RecordSet self, Field field) {
        self.call(field.compute());
        if (field.store() && self.hasId()) {
            List<String> fnames = new ArrayList<>();
            for (Field f : self.type()._field_computed.get(field)) {
                fnames.add(f.name);
            }
            self.call("_validate_fields", fnames);
        }
    }
}

class RecordSetId implements Function<RecordSet, Object> {
    @Override
    public Object apply(RecordSet t) {
        return t.id();
    }
}