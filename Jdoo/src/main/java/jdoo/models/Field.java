package jdoo.models;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import jdoo.apis.Cache;
import jdoo.apis.Environment;
import jdoo.apis.Environment.Protecting;
import jdoo.data.Cursor;
import jdoo.data.Cursor.SavePoint;
import jdoo.exceptions.AccessErrorException;
import jdoo.exceptions.MissingErrorException;
import jdoo.exceptions.ModelException;
import jdoo.exceptions.TypeErrorException;
import jdoo.util.Default;
import jdoo.util.Kvalues;
import jdoo.util.Pair;
import jdoo.tools.IdValues;
import jdoo.tools.Sql;
import jdoo.tools.Tools;
import jdoo.util.Tuple;

/**
 * The field descriptor contains the field definition, and manages accesses and
 * assignments of the corresponding field on records. The following attributes
 * may be provided when instanciating a field:
 * 
 * <p>
 * :param string: the label of the field seen by users (string); if not set, the
 * ORM takes the field name in the class (capitalized).
 * </p>
 * 
 * <p>
 * :param help: the tooltip of the field seen by users (string)
 * </p>
 * 
 * <p>
 * :param readonly: whether the field is readonly (boolean, by default
 * ``False``)
 * </p>
 * 
 * <p>
 * :param required: whether the value of the field is required (boolean, by
 * default ``False``)
 * </p>
 * 
 * <p>
 * :param index: whether the field is indexed in database. Note: no effect on
 * non-stored and virtual fields. (boolean, by default ``False``)
 * </p>
 * 
 * <p>
 * :param default: the default value for the field; this is either a static
 * value, or a function taking a recordset and returning a value; use
 * ``default=None`` to discard default values for the field
 * </p>
 * 
 * <p>
 * :param states: a dictionary mapping state values to lists of UI
 * attribute-value pairs; possible attributes are: 'readonly', 'required',
 * 'invisible'. Note: Any state-based condition requires the ``state`` field
 * value to be available on the client-side UI. This is typically done by
 * including it in the relevant views, possibly made invisible if not relevant
 * for the end-user.
 * </p>
 * 
 * <p>
 * :param groups: comma-separated list of group xml ids (string); this restricts
 * the field access to the users of the given groups only
 * </p>
 * 
 * <p>
 * :param bool copy: whether the field value should be copied when the record is
 * duplicated (default: ``True`` for normal fields, ``False`` for ``one2many``
 * and computed fields, including property fields and related fields)
 * </p>
 * 
 * .. _field-computed:
 * 
 * .. rubric:: Computed fields
 * 
 * <p>
 * One can define a field whose value is computed instead of simply being read
 * from the database. The attributes that are specific to computed fields are
 * given below. To define such a field, simply provide a value for the attribute
 * ``compute``.
 * </p>
 * 
 * <p>
 * :param compute: name of a method that computes the field
 * </p>
 * 
 * <p>
 * :param inverse: name of a method that inverses the field (optional)
 * </p>
 * 
 * <p>
 * :param search: name of a method that implement search on the field (optional)
 * </p>
 * 
 * <p>
 * :param store: whether the field is stored in database (boolean, by default
 * ``False`` on computed fields)
 * </p>
 * 
 * <p>
 * :param compute_sudo: whether the field should be recomputed as superuser to
 * bypass access rights (boolean, by default ``True``)
 * </p>
 * 
 * <p>
 * The methods given for ``compute``, ``inverse`` and ``search`` are model
 * methods. Their signature is shown in the following example::
 * </p>
 * <blockquote>
 * 
 * <pre>
 * Field upper = fields.Char()compute("_compute_upper").inverse("_inverse_upper").search("_search_upper");
 * 
 * &#64;api.depends('name')
 * void _compute_upper(RecordSet self){
 *      for(RecordSet rec : self){
 *          rec.upper = rec.name != null ? rec.name.upper() : null;
 *      }
 * } 
 * 
 * void _inverse_upper(RecordSet self){
 *      for(RecordSet rec : self){
 *          rec.name = rec.upper != null ? rec.upper.lower() : null;
 *      }
 * } 
 * 
 * List<Tuple<Object>> _search_upper(RecordSet self, String operator, Object value){
 *      if(operator == "like")
 *          operator = "ilike";
 *      return Arrays.asList(new Tuple<>("name", operator, value));
 * }
 * 
 * </pre>
 * 
 * </blockquote>
 * <p>
 * The compute method has to assign the field on all records of the invoked
 * recordset. The decorator :meth:`odoo.api.depends` must be applied on the
 * compute method to specify the field dependencies; those dependencies are used
 * to determine when to recompute the field; recomputation is automatic and
 * guarantees cache/database consistency. Note that the same method can be used
 * for several fields, you simply have to assign all the given fields in the
 * method; the method will be invoked once for all those fields.
 * </p>
 * 
 * <p>
 * By default, a computed field is not stored to the database, and is computed
 * on-the-fly. Adding the attribute ``store=True`` will store the field's values
 * in the database. The advantage of a stored field is that searching on that
 * field is done by the database itself. The disadvantage is that it requires
 * database updates when the field must be recomputed.
 * </p>
 * 
 * <p>
 * The inverse method, as its name says, does the inverse of the compute method:
 * the invoked records have a value for the field, and you must apply the
 * necessary changes on the field dependencies such that the computation gives
 * the expected value. Note that a computed field without an inverse method is
 * readonly by default.
 * </p>
 * 
 * <p>
 * The search method is invoked when processing domains before doing an actual
 * search on the model. It must return a domain equivalent to the condition:
 * ``field operator value``.
 * </p>
 * 
 * <p>
 * .. _field-related:
 * </p>
 * 
 * <p>
 * .. rubric:: Related fields
 * </p>
 * 
 * <p>
 * The value of a related field is given by following a sequence of relational
 * fields and reading a field on the reached model. The complete sequence of
 * fields to traverse is specified by the attribute
 * </p>
 * 
 * <p>
 * :param related: sequence of field names
 * </p>
 * 
 * <p>
 * Some field attributes are automatically copied from the source field if they
 * are not redefined: ``string``, ``help``, ``readonly``, ``required`` (only if
 * all fields in the sequence are required), ``groups``, ``digits``, ``size``,
 * ``translate``, ``sanitize``, ``selection``, ``comodel_name``, ``domain``,
 * ``context``. All semantic-free attributes are copied from the source field.
 * </p>
 * 
 * <p>
 * By default, the values of related fields are not stored to the database. Add
 * the attribute ``store=True`` to make it stored, just like computed fields.
 * Related fields are automatically recomputed when their dependencies are
 * modified.
 * </p>
 * 
 * </p>
 * .. _field-company-dependent:
 * </p>
 * 
 * </p>
 * .. rubric:: Company-dependent fields
 * </p>
 * 
 * <p>
 * Formerly known as 'property' fields, the value of those fields depends on the
 * company. In other words, users that belong to different companies may see
 * different values for the field on a given record.
 * </p>
 * 
 * .. warning::
 * 
 * <p>
 * Company-dependent fields aren't stored in the table of the model they're
 * defined on, instead, they are stored in the ``ir.property`` model's table.
 * </p>
 * 
 * <p>
 * :param company_dependent: whether the field is company-dependent (boolean)
 * </p>
 * 
 * <p>
 * .. _field-incremental-definition:
 * </p>
 * 
 * <p>
 * .. rubric:: Incremental definition
 * </p>
 * 
 * <p>
 * A field is defined as class attribute on a model class. If the model is
 * extended (see :class:`~odoo.models.Model`), one can also extend the field
 * definition by redefining a field with the same name and same type on the
 * subclass. In that case, the attributes of the field are taken from the parent
 * class and overridden by the ones given in subclasses.
 * </p>
 * 
 * <p>
 * For instance, the second class below only adds a tooltip on the field
 * ``state``::
 * </p>
 * <blockquote>
 * 
 * <pre>
 * class First extends Model{
 *      public First(){
 *          _name = 'foo'
 *      }
 *      static Field state = fields.Selection([...]).required(true);
 * } 
 * 
 * class Second extends Model{
 *      public First(){
 *          _inherit = 'foo'
 *      }
 *      static Field state = fields.Selection().help("Blah blah blah");
 * }
 * </pre>
 * 
 * </blockquote>
 */
