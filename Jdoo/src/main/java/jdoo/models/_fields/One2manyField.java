package jdoo.models._fields;

import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import jdoo.exceptions.UserErrorException;
import jdoo.models.Field;
import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.util.Kvalues;

/**
 * One2many field; the value of such a field is the recordset of all the records
 * in ``comodel_name`` such that the field ``inverse_name`` is equal to the
 * current record.
 * 
 * :param comodel_name: name of the target model (string)
 * 
 * :param inverse_name: name of the inverse ``Many2one`` field in
 * ``comodel_name`` (string)
 * 
 * :param domain: an optional domain to set on candidate values on the client
 * side (domain or string)
 * 
 * :param context: an optional context to use on the client side when handling
 * that field (dictionary)
 * 
 * :param auto_join: whether JOINs are generated upon search through that field
 * (boolean, by default ``False``)
 * 
 * :param limit: optional limit to use upon read (integer)
 * 
 * The attributes ``comodel_name`` and ``inverse_name`` are mandatory except in
 * the case of related fields or field extensions.
 */
public class One2manyField extends _RelationalMultiField<One2manyField> {

    public static final Slot inverse_name = new Slot("inverse_name", null);
    public static final Slot auto_join = new Slot("auto_join", false);
    public static final Slot limit = new Slot("limit", null);

    public One2manyField() {
        setattr(Slots.copy, false);
    }

    public One2manyField inverse_name(String inverse_name) {
        setattr(One2manyField.inverse_name, inverse_name);
        return this;
    }

    public One2manyField limit(int limit) {
        setattr(One2manyField.limit, limit);
        return this;
    }

    public One2manyField auto_join(boolean auto_join) {
        setattr(One2manyField.auto_join, auto_join);
        return this;
    }

    public String _inverse_name() {
        return getattr(String.class, One2manyField.inverse_name);
    }

    public boolean _auto_join() {
        return getattr(Boolean.class, Many2oneField.auto_join);
    }

    @Override
    public void _setup_regular_full(RecordSet model) {
        super._setup_regular_full(model);
        if (hasattr(inverse_name) && StringUtils.hasText(getattr(String.class, inverse_name))) {
            RecordSet comodel = model.env(_comodel_name());
            Field invf = comodel.getField(getattr(String.class, inverse_name));
            if (invf instanceof Many2oneField || invf instanceof Many2oneReferenceField) {
                // setting one2many fields only invalidates many2one inverses;
                // integer inverses (res_model/res_id pairs) are not supported
                model.type().field_inverses().add(this, invf);
            }
            comodel.type().field_inverses().add(invf, this);
        }
    }

    @Override
    public boolean update_db(RecordSet model, Map<String, Kvalues> columns) {
        if (model.type().pool().contains(_comodel_name())) {
            RecordSet comodel = model.env(_comodel_name());
            if (!comodel.hasField(getattr(String.class, inverse_name))) {
                throw new UserErrorException(
                        String.format("No inverse field %s found for %s", getattr(inverse_name), _comodel_name()));
            }
        }
        return true;
    }

    @Override
    public List<Object> get_domain_list(RecordSet model) {
        // TODO Auto-generated method stub
        return super.get_domain_list(model);
    }

    @Override
    public void read(RecordSet records) {
        // TODO Auto-generated method stub
        super.read(records);
    }

    // todo
}
