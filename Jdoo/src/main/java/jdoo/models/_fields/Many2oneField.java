package jdoo.models._fields;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jdoo.apis.Cache;
import jdoo.exceptions.MissingErrorException;
import jdoo.exceptions.ValueErrorException;
import jdoo.models.BaseModel;
import jdoo.models.Field;
import jdoo.models.NewId;
import jdoo.models.RecordSet;
import jdoo.tools.IdValues;
import jdoo.tools.Slot;
import jdoo.tools.Tools;
import jdoo.util.Kvalues;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.util.Utils;

/**
 * The value of such a field is a recordset of size 0 (no record) or 1 (a single
 * record).
 * 
 * :param comodel_name: name of the target model (string)
 * 
 * :param domain: an optional domain to set on candidate values on the client
 * side (domain or string)
 * 
 * :param context: an optional context to use on the client side when handling
 * that field (dictionary)
 * 
 * :param ondelete: what to do when the referred record is deleted; possible
 * values are: ``'set null'``, ``'restrict'``, ``'cascade'``
 * 
 * :param auto_join: whether JOINs are generated upon search through that field
 * (boolean, by default ``False``)
 * 
 * :param delegate: set it to ``True`` to make fields of the target model
 * accessible from the current model (corresponds to ``_inherits``)
 * 
 * :param check_company: add default domain ``['|', ('company_id', '=', False),
 * ('company_id', '=', company_id)]``. Mark the field to be verified in
 * ``_check_company``.
 * 
 * The attribute ``comodel_name`` is mandatory except in the case of related
 * fields or field extensions.
 */
public class Many2oneField extends _RelationalField<Many2oneField> {
    /** what to do when value is deleted */
    public static final Slot ondelete = new Slot("ondelete", null);
    /** whether joins are generated upon search */
    public static final Slot auto_join = new Slot("auto_join", false);
    /** whether self implements delegation */
    public static final Slot delegate = new Slot("delegate", false);

    public Many2oneField() {
        column_type = new Pair<>("varchar", "varchar");
    }

    public Many2oneField ondelete(String ondelete) {
        setattr(Many2oneField.ondelete, ondelete);
        return this;
    }

    public Many2oneField delegate(boolean delegate) {
        setattr(Many2oneField.delegate, delegate);
        return this;
    }

    public Many2oneField auto_join(boolean auto_join) {
        setattr(Many2oneField.auto_join, auto_join);
        return this;
    }

    boolean _delegate() {
        return getattr(Boolean.class, delegate);
    }

    public boolean _auto_join() {
        return getattr(Boolean.class, Many2oneField.auto_join);
    }

    @Override
    public void _setup_attrs(RecordSet model, String name) {
        super._setup_attrs(model, name);
        if (!getattr(Boolean.class, delegate)) {
            setattr(delegate, model.type().inherits().values().contains(getName()));
        }
    }

    @Override
    public void _setup_regular_base(RecordSet model) {
        super._setup_regular_base(model);
        // 3 cases:
        // 1) The ondelete attribute is not defined, we assign it a sensible default
        // 2) The ondelete attribute is defined and its definition makes sense
        // 3) The ondelete attribute is explicitly defined as 'set null' for a required
        // m2o, this is considered a programming error.
        if (!hasattr(ondelete)) {
            RecordSet comodel = model.env(_comodel_name());
            if (model.type().is_transient() && !comodel.type().is_transient()) {
                setattr(ondelete, _required() ? "cascade" : "set null");
            } else {
                setattr(ondelete, _required() ? "restrict" : "set null");
            }
        }
        if ("set null".equals(getattr(String.class, ondelete)) && _required()) {
            throw new ValueErrorException(String.format(
                    "The m2o field %s of model %s is required but declares its ondelete policy "
                            + "as being 'set null'. Only 'restrict' and 'cascade' make sense.",
                    getName(), model.name()));
        }
    }

    @Override
    public boolean update_db(RecordSet model, Map<String, Kvalues> columns) {
        RecordSet comodel = model.env(_comodel_name());
        if (!model.type().is_transient() && comodel.type().is_transient()) {
            throw new ValueErrorException(String.format("Many2one %s from Model to TransientModel is forbidden", this));
        }
        return super.update_db(model, columns);
    }

    @Override
    public void update_db_column(RecordSet model, Kvalues column) {
        super.update_db_column(model, column);
        // todo model.pool.post_init(self.update_db_foreign_key, model, column)
    }

    protected void update_db_foreign_key(RecordSet model, Kvalues column) {
        // todo
    }

    /** Update the cached value of ``self`` for ``records`` with ``value``. */
    public void _update(RecordSet records, Object value) {
        Cache cache = records.env().cache();
        for (RecordSet record : records)
            cache.set(record, this, convert_to_cache(value, record, false));
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Kvalues values, boolean validate) {
        return Boolean.FALSE.equals(value) ? null : value;
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        // cache format: id or None
        Object id_ = null;
        if (value instanceof String || value instanceof NewId) {
            id_ = value;
        } else if (value instanceof RecordSet) {
            RecordSet rec = (RecordSet) value;
            if (validate && (rec.name() != _comodel_name() || rec.size() > 1)) {
                throw new ValueErrorException("Wrong value for %s: %s".formatted(this, value));
            }
            id_ = rec.hasId() ? rec.get(0) : null;
        } else if (value instanceof Tuple) {
            // value is either a pair (id, name), or a tuple of ids
            id_ = Utils.bool(value) ? ((Tuple<?>) value).get(0) : null;
        } else if (value instanceof Map) {
            id_ = record.env(_comodel_name()).$new((Map<String, Object>) value).id();
        }
        if (_delegate() && record.hasId() && Boolean.FALSE.equals(record.id())) {
            id_ = Utils.bool(id_) ? new NewId(id_, null) : null;
        }
        return id_;
    }

