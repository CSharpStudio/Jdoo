package jdoo.models._fields;

import java.util.Collection;
import java.util.Map;

import jdoo.exceptions.ValueErrorException;
import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.tools.Tools;
import jdoo.util.Dict;
import jdoo.util.Pair;

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
    public static final Slot ondelete = new Slot("ondelete");
    /** whether joins are generated upon search */
    public static final Slot auto_join = new Slot("auto_join");
    /** whether self implements delegation */
    public static final Slot delegate = new Slot("delegate");
    static {
        default_slots().put(auto_join, false);
        default_slots().put(delegate, false);
    }

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
            RecordSet comodel = model.env(comodel_name());
            if (model.type().is_transient() && !comodel.type().is_transient()) {
                setattr(ondelete, required() ? "cascade" : "set null");
            } else {
                setattr(ondelete, required() ? "restrict" : "set null");
            }
        }
        if ("set null".equals(getattr(String.class, ondelete)) && required()) {
            throw new ValueErrorException(String.format(
                    "The m2o field %s of model %s is required but declares its ondelete policy "
                            + "as being 'set null'. Only 'restrict' and 'cascade' make sense.",
                    getName(), model.name()));
        }
    }

    @Override
    public boolean update_db(RecordSet model, Map<String, Dict> columns) {
        RecordSet comodel = model.env(comodel_name());
        if (!model.type().is_transient() && comodel.type().is_transient()) {
            throw new ValueErrorException(String.format("Many2one %s from Model to TransientModel is forbidden", this));
        }
        return super.update_db(model, columns);
    }

    @Override
    public void update_db_column(RecordSet model, Dict column) {
        super.update_db_column(model, column);
        // todo model.pool.post_init(self.update_db_foreign_key, model, column)
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Dict values, boolean validate) {
        return value;
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        // todo
        return super.convert_to_cache(value, record, validate);
    }

    @Override
    public Object convert_to_record(Object value, RecordSet record) {
        // TODO Auto-generated method stub
        return super.convert_to_record(value, record);
    }

    @Override
    public Object convert_to_read(Object value, RecordSet record) {
        // TODO Auto-generated method stub
        return super.convert_to_read(value, record);
    }

    @Override
    public Object convert_to_write(Object value, RecordSet record) {
        // TODO Auto-generated method stub
        return super.convert_to_write(value, record);
    }

    @Override
    public Object convert_to_export(Object value, RecordSet record) {
        // TODO Auto-generated method stub
        return super.convert_to_export(value, record);
    }

    @Override
    public String convert_to_display_name(Object value, RecordSet record) {
        // TODO Auto-generated method stub
        return super.convert_to_display_name(value, record);
    }

    @Override
    public Object convert_to_onchange(Object value, RecordSet record, Collection<String> names) {
        if (value == null || !Tools.hasId(((RecordSet) value).id())) {
            return null;
        }
        return super.convert_to_onchange(value, record, names);
    }

    @Override
    public RecordSet write(RecordSet records, Object value) {
        // todo
        return records;
    }
}