public class Field extends MetaField {
    protected static Logger _schema = LoggerFactory.getLogger("jdoo.schema");
    protected static Logger _logger = LoggerFactory.getLogger(Field.class);

    public Field $new(@Nullable Consumer<Field> consumer) {
        try {
            Field field = (Field) getClass().getConstructor().newInstance();
            field._slots.putAll(this._slots);
            if (consumer != null) {
                consumer.accept(field);
            }
            return field;
        } catch (Exception e) {
            throw new TypeErrorException(String.format("new field %s error", getClass().getName()), e);
        }
    }

    // =================================================================================
    // Base field setup: things that do not depend on other models/fields
    //

    public void setup_base(RecordSet model, String name) {
        if (_setup_done != SetupState.None && !hasattr(Slots.related)) {
            _setup_done = SetupState.Base;
        } else {
            _setup_attrs(model, name);
            if (!hasattr(Slots.related)) {
                _setup_regular_base(model);
            }
            _setup_done = SetupState.Base;
        }
    }

    public void _setup_attrs(RecordSet model, String name) {
        if (hasattr(Slots.compute)) {
            if (!hasattr(Slots.store)) {
                setattr(Slots.store, false);
            }
            if (!hasattr(Slots.copy)) {
                setattr(Slots.copy, false);
            }
            if (!hasattr(Slots.readonly)) {
                setattr(Slots.readonly, _inverse());
            }
        }
        if (hasattr(Slots.related)) {
            if (!hasattr(Slots.store)) {
                setattr(Slots.store, false);
            }
            if (!hasattr(Slots.copy)) {
                setattr(Slots.copy, false);
            }
            if (!hasattr(Slots.readonly)) {
                setattr(Slots.readonly, true);
            }
        }
        if (hasattr(Slots.company_dependent)) {
            setattr(Slots.store, false);
            if (!hasattr(Slots.copy)) {
                setattr(Slots.copy, false);
            }
            // todo
        }

        if (!(_store() && column_type() != null) || _manual() || _deprecated()) {
            setattr(Slots.prefetch, false);
        }

        if (!hasattr(Slots.string) && !hasattr(Slots.related)) {
            String n = name;
            if (name.endsWith("_ids")) {
                n = name.substring(0, name.length() - 4);
            } else if (name.endsWith("_id")) {
                n = name.substring(0, name.length() - 3);
            }
            n = n.replace('_', ' ');
            setattr(Slots.string, n);
        }
    }

