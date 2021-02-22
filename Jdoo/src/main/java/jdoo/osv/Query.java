package jdoo.osv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import jdoo.util.Default;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.util.Utils;

public class Query {
    List<String> tables;
    List<String> where_clause;
    List<Object> where_clause_params;
    Map<String, List<Tuple<String>>> joins;
    Map<Tuple<Object>, Pair<Object, Object>> extras;

    public Query(@Nullable List<String> tables, @Nullable List<String> where_clause,
            @Nullable List<Object> where_clause_params) {
        this.tables = tables;
        this.where_clause = where_clause;
        this.where_clause_params = where_clause_params;
        joins = new HashMap<>();
    }

    public Query tables(List<String> tables) {
        this.tables = tables;
        return this;
    }

    public Query where_clause(List<String> where_clause) {
        this.where_clause = where_clause;
        return this;
    }

    public Query where_clause_params(List<Object> where_clause_params) {
        this.where_clause_params = where_clause_params;
        return this;
    }

    public Query joins(Map<String, List<Tuple<String>>> joins) {
        this.joins = joins;
        return this;
    }

    public Query extras(Map<Tuple<Object>, Pair<Object, Object>> extras) {
        this.extras = extras;
        return this;
    }

    List<String> _get_table_aliases() {
        List<String> result = new ArrayList<>();
        for (String from_statement : tables) {
            result.add(Expression.get_alias_from_query(from_statement).second());
        }
        return result;
    }

    Map<String, String> _get_alias_mapping() {
        Map<String, String> mapping = new HashMap<>();
        for (String table : tables) {
            mapping.put(Expression.get_alias_from_query(table).second(), table);
        }
        return mapping;
    }

    public Pair<String, String> add_join(Tuple<String> connection, @Default("true") boolean implicit,
            @Default("false") boolean outer, @Default String extra, @Default("[]") List<Object> extra_params) {
        String lhs = connection.get(0);
        String table = connection.get(1);
        String lhs_col = connection.get(2);
        String col = connection.get(3);
        String link = connection.get(4);
        Pair<String, String> result = Expression.generate_table_alias(lhs, Arrays.asList(new Pair<>(table, link)));
        String alias = result.first();
        String alias_statement = result.second();
        if (implicit) {
            if (!tables.contains(alias_statement)) {
                tables.add(alias_statement);
                String condition = String.format("\"%s\".\"%s\" = \"%s\".\"%s\"", lhs, lhs_col, alias, col);
                where_clause.add(condition);
            }
            return result;
        } else {
            List<String> aliases = _get_table_aliases();
            assert aliases.contains(lhs) : String
                    .format("Left-hand-side table %s must already be part of the query tables %s!", lhs, tables);
            if (!tables.contains(alias_statement)) {
                tables.add(alias_statement);
                Tuple<String> join_tuple = new Tuple<>(alias, lhs_col, col, outer ? "LEFT JOIN" : "JOIN");
                Utils.setdefault(joins, lhs, new ArrayList<Tuple<String>>()).add(join_tuple);
                if (StringUtils.hasText(extra) || !extra_params.isEmpty()) {
                    if (StringUtils.hasText(extra)) {
                        extra.replace("{lhs}", lhs).replace("{rhs}", alias);
                    } else {
                        extra = "";
                    }
                    extras.put(new Tuple<>(lhs, join_tuple), new Pair<>(extra, extra_params));
                }
            }
            return result;
        }
    }

    public Sql get_sql() {
        List<String> tables_to_process = new ArrayList<>(tables);
        Map<String, String> alias_mapping = _get_alias_mapping();
        List<String> from_clause = new ArrayList<>();
        List<Object> from_params = new ArrayList<>();

        for (int i = 0; i < tables_to_process.size(); i++) {
            if (i > 0) {
                from_clause.add(",");
            }
            String table = tables.get(i);
            from_clause.add(table);
            String table_alias = Expression.get_alias_from_query(table).second();
            if (joins.containsKey(table_alias)) {
                add_joins_for_table(table_alias, tables_to_process, alias_mapping, from_clause, from_params);
            }
        }

        from_params.addAll(where_clause_params);
        return new Sql(String.join("", from_clause.toArray(new String[from_clause.size()])),
                String.join(" AND ", where_clause.toArray(new String[where_clause.size()])), from_params);
    }

    void add_joins_for_table(String lhs, List<String> tables_to_process, Map<String, String> alias_mapping,
            List<String> from_clause, List<Object> from_params) {
        for (Tuple<String> tuple : joins.getOrDefault(lhs, Collections.emptyList())) {
            String rhs = tuple.get(0);
            String lhs_col = tuple.get(1);
            String rhs_col = tuple.get(2);
            String join = tuple.get(3);
            String alias = alias_mapping.get(rhs);
            tables_to_process.remove(alias);
            from_clause.add(
                    String.format(" %s %s ON (\"%s\".\"%s\" = \"%s\".\"%s\"", join, alias, lhs, lhs_col, rhs, rhs_col));
            Pair<Object, Object> extra = extras.get(new Tuple<>(lhs, new Tuple<>(rhs, lhs_col, rhs_col, join)));
            if (extra != null) {
                if (extra.first() != null) {
                    from_clause.add(" AND ");
                    from_clause.add((String) extra.first());
                }
                if (extra.second() != null) {
                    from_params.add(extra.second());
                }
            }
            from_clause.add(")");
            add_joins_for_table(rhs, tables_to_process, alias_mapping, from_clause, from_params);
        }
    }

    @Override
    public String toString() {
        Sql sql = get_sql();
        return String.format("osv.Query: \"SELECT ... FROM %s WHERE %s\" with params: %s", sql.from_clause,
                sql.where_clause, sql.params);
    }

    public class Sql {
        String from_clause;
        String where_clause;
        List<Object> params;

        public Sql(String from_clause, String where_clause, List<Object> params) {
            this.from_clause = from_clause;
            this.where_clause = where_clause;
            this.params = params;
        }

        public String from_clause() {
            return from_clause;
        }

        public String where_clause() {
            return where_clause;
        }

        public List<Object> params() {
            return params;
        }
    }
}
