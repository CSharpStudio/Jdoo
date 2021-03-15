package jdoo.models._fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jdoo.apis.Cache;
import jdoo.exceptions.ValueErrorException;
import jdoo.models.Field;
import jdoo.models.NewId;
import jdoo.models.RecordSet;
import jdoo.util.OrderedSet;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.util.Utils;

/** Abstract class for relational fields *2many. */
public abstract class _RelationalMultiField<T extends _RelationalMultiField<T>> extends _RelationalField<T> {
    // todos

    /**
     * Update the cached value of ``self`` for ``records`` with ``value``, and
     * return whether everything is in cache.
     */
    public boolean _update(RecordSet records, Object value) {
        // todo
        boolean result = true;
        if (Utils.bool(value)) {
            Cache cache = records.env().cache();
            for (RecordSet record : records) {
                if (cache.contains(record, this)) {
                    Object val = convert_to_cache(Optional.of(record.get(getName())).orElse(value), record, false);
                    cache.set(record, this, val);
                } else {
                    result = false;
                }
            }
        }
        return result;
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        // cache format: tuple(ids)
        if (value instanceof RecordSet) {
            RecordSet rec = (RecordSet) value;
            if (validate && rec.name() != _comodel_name()) {
                throw new ValueErrorException("Wrong value for %s: %s".formatted(this, value));
            }
            if (record.hasId() && !Utils.bool(record.id())) {
                // x2many field value of new record is new records
                List<Object> ids = new ArrayList<>();
                for (Object it : rec.ids()) {
                    ids.add(new NewId(it, null));
                }
                return ids;
            }
            return rec.ids();
        } else if (value instanceof List) {
            // value is a list/tuple of commands, dicts or record ids
            RecordSet comodel = record.env(_comodel_name());
            Function<Object, RecordSet> browse;
            // if record is new, the field's value is new records
            if (record.hasId() && !Utils.bool(record.id())) {
                browse = it -> comodel.browse(new NewId(it, null));
            } else {
                browse = comodel::browse;
            }
            // determine the value ids
            Set<Object> ids = new HashSet<>(
                    validate ? ((RecordSet) record.get(getName())).ids() : Collections.emptyList());
            // modify ids with the commands
            for (Object command : (Collection<?>) value) {
                if (command instanceof List) {
                    List<Object> list = (List<Object>) command;
                    if (list.get(0).equals(0)) {
                        // (0, _, values)adds a new record created from the provided ``value`` dict.
                        ids.add(comodel.$new((Map<String, Object>) list.get(2), null, list.get(1)).id());
                    } else if (list.get(0).equals(1)) {
                        // (1, id, values)updates an existing record of id ``id`` with the values in
                        // ``values``. Can not be used in :meth:`~.create`.
                        RecordSet line = browse.apply(list.get(1));
                        if (validate) {
                            line.update((Map<String, Object>) list.get(2));
                        } else {
                            line.call("_update_cache", list.get(2), false);
                        }
                        ids.add(line.id());
                    } else if (list.get(0).equals(2) || list.get(0).equals(3)) {
                        // (2, id, _) removes the record of id ``id`` from the set, then deletes it
                        // (from the database). Can not be used in :meth:`~.create`.
                        // (3, id, _)removes the record of id ``id`` from the set, but does not delete
                        // it. Can not be used on :class:`~odoo.fields.One2many`. Can not be used in
                        // :meth:`~.create`.
                        ids.remove(browse.apply(list.get(1)).id());
                    } else if (list.get(0).equals(4)) {
                        // (4, id, _)adds an existing record of id ``id`` to the set. Can not be used on
                        // :class:`~odoo.fields.One2many`.
                        ids.add(browse.apply(list.get(1)).id());
                    } else if (list.get(0).equals(5)) {
                        // (5, _, _)removes all records from the set, equivalent to using the command
                        // ``3`` on every record explicitly. Can not be used on
                        // :class:`~odoo.fields.One2many`. Can not be used in :meth:`~.create`.
                        ids.clear();
                    } else if (list.get(0).equals(6)) {
                        // (6, _, ids)replaces all existing records in the set by the ``ids`` list,
                        // equivalent to using the command ``5`` followed by a command ``4`` for each
                        // ``id`` in ``ids``.
                        ids = new HashSet<>(((Collection<Object>) list.get(2)).stream().map(it -> browse.apply(it).id())
                                .collect(Collectors.toList()));
                    }
                } else if (command instanceof Map) {
                    ids.add(comodel.$new((Map<String, Object>) command).id());
                } else {
                    ids.add(browse.apply(command).id());
                }
            }
            // return result as a tuple
            return Tuple.fromCollection(ids);
        } else if (!Utils.bool(value)) {
            return Collections.emptyList();
        }
        throw new ValueErrorException("Wrong value for %s: %s".formatted(this, value));
    }

