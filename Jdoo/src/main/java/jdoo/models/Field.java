package jdoo.models;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import jdoo.apis.Cache;
import jdoo.apis.Environment;
import jdoo.apis.Environment.Protecting;
import jdoo.data.Cursor;
import jdoo.exceptions.AccessErrorException;
import jdoo.exceptions.MissingErrorException;
import jdoo.exceptions.ModelException;
import jdoo.util.Default;
import jdoo.util.Dict;
import jdoo.tools.IdValues;
import jdoo.tools.Sql;
import jdoo.util.Tuple;

/**
 * The field descriptor contains the field definition, and manages accesses and
 * assignments of the corresponding field on records.
 */
public class Field extends MetaField {
    public Object get(RecordSet record) {
        if (!record.hasId()) {
            Object value = convert_to_cache(null, record, false);
            return convert_to_record(value, record);
        }
        record.ensure_one();
        Environment env = record.env();
        if (StringUtils.hasText(compute) && env.all().tocompute(this).contains(record.id())
                && !env.is_protected(this, record)) {
            RecordSet recs = this.recursive() ? record : env.records_to_compute(this);
            try {
                compute_value(recs);
            } catch (AccessErrorException e) {
                compute_value(record);
            }
        }
        Object value = null;
        if (env.cache().contains(record, this)) {
            value = env.cache().get(record, this);
        } else {
            if (StringUtils.hasText(record.id()) && store()) {
                RecordSet recs = record._in_cache_without(this);
                try {
                    recs.call("_fetch_field", this);
                } catch (AccessErrorException e) {
                    record.call("_fetch_field", this);
                }
                if (env.cache().contains(record, this) && !record.call(RecordSet.class, "exists").hasId()) {
                    throw new MissingErrorException("Record does not exist or has been deleted.\r\n"
                            + String.format("(Record: %s, User: %s)", record, env.uid()));
                }
                value = env.cache().get(record, this);
            } else if (StringUtils.hasText(compute)) {
                if (env.is_protected(this, record)) {
                    value = convert_to_cache(null, record, false);
                    env.cache().set(record, this, value);
                } else {
                    RecordSet recs = this.recursive() || record.id().isBlank() ? record
                            : record._in_cache_without(this);
                    try {
                        compute_value(recs);
                    } catch (AccessErrorException e) {
                        compute_value(record);
                    }
                    value = env.cache().get(record, this);
                }
                compute_value(record);
                // } else if (record.id().isBlank() && record._origin != null) {
                // value = convert_to_cache(record._origin(getName()), record, true);
                // env.cache().set(record, this, value);
                // } else if (record.id().isBlank() && this instanceof Many2oneField &&
                // delegate()) {
                // Self parent = record.env(comodel_name).call(Self.class, "$new");
                // value = convert_to_cache(parent, record, true);
                // env.cache().set(record, this, value);
            } else {
                value = convert_to_cache(null, record, false);
                env.cache().set(record, this, value);
                Dict defaults = record.call(Dict.class, "default_get", Arrays.asList(getName()));
                if (defaults.containsKey(getName())) {
                    value = convert_to_cache(defaults.get(getName()), record, true);
                }
            }
        }

        return convert_to_record(value, record);
    }

    public void compute_value(RecordSet records) {
        if (compute_sudo()) {
            records = records.sudo();
        }
        try {
            records.call(compute, records);
        } catch (Exception e) {
            throw e;
        }
    }

    public void set(RecordSet records, Object value) {
        List<String> protected_ids = new ArrayList<>();
        List<String> new_ids = new ArrayList<>();
        List<String> other_ids = new ArrayList<>();
        for (String record_id : records.ids()) {
            if (records.env().all().$protected().get(this, Collections.emptyList()).contains(record_id)) {
                protected_ids.add(record_id);
            } else if (record_id == "__new__") {
                new_ids.add(record_id);
            } else {
                other_ids.add(record_id);
            }
        }
        if (!protected_ids.isEmpty()) {
            RecordSet protected_records = records.browse(protected_ids);
            write(protected_records, value);
        }
        if (!new_ids.isEmpty()) {
            RecordSet new_records = records.browse(new_ids);
            Collection<Field> field_computed = records._field_computed().get(this);
            if (field_computed == null) {
                field_computed = Arrays.asList(this);
            }
            try (Protecting a = records.env().protecting(field_computed, records)) {
                new_records.call("modifiel", Arrays.asList(getName()), false);
                write(new_records, value);
                if (relational()) {
                    new_records.call("modifiel", Arrays.asList(getName()), false);
                }
            }
        }
        if (!other_ids.isEmpty()) {
            records = records.browse(other_ids);
            Object write_value = convert_to_write(value, records);
            records.call("write", new Dict().set(getName(), write_value));
        }
    }

