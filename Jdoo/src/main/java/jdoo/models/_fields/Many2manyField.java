package jdoo.models._fields;

import java.util.List;
import java.util.Map;

import jdoo.models.RecordSet;
import jdoo.util.Kvalues;
import jdoo.util.Pair;

/**
 * Many2many field; the value of such a field is the recordset.
 * 
 * :param comodel_name: name of the target model (string)
 * 
 * The attribute ``comodel_name`` is mandatory except in the case of related
 * fields or field extensions.
 * 
 * :param relation: optional name of the table that stores the relation in the
 * database (string)
 * 
 * :param column1: optional name of the column referring to "these" records in
 * the table ``relation`` (string)
 * 
 * :param column2: optional name of the column referring to "those" records in
 * the table ``relation`` (string)
 * 
 * The attributes ``relation``, ``column1`` and ``column2`` are optional. If not
 * given, names are automatically generated from model names, provided
 * ``model_name`` and ``comodel_name`` are different!
 * 
 * Note that having several fields with implicit relation parameters on a given
 * model with the same comodel is not accepted by the ORM, since those field
 * would use the same table. The ORM prevents two many2many fields to use the
 * same relation parameters, except if
 * 
 * - both fields use the same model, comodel, and relation parameters are
 * explicit; or
 * 
 * - at least one field belongs to a model with ``_auto = False``.
 * 
 * :param domain: an optional domain to set on candidate values on the client
 * side (domain or string)
 * 
 * :param context: an optional context to use on the client side when handling
 * that field (dictionary)
 * 
 * :param limit: optional limit to use upon read (integer)
 * 
 * :param check_company: add default domain ``['|', ('company_id', '=', False),
 * ('company_id', '=', company_id)]``. Mark the field to be verified in
 * ``_check_company``.
 */
public class Many2manyField extends _RelationalMultiField<Many2manyField> {
    // todo
    public Many2manyField() {

    }

    // TODO
    boolean _explicit = true;
    String relation;
    String column1;
    String column2;
    Boolean auto_join;
    Integer limit;
    String ondelete;

    public String relation() {
        return relation;
    }

    public Many2manyField relation(String relation) {
        this.relation = relation;
        return this;
    }

    public String column1() {
        return column1;
    }

    public Many2manyField column1(String column1) {
        this.column1 = column1;
        return this;
    }

    public String column2() {
        return column2;
    }

    public Many2manyField column2(String column2) {
        this.column2 = column2;
        return this;
    }

    public Many2manyField ondelete(String ondelete) {
        this.ondelete = ondelete;
        return this;
    }

    public Many2manyField limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Many2manyField auto_join(boolean auto_join) {
        this.auto_join = auto_join;
        return this;
    }

    @Override
    public boolean update_db(RecordSet model, Map<String, Kvalues> columns) {
        return true;
    }

    @Override
    protected Object write_real(List<Pair<RecordSet, Object>> records_commands_list, boolean create) {
        // TODO Auto-generated method stub
        return null;
    }
}
