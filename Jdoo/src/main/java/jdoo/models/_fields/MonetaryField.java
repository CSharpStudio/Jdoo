package jdoo.models._fields;

import jdoo.models.Field;
import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.util.Kvalues;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.util.TypeUtils;

/**
 * The decimal precision and currency symbol are taken from the attribute.
 *  
 * :param currency_field: name of the field holding the currency this monetary
 * field is expressed in (default: `currency_id`)
 */
public class MonetaryField extends BaseField<MonetaryField> {
    public static final Slot currency_field = new Slot("currency_field");

    public MonetaryField() {
        setattr(Slots.group_operator, "sum");
        column_type = new Pair<>("numeric", "numeric");
        column_cast_from = new Tuple<>("float8");
    }

    public MonetaryField currency_field(String currency_field) {
        setattr(MonetaryField.currency_field, currency_field);
        return this;
    }

    void _setup_currency_field(RecordSet model) {
        if (!hasattr(currency_field)) {
            if (model.hasField("currency_id")) {
                setattr(currency_field, "currency_id");
            } else if (model.hasField("x_currency_id")) {
                setattr(currency_field, "x_currency_id");
            }
        }
        assert model.hasField(getattr(String.class, currency_field))
                : String.format("Field %s with unknown currency_field %s", toString(), getattr(currency_field));
    }

    @Override
    public void _setup_regular_full(RecordSet model) {
        super._setup_regular_full(model);
        _setup_currency_field(model);
    }

    @Override
    public void _setup_related_full(RecordSet model) {
        super._setup_related_full(model);
        if (inherited()) {
            setattr(currency_field, related_field().getattr(currency_field));
        }
        _setup_currency_field(model);
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Kvalues values, boolean validate) {
        String _currency_field = getattr(String.class, currency_field);
        RecordSet currency;
        if (values != null && values.containsKey(_currency_field)) {
            Field field = record.getField(_currency_field);
            currency = (RecordSet) field.convert_to_cache(values.get(_currency_field), record, validate);
            currency = (RecordSet) field.convert_to_record(currency, record);
        } else {
            currency = (RecordSet) record.get(0).get(_currency_field);
        }
        value = TypeUtils.toDouble(value);
        if (currency != null && currency.hasId()) {
            return currency.call("round", value);
        }
        return value;
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        value = TypeUtils.toDouble(value);
        String _currency_field = getattr(String.class, currency_field);
        RecordSet currency = record.get(RecordSet.class, _currency_field);
        if (validate && currency != null && currency.hasId()) {
            value = currency.call("round", value);
        }
        return value;
    }
}