    @Override
    public Object convert_to_record(Object value, RecordSet record) {
        // use registry to avoid creating a recordset for the model
        Collection<Object> ids = value == null ? Collections.emptyList() : new Tuple<>(value);
        Collection<Object> prefetch_ids = prefetch_many2one_ids(record, this);
        return record.pool(_comodel_name()).browse(record.env(), ids, prefetch_ids);
    }

    Collection<Object> prefetch_many2one_ids(RecordSet record, Field field) {
        RecordSet records = record.browse(record.prefetch_ids());
        Collection<Object> ids = record.env().cache().get_values(records, field);
        Set<Object> unique = new HashSet<>();
        for (Object id : ids) {
            if (id != null) {
                unique.add(id);
            }
        }
        return unique;
    }

    @Override
    public Object convert_to_read(Object value, RecordSet record) {
        RecordSet rec = (RecordSet) value;
        // evaluate name_get() as superuser, because the visibility of a
        // many2one field value (id and name) depends on the current record's
        // access rights, and not the value's access rights.
        if (rec.hasId()) {
            try {
                // performance: value.sudo() prefetches the same records as value
                return new Tuple<>(rec.id(), rec.sudo().get("display_name"));
            } catch (MissingErrorException e) {
                // Should not happen, unless the foreign key is missing.
                return false;
            }
        } else {
            return rec.id();
        }
    }

    @Override
    public Object convert_to_write(Object value, RecordSet record) {
        if (value instanceof String || value instanceof NewId) {
            return value;
        }
        if (!Utils.bool(value)) {
            return false;
        }
        if (value instanceof RecordSet && ((RecordSet) value).name() == _comodel_name()) {
            return ((RecordSet) value).id();
        }
        if (value instanceof Tuple) {
            // value is either a pair (id, name), or a tuple of ids
            return Utils.bool(value) ? ((Tuple<?>) value).get(0) : false;
        }
        if (value instanceof Map) {
            return record.env(_comodel_name()).$new((Map<String, Object>) value).id();
        }
        throw new ValueErrorException("Wrong value for %s: %s".formatted(this, value));
    }

    @Override
    public Object convert_to_export(Object value, RecordSet record) {
        if (value instanceof RecordSet) {
            return ((RecordSet) value).get("display_name");
        }
        return "";
    }

    @Override
    public String convert_to_display_name(Object value, RecordSet record) {
        return (String) ((RecordSet) value).get("display_name");
    }

    @Override
    public Object convert_to_onchange(Object value, RecordSet record, Collection<String> names) {
        if (value == null || !Tools.hasId(((RecordSet) value).id())) {
            return false;
        }
        return super.convert_to_onchange(value, record, names);
    }

    @Override
    public RecordSet write(RecordSet records, Object value) {
        // discard recomputation of self on records
        Cache cache = records.env().cache();
        Object cache_value = convert_to_cache(value, records, true);
        records = cache.get_records_different_from(records, this, cache_value);
        if (!records.hasId()) {
            return records;
        }
        // remove records from the cache of one2many fields of old corecords
        _remove_inverses(records, cache_value);
        // update the cache of self
        cache.update(records, this, Utils.mutli(Arrays.asList(cache_value), records.size()));

        // update the cache of one2many fields of new corecord
        _update_inverses(records, cache_value);

        if (_store()) {
            IdValues towrite = records.env().all().towrite(model_name());
            for (RecordSet record : records.filtered("id")) {
                // cache_value is already in database format
                towrite.set(record.id(), getName(), cache_value);
            }
        }
        return records;
    }

    /** Remove `records` from the cached values of the inverse fields of `self`. */
    void _remove_inverses(RecordSet records, Object value) {
        Cache cache = records.env().cache();
        Set<Object> record_ids = new HashSet<>(records.ids());
        for (Field invf : records.type().field_inverses().get(this)) {
            RecordSet corecords = records.env(_comodel_name()).browse(cache.get_values(records, this));
            for (RecordSet corecord : corecords) {
                Collection<Object> ids0 = (Collection<Object>) cache.get(corecord, invf, null);
                if (ids0 != null) {
                    Object ids1 = ids0.stream().filter(id_ -> !record_ids.contains(id_)).collect(Collectors.toList());
                    cache.set(corecord, invf, ids1);
                }
            }
        }
    }

    /** Add `records` to the cached values of the inverse fields of `self`. */
    void _update_inverses(RecordSet records, Object value) {
        if (value == null) {
            return;
        }
        Cache cache = records.env().cache();
        RecordSet corecord = (RecordSet) convert_to_record(value, records);
        for (Field invf : records.type().field_inverses().get(this)) {
            RecordSet valid_records = records.filtered_domain(((_RelationalField<?>) invf).get_domain_list(corecord));
            if (!valid_records.hasId()) {
                continue;
            }
            Collection<Object> ids0 = (Collection<Object>) cache.get(corecord, invf, null);
            // if the value for the corecord is not in cache, but this is a new
            // record, assign it anyway, as you won't be able to fetch it from
            // database (see `test_sale_order`)
            if (ids0 != null || !Utils.bool(corecord.id())) {
                ids0.addAll(valid_records.ids());
                Object ids1 = ids0.stream().distinct().collect(Collectors.toList());
                cache.set(corecord, invf, ids1);
            }
        }
    }
}