    // =================================================================================
    // Full field setup: everything else, except recomputation triggers
    //

    public void setup_full(RecordSet model) {
        if (_setup_done != SetupState.Full) {
            if (!hasattr(Slots.related)) {
                _setup_regular_full(model);
            } else {
                _setup_related_full(model);
            }
            _setup_done = SetupState.Full;
        }
    }

    // Setup of non-related fields

    /** Setup the attributes of a non-related field. */
    public void _setup_regular_base(RecordSet model) {

    }

    /** Determine the dependencies and inverse field(s) of ``self``. */
    public void _setup_regular_full(RecordSet model) {
        if (hasattr(Slots.depends)) {
            return;
        }
        // todo
    }

    // Setup of related fields

    public void _setup_related_full(RecordSet model) {
        RecordSet target = model;
        Field field = null;
        for (String name : _related()) {
            field = target.getField(name);
            field.setup_full(target);
            Object v = target.get(name);
            target = v instanceof RecordSet ? (RecordSet) v : null;
        }
        setattr(Slots.related_field, field);
        // TODO
    }

    /** Compute the related field ``self`` on ``records``. */
    public void _compute_related(RecordSet records) {
        // todo
    }

    /** No transformation by default, but allows override. */
    protected Object _process_related(Object value) {
        return value;
    }

    /** Inverse the related field ``self`` on ``records``. */
    public void _inverse_related(RecordSet records) {
        // TODO
    }

    /** Determine the domain to search on field ``self``. */
    public Domain _search_related(RecordSet records, String operator, Object value) {
        return d.on(String.join(".", _related()), operator, value);
    }

    public Field base_field() {
        if (hasattr(Slots.inherited_field)) {
            return getattr(Field.class, Slots.inherited_field).base_field();
        }
        return this;
    }

