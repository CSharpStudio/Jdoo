package jdoo.models._fields;

import java.util.ArrayList;
import java.util.List;

import jdoo.models.Domain;
import jdoo.models.RecordSet;
import jdoo.tools.Slot;
import jdoo.util.Dict;

/** Abstract class for relational fields. */
public abstract class _RelationalField<T extends _RelationalField<T>> extends BaseField<T> {
    /** domain for searching values */
    public static final Slot domain = new Slot("domain");
    /** context for searching values */
    public static final Slot context = new Slot("context");
    public static final Slot check_company = new Slot("check_company");
    static {
        default_slots().put(domain, new Domain());
        default_slots().put(context, new Dict());
        default_slots().put(check_company, false);
    }

    public _RelationalField() {
        relational = true;
    }

    @SuppressWarnings("unchecked")
    public T check_company(boolean check_company) {
        set(_RelationalField.check_company, check_company);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T comodel_name(String comodel_name) {
        set(_RelationalField.comodel_name, comodel_name);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T domain(Domain domain) {
        set(_RelationalField.domain, domain);
        return (T) this;
    }

    @Override
    public Object get(RecordSet records) {
        RecordSet comodel = records.env(comodel_name());
        List<RecordSet> list = new ArrayList<>();
        for (RecordSet record : records) {
            list.add((RecordSet) super.get(record));
        }
        return comodel.union(list);
    }
    // todo
}