    public RecordSet write(RecordSet records, Object value) {
        records.env().remove_to_compute(this, records);
        Cache cache = records.env().cache();
        Object cache_value = convert_to_cache(value, records, true);
        records = cache.get_records_different_from(records, this, cache_value);
        if (!records.hasId())
            return records;
        for (RecordSet rec : records) {
            cache.set(rec, this, cache_value);
        }
        if (store()) {
            IdValues towrite = records.env().all().towrite(records.name());
            RecordSet record = records.browse(records.id());
            Object write_value = convert_to_record(cache_value, record);
            Object column_value = convert_to_column(write_value, record, null, true);
            for (RecordSet rec : records) {
                towrite.set(rec.id(), getName(), column_value);
            }
        }
        return records;
    }

    String convert_to_display_name(Object value, RecordSet record) {
        return value == null ? "" : value.toString();
    }

    Object convert_to_column(Object value, RecordSet record, @Default Object values,
            @Default("true") boolean validate) {
        if (value == null)
            return null;
        return value.toString();
        // return pycompat.to_text(value)
    }

    /**
     * Convert ``value`` to the cache format; ``value`` may come from an assignment,
     * or have the format of methods :meth:`Model.read` or :meth:`Model.write`. If
     * the value represents a recordset, it should be added for prefetching on
     * ``record``.
     * 
     * @param value
     * @param record
     * @param validate
     * @return
     */
    public Object convert_to_cache(Object value, RecordSet record, @Default("true") boolean validate) {
        return value;
    }

    /**
     * Convert ``value`` from the cache format to the record format. If the value
     * represents a recordset, it should share the prefetching of ``record``
     * 
     * @param value
     * @param record
     * @return
     */
    public Object convert_to_record(Object value, RecordSet record) {
        return value;
    }

    /**
     * Convert ``value`` from the record format to the format returned by method
     * :meth:`Model.read`.
     * 
     * @param value
     * @param record
     * @return
     */
    public Object convert_to_read(Object value, RecordSet record) {
        return value;
    }

    /**
     * Convert ``value`` from any format to the format of method
     * :meth:`Model.write`.
     * 
     * @param value
     * @param record
     * @return
     */
    public Object convert_to_write(Object value, RecordSet record) {
        Object cache_value = convert_to_cache(value, record, false);
        Object record_value = convert_to_record(cache_value, record);
        return convert_to_read(record_value, record);
    }

    public boolean update_db(RecordSet model, Map<String, Dict> columns) {
        try {
            Dict column = columns.get(getName());
            update_db_column(model, column);
            update_db_notnull(model, column);
            update_db_index(model, column);
            return false;
        } catch (Exception e) {
            throw new ModelException(String.format("model %s field %s update_db error", model.name(), getName()));
        }
    }

    public void update_db_column(RecordSet model, Dict column) {
        Cursor cr = model.env().cr();
        if (column == null) {
            Sql.create_column(cr, model.table(), getName(), column_type().second().toString(), string);
            return;
        }
        if (column.get("udt_name").equals(column_type().first())) {
            return;
        }
        if (column_cast_from.contains(column.get("udt_name"))) {
            Sql.convert_column(cr, model.table(), getName(), column_type().second().toString());
        } else {
            String newname = getName() + "_moved{0}";
            int i = 0;
            while (Sql.column_exists(cr, model.table(), MessageFormat.format(newname, i))) {
                i += 1;
            }
            if ("NO".equals(column.get("is_nullable"))) {
                Sql.drop_not_null(cr, model.table(), getName());
            }
            Sql.rename_column(cr, model.table(), getName(), MessageFormat.format(newname, i));
            Sql.create_column(cr, model.table(), getName(), column_type().second().toString(), string);
        }
    }

    public void update_db_notnull(RecordSet model, Dict column) {
        boolean has_notnull = column != null && column.get("is_nullable").equals("NO");
        if (column != null || (required() && !has_notnull)) {
            if (model.call(Boolean.class, "_table_has_rows")) {
                model.call("_init_column", getName());
                model.call("flush", Arrays.asList(getName()));
            }
        }
        if (required() && !has_notnull) {
            Sql.set_not_null(model.env().cr(), model.table(), getName());
        } else if (!required() && has_notnull) {
            Sql.drop_not_null(model.env().cr(), model.table(), getName());
        }
    }

    public void update_db_index(RecordSet model, Dict column) {

    }

    public Object cache_key(Environment env) {
        List<Object> objs = new ArrayList<>();
        Dict ctx = env.context();
        for (String key : depends_context()) {
            if ("force_company".equals(key)) {
                if (ctx.containsKey("force_company")) {
                    objs.add(ctx.get("force_company"));
                } else {
                    objs.add(env.company().id());
                }
            } else if ("uid".equals(key)) {
                objs.add(new Tuple<>(env.uid(), env.su()));
            } else if ("active_test".equals(key)) {
                if (ctx.containsKey("active_test")) {
                    objs.add(ctx.get("active_test"));
                } else if (this.context().containsKey("active_test")) {
                    objs.add(context().get("active_test"));
                } else {
                    objs.add(true);
                }
            } else {
                objs.add(ctx.get(key));
            }
        }
        return new Tuple<>(objs);
    }

    public void setup_full() {

    }
}
