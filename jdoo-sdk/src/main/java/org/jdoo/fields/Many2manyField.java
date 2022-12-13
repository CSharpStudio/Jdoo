package org.jdoo.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.jdoo.Criteria;
import org.jdoo.DeleteMode;
import org.jdoo.Records;
import org.jdoo.core.BaseModel;
import org.jdoo.core.Constants;
import org.jdoo.data.Cursor;
import org.jdoo.data.DbColumn;
import org.jdoo.data.SqlDialect;
import org.jdoo.data.Query.SqlClause;
import org.jdoo.util.Cache;
import org.jdoo.util.Tuple;
import org.jdoo.utils.ArrayUtils;
import org.jdoo.data.Query;

/**
 * 多对多
 * 
 * @author lrz
 */
public class Many2manyField extends RelationalMultiField<Many2manyField> {
    @JsonIgnore
    String relation;
    @JsonIgnore
    String column1;
    @JsonIgnore
    String column2;
    @JsonIgnore
    Boolean autoJoin;
    @JsonIgnore
    Integer limit;
    @JsonIgnore
    DeleteMode ondelete;

    public Many2manyField() {
        type = Constants.MANY2MANY;
    }

    public Many2manyField(String comodel, String relation, String column1, String column2) {
        this();
        args.put("comodelName", comodel);
        args.put("relation", relation);
        args.put("column1", column1);
        args.put("column2", column2);
    }

    public String getRelation() {
        return relation;
    }

    public String getColumn1() {
        return column1;
    }

    public String getColumn2() {
        return column2;
    }

    @Override
    protected void updateDb(Records model, Map<String, DbColumn> columns) {
        Cursor cr = model.getEnv().getCursor();
        SqlDialect sd = cr.getSqlDialect();
        Records comodel = model.getEnv().get(getComodel());
        if (!sd.tableExists(cr, relation)) {
            sd.createM2MTable(cr, relation, column1, column2, String.format("RELATION BETWEEN %s AND %s",
                    model.getMeta().getTable(), comodel.getMeta().getTable()));
        }
    }