    public Object cache_key(Environment env) {
        List<Object> objs = new ArrayList<>();
        Kvalues ctx = env.context();
        for (String key : _depends_context()) {
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
                } else if (this._context().containsKey("active_test")) {
                    objs.add(_context().get("active_test"));
                } else {
                    objs.add(true);
                }
            } else {
                objs.add(ctx.get(key));
            }
        }
        return new Tuple<>(objs);
    }

    // =================================================================================
    // Conversion of values
    //

    /**
     * Convert ``value`` from the ``write`` format to the SQL format.
     * 
     * @param value
     * @param record
     * @param values   default null
     * @param validate default true
     * @return
     */
    public Object convert_to_column(Object value, RecordSet record, @Default Kvalues values,
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
     * @param validate default true
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

    /**
     * Convert ``value`` from the record format to the format returned by method
     * :meth:`BaseModel.onchange`.
     * 
     * @param value
     * @param record
     * @param names  a tree of field names (for relational fields only)
     * @return
     */
    public Object convert_to_onchange(Object value, RecordSet record, Collection<String> names) {
        return convert_to_read(value, record);
    }

    /**
     * Convert ``value`` from the record format to the export format.
     * 
     * @param value
     * @param record
     * @return
     */
    public Object convert_to_export(Object value, RecordSet record) {
        return Optional.of(value).orElse("");
    }

    /**
     * Convert ``value`` from the record format to a suitable display name.
     * 
     * @param value
     * @param record
     * @return
     */
    public String convert_to_display_name(Object value, RecordSet record) {
        return value == null ? "" : value.toString();
    }

    // =================================================================================
    // Update database schema
    //

    /**
     * Update the database schema to implement this field.
     * 
     * @param model   an instance of the field's model
     * @param columns a dict mapping column names to their configuration in database
     * @return true if the field must be recomputed on existing rows
     */
    public boolean update_db(RecordSet model, Map<String, Kvalues> columns) {
        if (column_type() == null) {
            return false;
        }
        try {
            Kvalues column = columns.get(getName());
            update_db_column(model, column);
            update_db_notnull(model, column);
            update_db_index(model, column);
            return column == null;
        } catch (Exception e) {
            throw new ModelException(String.format("model %s field %s update_db error", model.name(), getName()), e);
        }
    }

    /**
     * Create/update the column corresponding to this field
     * 
     * @param model  an instance of the field's model
     * @param column the column's configuration (dict) if it exists, or null
     */
    public void update_db_column(RecordSet model, @Nullable Kvalues column) {
        Cursor cr = model.env().cr();
        if (column == null) {
            Sql.create_column(cr, model.table(), getName(), column_type().second().toString(), _string());
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
            Sql.create_column(cr, model.table(), getName(), column_type().second().toString(), _string());
        }
    }

    /**
     * Add or remove the NOT NULL constraint on this field
     * 
     * @param model  an instance of the field's model
     * @param column the column's configuration (dict) if it exists, or null
     */
    public void update_db_notnull(RecordSet model, @Nullable Kvalues column) {
        boolean has_notnull = column != null && column.get("is_nullable").equals("NO");
        if (column != null || (_required() && !has_notnull)) {
            if (model.call(Boolean.class, "_table_has_rows")) {
                model.call("_init_column", getName());
                model.call("flush", Arrays.asList(getName()));
            }
        }
        if (_required() && !has_notnull) {
            Sql.set_not_null(model.env().cr(), model.table(), getName());
        } else if (!_required() && has_notnull) {
            Sql.drop_not_null(model.env().cr(), model.table(), getName());
        }
    }

    /**
     * Add or remove the index corresponding to this field
     * 
     * @param model  an instance of the field's model
     * @param column the column's configuration (dict) if it exists, or null
     */
    public void update_db_index(RecordSet model, @Nullable Kvalues column) {
        String indexname = String.format("%s_%s_index", model.table(), getName());
        if (_index()) {
            try (SavePoint p = model.cr().savepoint()) {
                Sql.create_index(model.cr(), indexname, model.table(),
                        Arrays.asList(String.format("\"%s\"", getName())));
            } catch (Exception e) {
                _schema.error("Unable to add index for {}", this);
            }
        } else {
            Sql.drop_index(model.cr(), indexname, model.table());
        }
    }

    // =================================================================================
    // Read from/write to database
    //

    /**
     * Read the value of this on records, and store it in cache.
     * 
     * @param records
     */
    public void read(RecordSet records) {
        throw new UnsupportedOperationException(String.format("Method read() undefined on %s", this));
    }

    /**
     * Write the value of this on the given records, which have just been created.
     * 
     * @param record_values a list of pairs (record, value), where value is in the
     *                      format of method :meth:`BaseModel.write`
     */
    public void create(List<Pair<RecordSet, Object>> record_values) {
        for (Pair<RecordSet, Object> p : record_values) {
            write(p.first(), p.second());
        }
    }

    /**
     * Write the value of this on ``records``. This method must update the cache and
     * prepare database updates.
     * 
     * @param records
     * @param value   a value in any format
     * @return the subset of `records` that have been modified
     */
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
        if (_store()) {
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

    // =================================================================================

    /**
     * return the value of field on ``record``
     * 
     * @param record
     * @return
     */
    public Object get(RecordSet record) {
        if (!record.hasId()) {
            Object value = convert_to_cache(null, record, false);
            return convert_to_record(value, record);
        }
        record.ensure_one();
        Environment env = record.env();
        if (StringUtils.hasText(_compute()) && env.all().tocompute(this).contains(record.id())
                && !env.is_protected(this, record)) {
            RecordSet recs = this._recursive() ? record : env.records_to_compute(this);
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
            if (Tools.hasId(record.id()) && _store()) {
                RecordSet recs = _in_cache_without(record, this);
                try {
                    Model._fetch_field(recs, this);
                } catch (AccessErrorException e) {
                    Model._fetch_field(record, this);
                }
                if (!env.cache().contains(record, this) && !record.call(RecordSet.class, "exists").hasId()) {
                    throw new MissingErrorException("Record does not exist or has been deleted.\r\n"
                            + String.format("(Record: %s, User: %s)", record, env.uid()));
                }
                value = env.cache().get(record, this);
            } else if (StringUtils.hasText(_compute())) {
                if (env.is_protected(this, record)) {
                    value = convert_to_cache(null, record, false);
                    env.cache().set(record, this, value);
                } else {
                    RecordSet recs = this._recursive() || !Tools.hasId(record.id()) ? record
                            : _in_cache_without(record, this);
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
                Kvalues defaults = record.call(Kvalues.class, "default_get", Arrays.asList(getName()));
                if (defaults.containsKey(getName())) {
                    value = convert_to_cache(defaults.get(getName()), record, true);
                }
            }
        }

        return convert_to_record(value, record);
    }

    private RecordSet _in_cache_without(RecordSet record, Field field) {
        RecordSet recs = record.browse(record._prefetch_ids);
        List<Object> ids = new ArrayList<>(record.ids());
        for (Object record_id : record.env().cache().get_missing_ids(recs.subtract(record), field)) {
            if (!Tools.hasId(record_id)) {
                continue;
            }
            ids.add(record_id);
        }
        return record.browse(ids);
    }

    /**
     * set the value of field on ``records``
     * 
     * @param records
     * @param value
     */
    public void set(RecordSet records, Object value) {
        List<Object> protected_ids = new ArrayList<>();
        List<Object> new_ids = new ArrayList<>();
        List<Object> other_ids = new ArrayList<>();
        for (Object record_id : records.ids()) {
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
                if (_relational()) {
                    new_records.call("modifiel", Arrays.asList(getName()), false);
                }
            }
        }
        if (!other_ids.isEmpty()) {
            records = records.browse(other_ids);
            Object write_value = convert_to_write(value, records);
            records.call("write", new Kvalues(k -> k.set(getName(), write_value)));
        }
    }

    /**
     * Invoke the compute method on ``records``; the results are in cache.
     * 
     * @param records
     */
    public void compute_value(RecordSet records) {
        Environment env = records.env();
        if (_compute_sudo()) {
            records = records.sudo();
        }
        List<Field> fields = records.type()._field_computed.get(this);
        for (Field field : fields) {
            env.remove_to_compute(field, records);
        }
        try (Protecting p = env.protecting(fields, records)) {
            Model._compute_field_value(records, this);
        } catch (Exception e) {
            throw e;
        }
    }

    void determine_inverse(RecordSet records) {
        Object inverse = getattr(Slots.inverse);
        if (inverse instanceof String) {
            records.call((String) inverse);
        }
        Consumer<RecordSet> func = (Consumer<RecordSet>) inverse;
        func.accept(records);
    }

    public List<Object> determine_domain(RecordSet records, String operator, Object value) {
        Object search = getattr(Slots.search);
        if (search instanceof String) {
            return (List<Object>) records.call((String) search, operator, value);
        }
        BiFunction<String, Object, List<Object>> func = (BiFunction<String, Object, List<Object>>) search;
        return func.apply(operator, value);
    }

    public boolean check_company() {
        return false;
    }
}
