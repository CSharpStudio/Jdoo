package org.jdoo.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.jdoo.Criteria;
import org.jdoo.DeleteMode;
import org.jdoo.Records;
import org.jdoo.core.Constants;
import org.jdoo.core.MetaField;
import org.jdoo.data.DbColumn;
import org.jdoo.util.Cache;
import org.jdoo.util.Tuple;

/**
 * 一对多
 * 
 * @author lrz
 */
public class One2manyField extends RelationalMultiField<One2manyField> {
    String inverseName;
    @JsonIgnore
    Integer limit = 1000;

    public One2manyField() {
        type = Constants.ONE2MANY;
    }

    public One2manyField(String comodel, String inverseName) {
        this();
        args.put("comodelName", comodel);
        args.put("inverseName", inverseName);
    }

    public One2manyField limit(Integer limit) {
        args.put("limit", limit);
        return this;
    }

    public String getInverseName() {
        return inverseName;
    }

    public Integer getLimit() {
        return limit;
    }

    @Override
    protected void updateDb(Records model, Map<String, DbColumn> columns) {
        // TODO check comodel
    }

    @Override
    public void read(Records records) {
        Map<String, Object> context = new HashMap<>(16);
        context.put("active_test", false);
        if (this.context != null) {
            context.putAll(this.context);
        }
        Records comodel = records.getEnv().get(getComodel()).withContext(context);
        MetaField inverseField = comodel.getMeta().getField(inverseName);
        Criteria criteria = getCriteria(records).and(Criteria.in(inverseName, Arrays.asList(records.getIds())));
        Records lines = comodel.find(criteria, 0, limit, null);
        Map<Object, List<String>> group = new HashMap<>(records.size());
        for (Records line : lines.withContext(Constants.PREFETCH_FIELDS, false)) {
            Object lineId = inverseField.getType() == "many2one" ? ((Records) line.get(inverseName)).getId()
                    : line.get(inverseName);
            List<String> ids = group.get(lineId);
            if (ids == null) {
                ids = new ArrayList<>();
                group.put(lineId, ids);
            }
            ids.add(line.getId());
        }
        Cache cache = records.getEnv().getCache();
        for (Records record : records) {
            cache.set(record, this, group.getOrDefault(record.getId(), Collections.emptyList()));
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
            boolean allowFullDelete = !create;
            List<Map<String, Object>> toCreate = new ArrayList<>();
            List<String> toDelete = new ArrayList<>();
            Map<Records, Set<String>> toInverse = new HashMap<>();
            String inverse = getInverseName();
            for (Tuple<Records, Object> t : records_commands_list) {
                Records rs = t.getItem1();
                List<List<Object>> commands = (List<List<Object>>) t.getItem2();
                for (List<Object> command : commands) {
                    Command cmd = Command.get(command.get(0));
                    if (cmd == Command.ADD) {
                        for (Records r : rs) {
                            Map<String, Object> values = new HashMap<>((Map<String, Object>) command.get(2));
                            values.put(inverse, r.getId());
                            toCreate.add(values);
                        }
                        allowFullDelete = false;
                    } else if (cmd == Command.UPDATE) {
                        comodel.browse((String) command.get(1)).update((Map<String, Object>) command.get(2));
                    } else if (cmd == Command.DELETE) {
                        toDelete.add((String) command.get(1));
                    } else if (cmd == Command.REMOVE) {
                        unlink(comodel.browse((String) command.get(1)), toDelete, inverse);
                    } else if (cmd == Command.PUT) {
                        Records r = rs.browse(rs.getIds()[0]);
                        Set<String> lines = toInverse.get(r);
                        if (lines == null) {
                            lines = new HashSet<>();
                            toInverse.put(r, lines);
                        }
                        lines.add((String) command.get(1));
                        allowFullDelete = false;
                    } else if (cmd == Command.CLEAR || cmd == Command.REPLACE) {
                        List<String> lineIds = cmd == Command.REPLACE ? (List<String>) command.get(2)
                                : Collections.emptyList();
                        if (!allowFullDelete && lineIds.size() == 0) {
                            continue;
                        }
                        flush(rs, toCreate, toDelete, toInverse, inverse);
                        Records lines = comodel.browse(lineIds);
                        Criteria criteria = Criteria.in(inverse, Arrays.asList(rs.getIds()))
                                .and(Criteria.notIn("id", Arrays.asList(lines.getIds())));
                        unlink(comodel.find(criteria, 0, 0, null), toDelete, inverse);
                        // assign the given lines to the last record only
                        lines.set(inverse, rs.getIds()[rs.size() - 1]);
                    }
                }
            }
            flush(comodel, toCreate, toDelete, toInverse, inverse);
        } else {
            // TODO
        }
        return records;
    }

    void unlink(Records lines, List<String> toDelete, String inverse) {
        Many2oneField m2o = (Many2oneField) lines.getMeta().getField(inverse);
        if (m2o.getOnDelete() == DeleteMode.Cascade) {
            toDelete.addAll(Arrays.asList(lines.getIds()));
        } else {
            lines.set(inverse, null);
        }
    }

    void flush(Records comodel, List<Map<String, Object>> toCreate, List<String> toDelete,
            Map<Records, Set<String>> toInverse, String inverse) {
        if (toDelete.size() > 0) {
            comodel.browse(toDelete).delete();
            toDelete.clear();
        }
        if (toCreate.size() > 0) {
            comodel.createBatch(toCreate);
            toCreate.clear();
        }
        if (toInverse.size() > 0) {
            for (Entry<Records, Set<String>> e : toInverse.entrySet()) {
                Records r = e.getKey();
                Records lines = comodel.browse(e.getValue());
                lines = lines.filter(line -> !r.getId().equals(line.get(inverse)));
                lines.set(inverse, r);
            }
        }
    }
}