    @Override
    public void read(Records records) {
        Map<String, Object> context = new HashMap<>(16);
        context.put("active_test", false);
        context.putAll(getContext());
        Records comodel = records.getEnv().get(getComodel()).withContext(context);
        Criteria criteria = getCriteria(records);
        comodel.call("flushSearch", criteria, Collections.emptyList(), "");
        Query wquery = BaseModel.whereCalc(comodel, criteria, true);
        String orderBy = BaseModel.generateOrderBy(comodel, null, wquery);
        SqlClause q = wquery.getSql();
        Cursor cr = records.getEnv().getCursor();
        String rel = cr.quote(relation);
        String id1 = cr.quote(column1);
        String id2 = cr.quote(column2);
        String where = q.getWhere();
        if (StringUtils.isEmpty(where)) {
            where = "1=1";
        }
        String sql = "SELECT " + rel + "." + id1 + ", " + rel + "." + id2
                + " FROM " + rel + ", " + q.getFrom() + " WHERE " + where + " AND " + rel + "." + id1
                + " IN %s AND " + rel + "." + id2 + "=" + cr.quote(comodel.getMeta().getTable()) + ".id ORDER BY"
                + orderBy;
        sql = cr.getSqlDialect().getPaging(sql, limit, 0);
        List<Object> params = q.getParams();
        params.add(Arrays.asList(records.getIds()));
        cr.execute(sql, params);
        Map<String, List<String>> group = new HashMap<>();
        for (Object[] row : cr.fetchAll()) {
            List<String> list = group.get(row[0]);
            if (list == null) {
                list = new ArrayList<>();
                group.put((String) row[0], list);
            }
            list.add((String) row[1]);
        }

        Cache cache = records.getEnv().getCache();
        for (Records r : records) {
            cache.set(r, this, group.get(r.getId()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Records doSave(List<Tuple<Records, Object>> records_commands_list, boolean create) {
        if (records_commands_list.size() == 0) {
            return null;
        }
        Records model = records_commands_list.get(0).getItem1().browse();
        Records comodel = model.getEnv().get(getComodel()).withContext(getContext());
        Set<String> ids = new HashSet<>();
        records_commands_list.stream().forEach(t -> ids.addAll(Arrays.asList(t.getItem1().getIds())));
        Records records = model.browse(ids);

        if (isStore()) {
            Collection<String> missing_ids = records.getEnv().getCache().getMissingIds(records, this);
            if (missing_ids.size() > 0) {
                read(records.browse(missing_ids));
            }
        }

        Map<String, Set<String>> oldRelation = new HashMap<>();
        Map<String, Set<String>> newRelation = new HashMap<>();
        for (Records record : records) {
            List<String> rids = Arrays.asList(((Records) record.get(getName())).getIds());
            oldRelation.put(record.getId(), new HashSet<>(rids));
            newRelation.put(record.getId(), new HashSet<>(rids));
        }

        BiConsumer<String[], String> relationAdd = (xs, y) -> {
            for (String x : xs) {
                Set<String> ys = newRelation.get(x);
                if (ys == null) {
                    ys = new HashSet<>();
                    newRelation.put(x, ys);
                }
                ys.add(y);
            }
        };

        BiConsumer<String[], String> relationRemove = (xs, y) -> {
            for (String x : xs) {
                Set<String> ys = newRelation.get(x);
                if (ys != null) {
                    ys.remove(y);
                }
            }
        };

        BiConsumer<String[], List<String>> relationSet = (xs, ys) -> {
            for (String x : xs) {
                newRelation.put(x, new HashSet<>(ys));
            }
        };

        Consumer<List<String>> relationDelete = (ys) -> {
            for (Set<String> ys1 : oldRelation.values()) {
                ys1.removeAll(ys);
            }
            for (Set<String> ys1 : newRelation.values()) {
                ys1.removeAll(ys);
            }
        };

        for (Tuple<Records, Object> tuple : records_commands_list) {
            List<Tuple<String[], Map<String, Object>>> toCreate = new ArrayList<>();
            List<String> toDelete = new ArrayList<>();
            List<Object> commands = (List<Object>) tuple.getItem2();
            Records recs = tuple.getItem1();
            for (Object cmds : commands) {
                if (!(cmds instanceof List)) {
                    continue;
                }
                List<Object> command = (List<Object>) cmds;
                Command cmd = Command.get(command.get(0));
                if (cmd == Command.ADD) {
                    toCreate.add(new Tuple<>(recs.getIds(), (Map<String, Object>) command.get(2)));
                } else if (cmd == Command.UPDATE) {
                    comodel.browse((String) command.get(1)).update((Map<String, Object>) command.get(2));
                } else if (cmd == Command.DELETE) {
                    toDelete.add((String) command.get(1));
                } else if (cmd == Command.REMOVE) {
                    relationRemove.accept(recs.getIds(), (String) command.get(1));
                } else if (cmd == Command.PUT) {
                    relationAdd.accept(recs.getIds(), (String) command.get(1));
                } else if (cmd == Command.CLEAR || cmd == Command.REPLACE) {
                    toCreate = toCreate.stream().map(c -> {
                        Set<String> set = new HashSet<>(Arrays.asList(c.getItem1()));
                        set.removeAll(Arrays.asList(recs.getIds()));
                        return new Tuple<>(set.toArray(ArrayUtils.EMPTY_STRING_ARRAY), c.getItem2());
                    }).collect(Collectors.toList());
                    relationSet.accept(recs.getIds(),
                            cmd == Command.REPLACE ? (List<String>) command.get(2) : Collections.emptyList());
                }
            }

            if (toCreate.size() > 0) {
                Records lines = comodel
                        .createBatch(toCreate.stream().map(t -> t.getItem2()).collect(Collectors.toList()));
                for (List<Object> line : ArrayUtils.zip(lines, toCreate)) {
                    relationAdd.accept(((Tuple<String[], Map<String, Object>>) line.get(1)).getItem1(),
                            ((Records) line.get(0)).getId());
                }
            }

            if (toDelete.size() > 0) {
                comodel.browse(toDelete).delete();
                relationDelete.accept(toDelete);
            }
        }

        // update the cache of self
        Cache cache = records.getEnv().getCache();
        for (Records record : records) {
            cache.set(record, this, newRelation.get(record.getId()));
        }

        // process pairs to add (beware of duplicates)
        List<List<String>> pairs = new ArrayList<>();
        for (Entry<String, Set<String>> e : newRelation.entrySet()) {
            Set<String> set = new HashSet<>(e.getValue());
            set.removeAll(oldRelation.get(e.getKey()));
            for (String id : set) {
                pairs.add(Arrays.asList(e.getKey(), id));
            }
        }

        Cursor cr = records.getEnv().getCursor();
        if (pairs.size() > 0) {
            String sql = String.format("INSERT INTO %s(%s,%s) VALUES (%%s,%%s)", cr.quote(relation), cr.quote(column1),
                    cr.quote(column2));
            for (List<String> params : pairs) {
                cr.execute(sql, params);
            }

            // TODO update the cache of inverse fields
        }

        // process pairs to remove
        pairs = new ArrayList<>();
        for (Entry<String, Set<String>> e : oldRelation.entrySet()) {
            Set<String> set = new HashSet<>(e.getValue());
            set.removeAll(newRelation.get(e.getKey()));
            for (String id : set) {
                pairs.add(Arrays.asList(e.getKey(), id));
            }
        }
        if (pairs.size() > 0) {
            Map<String, Set<String>> yToXs = new HashMap<>();
            for (List<String> p : pairs) {
                Set<String> xs = yToXs.get(p.get(1));
                if (xs == null) {
                    xs = new HashSet<>();
                    yToXs.put(p.get(1), xs);
                }
                xs.add(p.get(0));
            }

            if (isStore()) {
                Map<Set<String>, Set<String>> xsToYs = new HashMap<>();
                for (Entry<String, Set<String>> e : yToXs.entrySet()) {
                    Set<String> ys = xsToYs.get(e.getValue());
                    if (ys == null) {
                        ys = new HashSet<>();
                        xsToYs.put(e.getValue(), ys);
                    }
                    ys.add(e.getKey());
                }

                String condition = String.format("%s IN %%s AND %s IN %%s", cr.quote(column1), cr.quote(column2));
                String where = xsToYs.entrySet().stream().map(p -> condition).collect(Collectors.joining(" OR "));
                String sql = String.format("DELETE FROM %s WHERE %s", cr.quote(relation), where);
                List<Object> params = new ArrayList<>();
                for (Entry<Set<String>, Set<String>> e : xsToYs.entrySet()) {
                    params.add(Arrays.asList(e.getKey().toArray()));
                    params.add(Arrays.asList(e.getValue().toArray()));
                }
                cr.execute(sql, params);
            }

            // TODO update the cache of inverse fields
        }
        return records.filter(record -> !newRelation.get(record.getId()).equals(oldRelation.get(record.getId())));
    }
}
