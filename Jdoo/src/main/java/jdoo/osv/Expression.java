package jdoo.osv;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import jdoo.data.Cursor;
import jdoo.exceptions.ValueErrorException;
import jdoo.models.Domain;
import jdoo.models.Field;
import jdoo.models.RecordSet;
import jdoo.models.d;
import jdoo.models._fields.BinaryField;
import jdoo.models._fields.BooleanField;
import jdoo.models._fields.DateTimeField;
import jdoo.models._fields.IntegerField;
import jdoo.models._fields.Many2manyField;
import jdoo.models._fields.Many2oneField;
import jdoo.models._fields.One2manyField;
import jdoo.util.Default;
import jdoo.util.Dict;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.util.Utils;

public class Expression {
    private static Logger _logger = LogManager.getLogger(Expression.class);

    public static String NOT_OPERATOR = "!";
    public static String OR_OPERATOR = "|";
    public static String AND_OPERATOR = "&";
    public static List<String> DOMAIN_OPERATORS = Arrays.asList(NOT_OPERATOR, OR_OPERATOR, AND_OPERATOR);

    public static Tuple<String> TERM_OPERATORS = new Tuple<>("=", "!=", "<=", "<", ">", ">=", "=?", "=like", "=ilike",
            "like", "not like", "ilike", "not ilike", "in", "not in", "child_of", "parent_of");

    public static Tuple<String> NEGATIVE_TERM_OPERATORS = new Tuple<>("!=", "not like", "not ilike", "not in");

    public static Map<String, String> DOMAIN_OPERATORS_NEGATION = new Dict<>(d -> d.set(AND_OPERATOR, OR_OPERATOR)//
            .set(OR_OPERATOR, AND_OPERATOR));

    public static Map<String, String> TERM_OPERATORS_NEGATION = new Dict<>(d -> d//
            .set("<", ">=")//
            .set(">", "<=")//
            .set("<=", ">")//
            .set(">=", "<")//
            .set("=", "!=")//
            .set("!=", "=")//
            .set("in", "not in")//
            .set("like", "not like")//
            .set("ilike", "not ilike")//
            .set("not in", "in")//
            .set("not like", "like")//
            .set("not ilike", "ilike"));

    public static Tuple<Object> TRUE_LEAF = new Tuple<>(1, "=", 1);
    public static Tuple<Object> FALSE_LEAF = new Tuple<>(0, "=", 1);
    public static List<Object> TRUE_DOMAIN = Arrays.asList(TRUE_LEAF);
    public static List<Object> FALSE_DOMAIN = Arrays.asList(FALSE_LEAF);

