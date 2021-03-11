package jdoo.models._fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import jdoo.models.Domain;
import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.util.Kvalues;

/** Abstract class for relational fields. */
public abstract class _RelationalField<T extends _RelationalField<T>> extends BaseField<T> {
    /** domain for searching values */
    public static final Slot domain = new Slot("domain", Collections.emptyList());
    /** context for searching values */
    public static final Slot context = new Slot("context", Kvalues.empty());
    public static final Slot check_company = new Slot("check_company", false);

    public _RelationalField() {
        relational = true;
    }

    public T check_company(boolean check_company) {
        setattr(_RelationalField.check_company, check_company);
        return (T) this;
    }

    @Override
    public boolean check_company() {
        return getattr(Boolean.class, _RelationalField.check_company);
    }

    public T comodel_name(String comodel_name) {
        setattr(Slots.comodel_name, comodel_name);
        return (T) this;
    }

    public T context(Kvalues ctx) {
        setattr(_RelationalField.context, ctx);
        return (T) this;
    }

    public T domain(Domain domain) {
        setattr(_RelationalField.domain, domain);
        return (T) this;
    }

    public T domain(Function<RecordSet, Domain> domain) {
        setattr(_RelationalField.domain, domain);
        return (T) this;
    }

    @Override
    public Object get(RecordSet records) {
        // base case: do the regular access
        if (records == null || records.size() <= 1) {
            return super.get(records);
        }
        // multirecord case: return the union of the values of 'self' on records
        RecordSet comodel = records.env(_comodel_name());
        List<RecordSet> list = new ArrayList<>();
        for (RecordSet record : records) {
            list.add((RecordSet) super.get(record));
        }
        return comodel.union(list);
    }

    @Override
    public void _setup_regular_base(RecordSet model) {
        super._setup_regular_base(model);
        if (!model.type().pool().contains(_comodel_name())) {
            _logger.warn("Field {} with unknown comodel_name {}", this, _comodel_name());
            comodel_name("_unknown");
        }
    }

    public List<Object> get_domain_list(RecordSet model) {
        Object d = getattr(_RelationalField.domain);
        if (d instanceof Function) {
            return ((Function<RecordSet, List<Object>>) d).apply(model);
        }
        if (d instanceof List) {
            return (List<Object>) d;
        }
        return Collections.emptyList();
    }
    // todo
}
