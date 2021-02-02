package jdoo.models._fields;

import jdoo.models.Domain;
import jdoo.util.Dict;

public class _RelationalField<T extends _RelationalField<T>> extends BaseField<T> {
    public _RelationalField() {
        relational = true;
    }

    Domain domain;
    Dict context;
    Boolean check_company;

    @SuppressWarnings("unchecked")
    public T check_company(boolean check_company) {
        this.check_company = check_company;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T comodel_name(String comodel_name) {
        this.comodel_name = comodel_name;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T domain(Domain domain) {
        this.domain = domain;
        return (T) this;
    }
}