    public static boolean is_false(List<Object> domain) {
        // todo
        return false;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> combine(String operator, List<Object> unit, List<Object> zero, List<Object>... domains) {
        List<Object> result = new ArrayList<>();
        int count = 0;
        if (domains.length == 1 && domains[0].equals(unit)) {
            return unit;
        }
        for (List<Object> domain : domains) {
            if (unit.equals(domain)) {
                continue;
            }
            if (zero.equals(domain)) {
                return zero;
            }
            if (domain != null) {
                result.addAll(normalize_domain(domain));
                count += 1;
            }
        }
        for (int i = 0; i < count - 1; i++) {
            result.add(0, operator);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> AND(List<Object>... domains) {
        return combine(AND_OPERATOR, TRUE_DOMAIN, FALSE_DOMAIN, domains);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> OR(List<Object>... domains) {
        return combine(OR_OPERATOR, FALSE_DOMAIN, TRUE_DOMAIN, domains);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> distribute_not(List<Object> domain) {
        List<Object> result = new ArrayList<>();
        Stack<Boolean> stack = new Stack<>();
        stack.add(false);
        for (Object token : domain) {
            boolean negate = stack.pop();
            if (is_leaf(token, false)) {
                if (negate) {
                    Tuple<Object> tuple = (Tuple<Object>) token;
                    Object left = tuple.get(0);
                    Object operator = tuple.get(1);
                    Object right = tuple.get(2);
                    if (TERM_OPERATORS_NEGATION.containsKey(operator)) {
                        result.add(new Tuple<>(left, TERM_OPERATORS_NEGATION.get(operator), right));
                    } else {
                        result.add(NOT_OPERATOR);
                        result.add(token);
                    }
                } else {
                    result.add(token);
                }
            } else if (NOT_OPERATOR.equals(token)) {
                stack.add(!negate);
            } else if (DOMAIN_OPERATORS_NEGATION.containsKey(token)) {
                result.add(negate ? DOMAIN_OPERATORS_NEGATION.get(token) : token);
                stack.add(negate);
                stack.add(negate);
            } else {
                result.add(token);
            }
        }
        return result;
    }

    public static String _quote(String to_quote) {
        if (to_quote.contains("\"")) {
            return to_quote;
        }
        return "\"" + to_quote + "\"";
    }

    public static Pair<String, String> get_alias_from_query(String from_query) {
        String[] from_splitted = from_query.split(" as ");
        if (from_splitted.length > 1) {
            return new Pair<String, String>(from_splitted[0].replace("\"", ""), from_splitted[1].replace("\"", ""));
        } else {
            return new Pair<String, String>(from_splitted[0].replace("\"", ""), from_splitted[0].replace("\"", ""));
        }
    }

    public static Pair<String, String> generate_table_alias(String src_table_alias,
            List<Pair<String, String>> joined_tables) {
        String alias = src_table_alias;
        if (joined_tables.isEmpty()) {
            return new Pair<>(alias, _quote(alias));
        }
        for (Pair<String, String> link : joined_tables) {
            alias += "__" + link.second();
        }
        if (alias.length() >= 64) {
            byte[] b = alias.getBytes(Charset.forName("UTF-8"));
            CRC32 c = new CRC32();
            c.reset();// Resets CRC-32 to initial value.
            c.update(b, 0, b.length);
            String alias_hash = Long.toHexString(c.getValue());
            alias = alias.substring(0, 62 - alias_hash.length()) + "_" + alias_hash;
        }
        return new Pair<>(alias,
                String.format("%s as %s", _quote(joined_tables.get(joined_tables.size() - 1).first()), _quote(alias)));
    }

    static boolean is_leaf(Object element, @Default("false") boolean internal) {
        List<Object> INTERNAL_OPS = new ArrayList<>(TERM_OPERATORS);
        INTERNAL_OPS.add("<>");
        if (internal) {
            INTERNAL_OPS.add("inselect");
            INTERNAL_OPS.add("not inselect");
        }

        if (element instanceof List) {
            List<?> e = (List<?>) element;
            return e.size() == 3 && INTERNAL_OPS.contains(e.get(1))
                    && (e.get(0) instanceof String && StringUtils.hasText((String) e.get(0)) || TRUE_LEAF.equals(e)
                            || FALSE_LEAF.equals(e));
        }

        return false;
    }

    static List<Object> normalize_domain(List<Object> domain) {
        if (domain.isEmpty()) {
            return TRUE_DOMAIN;
        }
        List<Object> result = new ArrayList<>();
        int expected = 1;
        Map<String, Integer> op_arity = new Dict<>(
                d -> d.set(NOT_OPERATOR, 1).set(AND_OPERATOR, 2).set(OR_OPERATOR, 2));

        for (Object token : domain) {
            if (expected == 0) {
                result.add(0, AND_OPERATOR);
                expected = 1;
            }
            result.add(token);
            if (token instanceof List) {
                expected -= 1;
            } else {
                expected += op_arity.getOrDefault(token, 0) - 1;
            }
        }
        assert expected == 0 : String.format("This domain is syntactically not correct: %s", domain);
        return result;
    }

    RecordSet root_model;
    List<Object> joins;
    List<ExtendedLeaf> result;
    List<Object> expression;
    Stack<ExtendedLeaf> stack;

    public Expression(List<Object> domain, RecordSet model) {
        joins = new ArrayList<>();
        root_model = model;
        expression = distribute_not(normalize_domain(domain));
        parse();
    }

    public List<String> get_tables() {
        List<String> tables = new ArrayList<>();
        for (ExtendedLeaf leaf : result) {
            for (String table : leaf.get_tables()) {
                if (!tables.contains(table)) {
                    tables.add(table);
                }
            }
        }
        String table_name = _quote(root_model.table());
        if (!tables.contains(table_name)) {
            tables.add(table_name);
        }
        return tables;
    }

    @SuppressWarnings("unchecked")
    Collection<Object> to_ids(Object value, RecordSet comodel, Object leaf) {
        List<Object> names = new ArrayList<>();
        if (value instanceof String) {
            names.add(value);
        } else if (value instanceof List && ((List<Object>) value).stream().allMatch(item -> item instanceof String)) {
            names = (List<Object>) value;
        }
        if (!names.isEmpty()) {
            return names.stream().map(name -> comodel.name_search((String) name, Collections.emptyList(), "ilike", -1))
                    .collect(Collectors.toList());
        }
        return (List<Object>) value;
    }

    @SuppressWarnings("unchecked")
    List<Object> child_of_domain(Object left, Collection<?> ids, RecordSet left_model, @Default String parent,
            @Default("") String prefix) {
        if (ids.isEmpty()) {
            return FALSE_DOMAIN;
        }
        if (left_model.type().parent_store()) {
            List<Object> doms = OR(left_model.browse(ids).stream()
                    .map(rec -> new Tuple<Object>("parent_path", "=like", rec.get("parent_path") + "%"))
                    .collect(Collectors.toList()));
            if (StringUtils.hasText(prefix)) {
                return Arrays.asList(new Tuple<>(left, "in", left_model.search(doms).ids()));
            }
            return doms;
        } else {
            String parent_name = StringUtils.hasText(parent) ? parent : left_model.type().parent_name();
            Set<Object> child_ids = new HashSet<>(ids);
            while (!ids.isEmpty()) {
                ids = left_model.search(Arrays.asList(new Tuple<>(parent_name, "in", ids))).ids();
                child_ids.addAll(ids);
            }
            return Arrays.asList(new Tuple<>(left, "in", child_ids));
        }
    }

    List<Object> parent_of_domain(Object left, Collection<?> ids, RecordSet left_model, @Default String parent,
            @Default("") String prefix) {
        if (left_model.type().parent_store()) {
            List<Object> parent_ids = left_model.browse(ids).stream()
                    .flatMap(rec -> Stream.of(((String) rec.get("parent_path")).split("/")))// todo [:-1] 不取最后一个值
                    .collect(Collectors.toList());
            return Arrays.asList(new Tuple<>("id", "in", parent_ids));
        } else {
            String parent_name = StringUtils.hasText(parent) ? parent : left_model.type().parent_name();
            Set<Object> parent_ids = new HashSet<>();
            for (RecordSet record : left_model.browse(ids)) {
                while (record.hasId()) {
                    parent_ids.add(record.id());
                    Object p = record.get(parent_name);
                    if (p instanceof RecordSet) {
                        record = (RecordSet) p;
                    } else {
                        break;
                    }
                }
            }
            return Arrays.asList(new Tuple<>(left, "in", parent_ids));
        }
    }

    public Pair<String, List<Object>> to_sql() {
        Stack<String> stack = new Stack<>();
        List<Object> params = new ArrayList<>();
        Map<String, String> ops = new Dict<>(d -> d.set(AND_OPERATOR, "AND").set(OR_OPERATOR, "OR"));
        for (int i = result.size() - 1; i >= 0; i--) {
            ExtendedLeaf leaf = result.get(i);
            if (leaf.is_leaf(true)) {
                Pair<String, List<Object>> pair = __leaf_to_sql(leaf);
                stack.add(pair.first());
                List<Object> list = pair.second();
                for (int x = list.size() - 1; x >= 0; x--) {
                    params.add(list.get(x));
                }
                params.addAll(pair.second());
            } else if (NOT_OPERATOR.equals(leaf.leaf)) {
                stack.add(String.format("(NOT (%s))", stack.pop()));
            } else {
                String q1 = stack.pop();
                String q2 = stack.pop();
                stack.add(String.format("(%s %s %s)", q1, ops.get(leaf.leaf), q2));
            }
        }
        assert stack.size() == 1;
        String query = stack.get(0);
        String _joins = String.join(" AND ", joins.toArray(new String[joins.size()]));
        if (StringUtils.hasText(_joins)) {
            query = String.format("(%s) AND %s", _joins, query);
        }
        Collections.reverse(params);
        return new Pair<>(query, params);
    }

    @SuppressWarnings("unchecked")
    void parse() {
        result = new ArrayList<>();
        stack = new Stack<>();
        for (int i = expression.size() - 1; i >= 0; i--) {
            Object leaf = expression.get(i);
            stack.add(new ExtendedLeaf(leaf, root_model, null, false));
        }
        Cursor cr = root_model.cr();
        while (stack.size() > 0) {
            ExtendedLeaf leaf = stack.pop();
            Object left, operator, right;
            if (leaf.is_operator()) {
                left = leaf.leaf;
                operator = null;
                right = null;
            } else if (leaf.is_true_leaf() || leaf.is_false_leaf()) {
                Tuple<Object> l = (Tuple<Object>) leaf.leaf;
                left = l.get(0).toString();
                operator = l.get(1);
                right = l.get(2);
            } else {
                Tuple<Object> l = (Tuple<Object>) leaf.leaf;
                left = l.get(0);
                operator = l.get(1);
                right = l.get(2);
            }
            // ----------------------------------------
            // SIMPLE CASE
            // 1. leaf is an operator
            // 2. leaf is a true/false leaf
            // -> add directly to result
            // ----------------------------------------
            if (leaf.is_operator() || leaf.is_true_leaf() || leaf.is_false_leaf()) {
                result.add(leaf);
            } else {
                String[] path = left.toString().split("\\.", 2);

                RecordSet model = leaf.model;
                Field field = model.getField(path[0]);
                String comodel_name = field.comodel_name();
                RecordSet comodel = StringUtils.hasText(comodel_name) ? model.env(comodel_name) : null;

                if (field.inherited()) {
                    // comments about inherits'd fields
                    // { 'field_name': ('parent_model', 'm2o_field_to_reach_parent',
                    // field_column_obj, origina_parent_model), ... }
                    RecordSet parent_model = model.env(field.related_field().model_name());
                    String parent_fname = model.type().inherits().get(parent_model.name());
                    leaf.add_join_context(parent_model, parent_fname, "id", parent_fname);
                    stack.add(leaf);
                } else if ("id".equals(left) && ("child_of".equals(operator) || "parent_of".equals(operator))) {
                    Collection<Object> ids2 = to_ids(right, model, leaf.leaf);
                    List<Object> dom = "child_of".equals(operator) ? child_of_domain(left, ids2, model, null, "")
                            : parent_of_domain(left, ids2, model, null, "");
                    Collections.reverse(dom);
                    for (Object dom_leaf : dom) {
                        ExtendedLeaf new_leaf = create_substitution_leaf(leaf, dom_leaf, model, false);
                        stack.add(new_leaf);
                    }
                }
                // ----------------------------------------
                // PATH SPOTTED
                // -> many2one or one2many with _auto_join:
                // - add a join, then jump into linked column: column.remaining on
                // src_table is replaced by remaining on dst_table, and set for re-evaluation
                // - if a domain is defined on the column, add it into evaluation
                // on the relational table
                // -> many2one, many2many, one2many: replace by an equivalent computed
                // domain, given by recursively searching on the remaining of the path
                // -> note: hack about columns.property should not be necessary anymore
                // as after transforming the column, it will go through this loop once again
                // ----------------------------------------
                else if (path.length > 1 && field.store() && field instanceof Many2oneField
                        && ((Many2oneField) field).auto_join()) {
                    // res_partner.state_id = res_partner__state_id.id
                    leaf.add_join_context(comodel, path[0], "id", path[0]);
                    stack.add(create_substitution_leaf(leaf, new Tuple<>(path[1], operator, right), comodel, false));
                } else if (path.length > 1 && field.store() && field instanceof One2manyField
                        && ((One2manyField) field).auto_join()) {
                    // res_partner.id = res_partner__bank_ids.partner_id
                    leaf.add_join_context(comodel, "id", ((One2manyField) field).inverse_name(), path[0]);
                    List<Object> domain = ((One2manyField) field).get_domain_list(model);
                    stack.add(create_substitution_leaf(leaf, new Tuple<>(path[1], operator, right), comodel, false));
                    if (!domain.isEmpty()) {
                        domain = normalize_domain(domain);
                        for (int i = domain.size() - 1; i >= 0; i--) {
                            Object elem = domain.get(i);
                            stack.add(create_substitution_leaf(leaf, elem, comodel, false));
                        }
                        stack.add(create_substitution_leaf(leaf, AND_OPERATOR, comodel, false));
                    }
                } else if (path.length > 1 && field.store() && field instanceof Many2oneField) {
                    Collection<?> right_ids = comodel.with_context(ctx -> ctx.put("active_test", false))
                            .search(d.on(path[1], (String) operator, right)).ids();
                    leaf.leaf = new Tuple<>(path[0], "in", right_ids);
                    stack.add(leaf);
                }
                // Making search easier when there is a left operand as one2many or many2many
                else if (path.length > 1 && field.store()
                        && (field instanceof Many2manyField || field instanceof One2manyField)) {
                    Collection<?> right_ids = comodel.search(d.on(path[1], (String) operator, right)).ids();
                    leaf.leaf = new Tuple<>(path[0], "in", right_ids);
                    stack.add(leaf);
                } else if (!field.store()) {
                    // Non-stored field should provide an implementation of search.
                    List<Object> domain;
                    if (!field.search()) {
                        _logger.error("Non-stored field {} cannot be searched.", field);
                        if (_logger.isDebugEnabled()) {
                            // _logger.debug(''.join(traceback.format_stack()))
                        }
                        // Ignore it: generate a dummy leaf.
                        domain = Collections.emptyList();
                    } else {
                        if (path.length > 1) {
                            right = comodel.search(Arrays.asList(new Tuple<>(path[1], operator, right))).ids();
                            operator = "in";
                        }
                        domain = field.determine_domain(model, (String) operator, right);
                    }
                    if (domain.isEmpty()) {
                        leaf.leaf = TRUE_LEAF;
                        stack.add(leaf);
                    } else {
                        Collections.reverse(domain);
                        for (Object elem : domain) {
                            stack.add(create_substitution_leaf(leaf, elem, model, true));
                        }
                    }
                }
                // -------------------------------------------------
                // RELATIONAL FIELDS
                // -------------------------------------------------

                // Applying recursivity on field(one2many)
                else if (field instanceof One2manyField
                        && ("child_of".equals(operator) || "parent_of".equals(operator))) {
                    Collection<Object> ids2 = to_ids(right, comodel, leaf.leaf);
                    List<Object> dom;
                    if (field.comodel_name() != model.name()) {
                        dom = "child_of".equals(operator)
                                ? child_of_domain(left, ids2, model, null, field.comodel_name())
                                : parent_of_domain(left, ids2, model, null, field.comodel_name());
                    } else {
                        dom = "child_of".equals(operator) ? child_of_domain("id", ids2, model, (String) left, "")
                                : parent_of_domain("id", ids2, model, (String) left, "");
                    }
                    Collections.reverse(dom);
                    for (Object dom_leaf : dom) {
                        stack.add(create_substitution_leaf(leaf, dom_leaf, model, false));
                    }
                } else if (field instanceof One2manyField) {
                    One2manyField f = (One2manyField) field;
                    List<Object> domain = f.get_domain_list(model);
                    Field inverse_field = comodel.getField(f.inverse_name());
                    boolean inverse_is_int = inverse_field instanceof IntegerField;

                    if (right != null) {
                        Collection<Object> ids1, ids2;
                        if (right instanceof String) {
                            String op2 = TERM_OPERATORS_NEGATION.containsKey(operator)
                                    ? TERM_OPERATORS_NEGATION.get(operator)
                                    : (String) operator;
                            ids2 = new ArrayList<>();
                            for (Pair<Object, Object> pair : comodel.name_search((String) right, domain, op2, null)) {
                                ids2.add(pair.first());
                            }
                        } else if (right instanceof Collection) {
                            ids2 = (Collection<Object>) right;
                        } else {
                            ids2 = Arrays.asList(right);
                        }
                        if (!ids2.isEmpty() && inverse_is_int && !domain.isEmpty()) {
                            List<Object> _domain = d.on("id", "in", ids2);
                            _domain.addAll(domain);
                            ids2 = (Collection<Object>) comodel.search(_domain).ids();
                        }
                        if (ids2.isEmpty()) {
                            ids1 = Collections.emptyList();
                        } else if (inverse_field.store()) {
                            ids1 = select_from_where(cr, f.inverse_name(), comodel.table(), "id", ids2,
                                    (String) operator);
                        } else {
                            RecordSet recs = comodel.browse(ids2).sudo()
                                    .with_context(ctx -> ctx.put("prefetch_fields", false));
                            ids1 = null;
                            ids1 = (Collection<Object>) ((RecordSet) recs.mapped(f.inverse_name())).ids();
                        }
                        String op1 = NEGATIVE_TERM_OPERATORS.contains(operator) ? "not in" : "in";
                        stack.add(create_substitution_leaf(leaf, new Tuple<>("id", op1, ids1), model, false));
                    } else {
                        Collection<Object> ids1;
                        if (inverse_field.store() && !(inverse_is_int && !domain.isEmpty())) {
                            ids1 = select_distinct_from_where_not_null(cr, f.inverse_name(), comodel.table());
                        } else {
                            Domain comodel_domain = d.on(f.inverse_name(), "!=", false);
                            if (inverse_is_int && !domain.isEmpty()) {
                                comodel_domain.addAll(domain);
                            }
                            RecordSet recs = comodel.search(comodel_domain).sudo()
                                    .with_context(ctx -> ctx.put("prefetch_fields", false));
                            ids1 = (Collection<Object>) ((RecordSet) recs.mapped(f.inverse_name())).ids();

                        }
                        String op1 = NEGATIVE_TERM_OPERATORS.contains(operator) ? "in" : "not in";
                        stack.add(create_substitution_leaf(leaf, new Tuple<>("id", op1, ids1), model, false));
                    }
                } else if (field instanceof Many2manyField) {
                    Many2manyField f = (Many2manyField) field;
                    String rel_table = f.relation();
                    String rel_id1 = f.column1();
                    String rel_id2 = f.column2();
                    //todo
                } else if (field instanceof Many2oneField) {
                    int todo = 0;
                    // TODO
                } else if (field instanceof BinaryField && ((BinaryField) field).attachment()) {
                    // TODO
                    int todo = 0;
                } else {
                    if (field instanceof DateTimeField && right != null) {
                        if (right instanceof String && ((String) right).length() == 10) {
                            if (">".equals(operator) || "<=".equals(operator)) {
                                right += " 23:59:59";
                            } else {
                                right += " 00:00:00";
                            }
                            stack.add(create_substitution_leaf(leaf, new Tuple<>(left, operator, right), model, false));
                        } else if (right instanceof Date) {
                            //
                            stack.add(create_substitution_leaf(leaf, new Tuple<>(left, operator, right), model, false));
                        }
                        result.add(leaf);
                    } else if (field.translate() && right != null) {
                        boolean need_wildcard = "like".equals(operator) || "ilike".equals(operator)
                                || "not like".equals(operator) || "not ilike".equals(operator);
                        String sql_operator = (String) operator;
                        if ("=like".equals(operator)) {
                            sql_operator = "like";
                        }
                        if ("=ilike".equals(operator)) {
                            sql_operator = "ilike";
                        }
                        if (need_wildcard) {
                            right = "%" + right + "%";
                        }
                        String inselect_operator = "inselect";
                        if (NEGATIVE_TERM_OPERATORS.contains(sql_operator)) {
                            sql_operator = sql_operator.startsWith("not") ? sql_operator.substring(4) : "=";
                            inselect_operator = "not inselect";
                        }
                        if ("in".equals(sql_operator)) {
                            if (!(right instanceof Collection)) {
                                right = new Tuple<>(right);
                            }
                        }

                        String subselect = "WITH temp_irt_current (id, name) as (\n"//
                                + "     SELECT ct.id, coalesce(it.value,ct." + _quote((String) left) + ")\n"//
                                + "     FROM " + model.table() + " ct\n"//
                                + "     LEFT JOIN ir_translation it ON (it.name = %s and\n"//
                                + "          it.lang = %s and\n"//
                                + "          it.type = %s and\n"//
                                + "          it.res_id = ct.id and\n"//
                                + "          it.value != '')\n"//
                                + "     )\n"//
                                + "     SELECT id FROM temp_irt_current WHERE name " + sql_operator
                                + " %s order by name\n";

                        Tuple<Object> params = new Tuple<>(model.name() + ',' + left, "zh-cn", // get_lang(model.env()).code,
                                "model", right);
                        stack.add(create_substitution_leaf(leaf,
                                new Tuple<>("id", inselect_operator, new Tuple<>(subselect, params)), model, true));

                    } else {
                        result.add(leaf);
                    }
                }
            }
        }

        Set<String> _joins = new HashSet<>();
        for (ExtendedLeaf leaf : result) {
            _joins.addAll(leaf.get_join_conditions());
        }
        joins = new ArrayList<>(_joins);
    }

    static List<Object> select_from_where(Cursor cr, String select_field, String from_table, String where_field,
            Collection<Object> where_ids, String where_operator) {
        List<Object> res = new ArrayList<>();
        if (!where_ids.isEmpty()) {
            if ("<".equals(where_operator) || ">".equals(where_operator) || ">=".equals(where_operator)
                    || "<=".equals(where_operator)) {
                cr.execute(String.format("SELECT \"%s\" FROM \"%s\" WHERE \"%s\" %s %%s", select_field, from_table,
                        where_field, where_operator), Arrays.asList(where_ids.toArray()[0]));
                for (Tuple<?> tuple : cr.fetchall()) {
                    res.add(tuple.get(0));
                }
            } else {
                for (Tuple<?> subids : cr.split_for_in_conditions(where_ids)) {
                    cr.execute(String.format("SELECT \"%s\" FROM \"%s\" WHERE \"%s\" IN %%s", select_field, from_table,
                            where_field), new Tuple<>(subids));
                    for (Tuple<?> tuple : cr.fetchall()) {
                        res.add(tuple.get(0));
                    }
                }
            }
        }
        return res;
    }

    static List<Object> select_distinct_from_where_not_null(Cursor cr, String select_field, String from_table) {
        cr.execute(String.format("SELECT distinct(\"%s\") FROM \"%s\" where \"%s\" is not null", select_field,
                from_table, select_field));
        List<Object> result = new ArrayList<>();
        for (Tuple<?> tuple : cr.fetchall()) {
            result.add(tuple.get(0));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    Pair<String, List<Object>> __leaf_to_sql(ExtendedLeaf eleaf) {
        RecordSet model = eleaf.model;
        Tuple<Object> leaf = (Tuple<Object>) eleaf.leaf;
        Object left = leaf.get(0);
        String operator = (String) leaf.get(1);
        Object right = leaf.get(2);
        assert TERM_OPERATORS.contains(operator) || "inselect".equals(operator) || "not inselect".equals(operator)
                : String.format("Invalid operator %s in domain term %s", operator, leaf);
        assert TRUE_LEAF.equals(leaf) || FALSE_LEAF.equals(leaf) || model.hasField((String) left)
                : String.format("Invalid field %s in domain term %s", left, leaf);
        assert !(right instanceof RecordSet) : String.format("Invalid value %s in domain term %s", right, leaf);

        Field field = model.hasField((String) left) ? model.getField((String) left) : null;
        String table_alias = String.format("\"%s\"", eleaf.generate_alias());
        String query = null;
        List<Object> params = null;
        if (TRUE_LEAF.equals(leaf)) {
            query = "TRUE";
            params = Collections.emptyList();
        } else if (FALSE_LEAF.equals(leaf)) {
            query = "FALSE";
            params = Collections.emptyList();
        } else if ("inselect".equals(operator)) {
            Tuple<Object> tuple = (Tuple<Object>) right;
            query = String.format("(%s.\"%s\" in (%s))", table_alias, left, tuple.get(0));
            params = (List<Object>) tuple.get(1);
        } else if ("not inselect".equals(operator)) {
            Tuple<Object> tuple = (Tuple<Object>) right;
            query = String.format("(%s.\"%s\" not in (%s))", table_alias, left, tuple.get(0));
            params = (List<Object>) tuple.get(1);
        } else if ("in".equals(operator) || "not in".equals(operator)) {
            if (right instanceof Boolean) {
                _logger.warn("The domain term '{}' should use the '=' or '!=' operator.", leaf);
                if (("in".equals(operator) && right != null) || "not in".equals(operator) && right == null) {
                    query = String.format("(%s.\"%s\" IS NOT NULL)", table_alias, left);
                } else {
                    query = String.format("(%s.\"%s\" IS NULL)", table_alias, left);
                }
                params = Collections.emptyList();
            } else if (right instanceof List) {
                Collection<Object> col = (Collection<Object>) right;
                params = new ArrayList<>();
                for (Object o : col) {
                    if (o != null) {
                        params.add(o);
                    }
                }
                boolean check_nnull = params.size() < col.size();
                if (!params.isEmpty()) {
                    String instr;
                    if ("id".equals(left)) {
                        instr = String.join(",", Utils.mutli(Arrays.asList("%s"), params.size()));
                    } else {
                        instr = String.join(",", Utils.mutli(Arrays.asList(field.column_format()), params.size()));
                        for (int i = 0; i < params.size(); i++) {
                            params.set(i, field.convert_to_column(params.get(i), model, null, false));
                        }
                    }
                    query = String.format("(%s.\"%s\" %s (%s))", table_alias, left, operator, instr);

                } else {
                    query = "in".equals(operator) ? "FALSE" : "TRUE";
                }
                if ((check_nnull && "in".equals(operator)) || !check_nnull && "not in".equals(operator)) {
                    query = String.format("(%s OR %s.\"%s\" IS NULL)", query, table_alias, left);
                } else if (check_nnull && "not in".equals(operator)) {
                    query = String.format("(%s AND %s.\"%s\" IS NOT NULL)", query, table_alias, left);
                }
            } else {
                throw new ValueErrorException(String.format("Invalid domain term %s", leaf));
            }
        } else if (field != null && field instanceof BooleanField
                && ("=".equals(operator) && Boolean.FALSE.equals(right)
                        || "!=".equals(operator) && Boolean.TRUE.equals(right))) {
            query = String.format("(%s.\"%s\" IS NULL or %s.\"%s\" = false )", table_alias, left, table_alias, left);
            params = Collections.emptyList();
        } else if (right == null && "=".equals(operator)) {
            query = String.format("%s.\"%s\" IS NULL ", table_alias, left);
            params = Collections.emptyList();
        } else if (field != null && field instanceof BooleanField
                && ("!=".equals(operator) && Boolean.FALSE.equals(right)
                        || "==".equals(operator) && Boolean.TRUE.equals(right))) {
            query = String.format("(%s.\"%s\" IS NOT NULL or %s.\"%s\" != false )", table_alias, left, table_alias,
                    left);
            params = Collections.emptyList();
        } else if (right == null && "=".equals(operator)) {
            query = String.format("%s.\"%s\" IS NOT NULL ", table_alias, left);
            params = Collections.emptyList();
        } else if ("=?".equals(operator)) {
            if (right == null) {
                query = "TRUE";
                params = Collections.emptyList();
            } else {
                Pair<String, List<Object>> pair = __leaf_to_sql(
                        create_substitution_leaf(eleaf, new Tuple<>(left, '=', right), model, false));
                query = pair.first();
                params = pair.second();
            }
        } else {
            boolean need_wildcard = "like".equals(operator) || "ilike".equals(operator) || "not like".equals(operator)
                    || "not ilike".equals(operator);
            String sql_operator = operator;
            if ("=like".equals(operator)) {
                sql_operator = "like";
            }
            if ("=ilike".equals(operator)) {
                sql_operator = "ilike";
            }
            String cast = sql_operator.endsWith("like") ? "::text" : "";
            if (field == null) {
                throw new ValueErrorException(String.format("Invalid field %s in domain term %s", left, leaf));
            }
            String format = need_wildcard ? "%s" : field.column_format();
            String column = String.format("%s.%s", table_alias, _quote((String) left));
            query = String.format("(%s %s %s)", column + cast, sql_operator, format);
            if (need_wildcard && right == null || right != null && NEGATIVE_TERM_OPERATORS.contains(operator)) {
                query = String.format("(%s OR %s.\"%s\" IS NULL)", query, table_alias, left);
            }
            if (need_wildcard) {
                params = Arrays.asList("%" + right + "%");
            } else {
                params = Arrays.asList(field.convert_to_column(right, model, null, false));
            }
        }

        return new Pair<>(query, params);
    }

    static ExtendedLeaf create_substitution_leaf(ExtendedLeaf leaf, Object new_elements, @Default RecordSet new_model,
            @Default("false") boolean internal) {
        if (new_model == null) {
            new_model = leaf.model;
        }
        List<Tuple<Object>> new_join_context = new ArrayList<>();
        for (Tuple<Object> context : leaf.join_context) {
            new_join_context.add(Tuple.fromCollection(context));
        }
        return new ExtendedLeaf(new_elements, new_model, new_join_context, internal);
    }
}

class ExtendedLeaf {
    private static Logger _logger = LogManager.getLogger(ExtendedLeaf.class);
    Object leaf;
    List<Tuple<Object>> join_context;
    RecordSet model;
    List<RecordSet> _models;

    public ExtendedLeaf(Object leaf, RecordSet model, @Default List<Tuple<Object>> join_context,
            @Default("false") boolean internal) {
        this.leaf = leaf;
        this.join_context = join_context == null ? new ArrayList<>() : join_context;

        this.leaf = normalize_leaf(leaf);
        this.model = model;
        _models = new ArrayList<>();
        for (Tuple<Object> item : this.join_context) {
            _models.add((RecordSet) item.get(0));
        }
        _models.add(model);
        check_leaf(internal);
    }

    @Override
    public String toString() {
        return String.format("<osv.ExtendedLeaf: %s on %s (ctx: %s)>", leaf, model.table(), _get_context_debug());
    }

    public String generate_alias() {
        List<Pair<String, String>> links = new ArrayList<>();
        for (Tuple<Object> context : join_context) {
            links.add(new Pair<>(((RecordSet) context.get(1)).table(), (String) context.get(4)));
        }
        Pair<String, String> pair = Expression.generate_table_alias(_models.get(0).table(), links);
        return pair.first();
    }

    void add_join_context(RecordSet model, String lhs_col, String table_col, String link) {
        join_context.add(new Tuple<>(this.model, model, lhs_col, table_col, link));
        _models.add(model);
        this.model = model;
    }

    List<String> get_join_conditions() {
        List<String> conditions = new ArrayList<>();
        String alias = _models.get(0).table();
        for (Tuple<?> context : join_context) {
            String previous_alias = alias;
            alias += "__" + context.get(4);
            conditions.add(String.format("\"%s\".\"%s\"=\"%s\".\"%s\"", previous_alias, context.get(2), alias,
                    context.get(3)));
        }
        return conditions;
    }

    Set<String> get_tables() {
        Set<String> tables = new HashSet<>();
        List<Pair<String, String>> links = new ArrayList<>();
        for (Tuple<?> context : join_context) {
            links.add(new Pair<>(((RecordSet) context.get(1)).table(), (String) context.get(4)));
            Pair<String, String> pair = Expression.generate_table_alias(_models.get(0).table(), links);
            tables.add(pair.second());
        }
        return tables;
    }

    public Object _get_context_debug() {
        return null;
    }

    void check_leaf(boolean internal) {
        if (!is_operator() && !Expression.is_leaf(leaf, internal)) {
            throw new ValueErrorException(String.format("Invalid leaf %s", leaf));
        }
    }

    boolean is_true_leaf() {
        return Expression.TRUE_LEAF.equals(leaf);
    }

    boolean is_false_leaf() {
        return Expression.FALSE_LEAF.equals(leaf);
    }

    boolean is_leaf(@Default("false") boolean internal) {
        return Expression.is_leaf(leaf, internal);
    }

    boolean is_operator() {
        return leaf instanceof String && Expression.DOMAIN_OPERATORS.contains(leaf);
    }

    @SuppressWarnings("unchecked")
    Object normalize_leaf(Object element) {
        if (!Expression.is_leaf(element, false)) {
            return element;
        }
        List<Object> tuple = (List<Object>) element;
        Object left = tuple.get(0);
        String operator = (String) tuple.get(1);
        Object right = tuple.get(2);
        Object original = operator.toLowerCase();
        if (operator.equals("<>>")) {
            operator = "!=";
        }
        if (right instanceof Boolean && (operator.equals("in") || operator.equals("not in"))) {
            _logger.warn("The domain term '{}' should use the '=' or '!=' operator.",
                    new Tuple<>(left, original, right));
            operator = "in".equals(operator) ? "=" : "!=";
        }
        if ((right instanceof List) && (operator.equals("=") || operator.equals("!="))) {
            _logger.warn("The domain term '{}' should use the 'in' or 'not in' operator.",
                    new Tuple<>(left, original, right));
            operator = "=".equals(operator) ? "in" : "not in";
        }

        return new Tuple<>(left, operator, right);
    }
}