    @Override
    public Object convert_to_record(Object value, RecordSet record) {
        // use registry to avoid creating a recordset for the model
        Collection<Object> prefetch_ids = prefetch_x2many_ids(record, this);
        Collection<Object> ids = value instanceof Collection ? (Collection<Object>) value : new Tuple<>(value);
        RecordSet corecords = record.pool(_comodel_name()).browse(record.env(), ids, prefetch_ids);
        if (corecords.hasField("active") && (Boolean) record.env().context().getOrDefault("active_test", true)) {
            corecords = corecords.filtered("active").with_prefetch(prefetch_ids);
        }
        return corecords;
    }

    @Override
    public Object convert_to_read(Object value, RecordSet record) {
        return ((RecordSet) value).ids();
    }

    @Override
    public Object convert_to_write(Object value, RecordSet record) {
        if (value == null || Boolean.FALSE.equals(value)) {
            return Arrays.asList(new Tuple<>(5));
        }

        if (value instanceof Tuple) {
            // a tuple of ids, this is the cache format
            value = record.env(_comodel_name()).browse(value);
        }
        if (value instanceof RecordSet && ((RecordSet) value).name() == _comodel_name()) {
            // make result with new and existing records
            Map<String, Field> inv_names = record.type().field_inverses().get(this).stream()
                    .collect(Collectors.toMap(field -> field.getName(), field -> field));
            List<Tuple<Object>> result = Utils.asList(new Tuple<>(6, 0, new ArrayList<Object>()));
            for (RecordSet rec : (RecordSet) value) {
                RecordSet origin = rec.origin();
                if (!origin.hasId()) {
                    Object values = rec._convert_to_write(
                            rec._cache().keySet().stream().filter(name -> !inv_names.containsKey(name))
                                    .collect(Collectors.toMap(name -> name, name -> rec.get(name))));
                    result.add(new Tuple<>(0, 0, values));
                } else {
                    ((List<Object>) result.get(0).get(2)).add(origin.id());
                    if (!rec.equals(origin)) {
                        Object values = rec._convert_to_write(rec._cache().keySet().stream().filter(
                                name -> !inv_names.containsKey(name) && !rec.get(name).equals(origin.get(name)))
                                .collect(Collectors.toMap(name -> name, name -> rec.get(name))));
                        if (Utils.bool(values)) {
                            result.add(new Tuple<>(1, origin.id(), values));
                        }
                    }
                }
            }
            return result;
        }
        if (value instanceof List) {
            return value;
        }
        throw new ValueErrorException("Wrong value for %s: %s".formatted(this, value));
    }

    @Override
    public Object convert_to_export(Object value, RecordSet record) {
        if (value == null || !(value instanceof RecordSet)) {
            return "";
        }
        return String.join(",",
                ((RecordSet) value).name_get().stream().map(p -> p.second()).collect(Collectors.toList()));
    }

    @Override
    public String convert_to_display_name(Object value, RecordSet record) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void _setup_regular_full(RecordSet model) {
        super._setup_regular_full(model);
        Object d = getattr(domain);
        if (d instanceof List && ((List<?>) d).size() > 0) {
            Collection<String> _depends = _depends();
            List<String> depends = new ArrayList<>();
            if (_depends != null) {
                depends.addAll(_depends);
            }
            for (Object arg : (List<Object>) d) {
                if (arg instanceof List) {
                    Object a = ((List<Object>) arg).get(0);
                    if (a instanceof String) {
                        depends.add(getName() + "." + a);
                    }
                }
            }
            depends(depends);
        }
    }

    @Override
    public void create(List<Pair<RecordSet, Object>> record_values) {
        // TODO Auto-generated method stub
        super.create(record_values);
    }

    @Override
    public RecordSet write(RecordSet records, Object value) {
        // TODO Auto-generated method stub
        return super.write(records, value);
    }

    RecordSet write_batch() {
        // todo
        return null;
    }

    Collection<Object> prefetch_x2many_ids(RecordSet record, Field field) {
        RecordSet records = record.browse(record.prefetch_ids());
        Collection<Object> ids_list = record.env().cache().get_values(records, field);
        Set<Object> unique = new HashSet<>();
        for (Object o : ids_list) {
            if (o instanceof Collection) {
                unique.addAll((Collection<Object>) o);
            } else {
                unique.add(o);
            }
        }
        return unique;
    }
}
