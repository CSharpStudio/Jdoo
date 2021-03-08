package jdoo.models._fields;

import java.util.List;
import java.util.function.Function;

import org.springframework.util.StringUtils;

import jdoo.apis.Environment;
import jdoo.data.PgVarchar;
import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.util.Kvalues;
import jdoo.util.Pair;

/**
 * :param selection: specifies the possible values for this field. It is given
 * as either a list of pairs ``(value, label)``, or a model method, or a method
 * name.
 * 
 * :param selection_add: provides an extension of the selection in the case of
 * an overridden field. It is a list of pairs ``(value, label)`` or singletons
 * ``(value,)``, where singleton values must appear in the overridden selection.
 * The new values are inserted in an order that is consistent with the
 * overridden selection and this list::
 * 
 * selection = [('a', 'A'), ('b', 'B')] selection_add = [('c', 'C'), ('b',)] >
 * result = [('a', 'A'), ('c', 'C'), ('b', 'B')]
 * 
 * The attribute ``selection`` is mandatory except in the case of :ref:`related
 * fields <field-related>` or :ref:`field extensions
 * <field-incremental-definition>`.
 */
public class SelectionField extends BaseField<SelectionField> {

    public static final Slot selection = new Slot("selection");
    public static final Slot validate = new Slot("validate");
    static {
        default_slots().put(validate, true);
    }

    Function<RecordSet, Object> $selection;

    public SelectionField() {
        column_type = new Pair<>("varchar", new PgVarchar());
    }

    public SelectionField selection(String selection) {
        setattr(SelectionField.selection, selection);
        return this;
    }

    public SelectionField selection(List<Pair<String, String>> selection) {
        setattr(SelectionField.selection, selection);
        return this;
    }

    public SelectionField selection_add(List<Pair<String, String>> selection) {
        // todo
        return this;
    }

    public SelectionField selection(Function<RecordSet, Object> func) {
        setattr(SelectionField.selection, func);
        return this;
    }

    public SelectionField validate(boolean validate) {
        setattr(SelectionField.validate, validate);
        return this;
    }

    @Override
    public void _setup_regular_base(RecordSet model) {
        super._setup_regular_base(model);
        assert hasattr(selection) : String.format("Field %s with non-str value in selection", this);
    }

    @Override
    public void _setup_regular_full(RecordSet model) {
        super._setup_regular_full(model);
        SelectionField field = getattr(SelectionField.class, Slots.related_field);
        $selection = m -> field._description_selection(model.env());
    }

    @Override
    public void _setup_attrs(RecordSet model, String name) {
        super._setup_attrs(model, name);
        // todo
    }

    Object _description_selection(Environment env) {
        Object _selection = getattr(selection);
        if (_selection instanceof String) {
            return env.get(model_name()).call((String) _selection);
        } else if (_selection instanceof Function) {
            return ((Function<RecordSet, Object>) _selection).apply(env.get(model_name()));
        }
        if (StringUtils.hasText(env.lang())) {
            return env.get("ir.translation").call("get_field_selection", model_name(), getName());
        } else {
            return _selection;
        }
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Kvalues values, boolean validate) {
        if (validate && getattr(Boolean.class, SelectionField.validate)) {
            value = convert_to_cache(value, record, true);
        }
        return super.convert_to_column(value, record, values, validate);
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        if (!validate) {
            return value;
        }
        // todo
        return super.convert_to_cache(value, record, validate);
    }

    @Override
    public Object convert_to_export(Object value, RecordSet record) {
        // todo
        return super.convert_to_export(value, record);
    }
}
