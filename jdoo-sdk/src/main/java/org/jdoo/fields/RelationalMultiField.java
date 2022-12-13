package org.jdoo.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jdoo.Records;
import org.jdoo.core.MetaField;
import org.jdoo.exceptions.ValueException;
import org.jdoo.util.Tuple;
import org.jdoo.utils.ArrayUtils;

/**
 * 关联多个
 * 
 * @author lrz
 */
public class RelationalMultiField<T extends BaseField<T>> extends RelationalField<T> {

    public enum Command {
        /**
         * (0, 0, values) adds a new record created from the provided ``value`` dict.
         */
        ADD(0, "add"),
        /**
         * (1, id, values) updates an existing record of id ``id`` with the values in
         * ``values``. Can not be used in :meth:`~.create`.
         */
        UPDATE(1, "update"),
        /**
         * (2, id, 0)
         */
        DELETE(2, "delete"),
        /**
         * (3, id, 0)
         */
        REMOVE(3, "remove"),
        /**
         * (4, id, 0)
         */
        PUT(4, "put"),
        /**
         * (5, 0, 0)
         */
        CLEAR(5, "clear"),
        /**
         * (6, 0, ids)
         */
        REPLACE(6, "replace");

        int value;
        String name;

        Command(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public static Command get(Object valueOrName) {
            if (valueOrName instanceof Command) {
                return (Command) valueOrName;
            }
            if (valueOrName.equals(0) || valueOrName.equals("add")) {
                return ADD;
            } else if (valueOrName.equals(1) || valueOrName.equals("update")) {
                return UPDATE;
            } else if (valueOrName.equals(2) || valueOrName.equals("delete")) {
                return DELETE;
            } else if (valueOrName.equals(3) || valueOrName.equals("remove")) {
                return REMOVE;
            } else if (valueOrName.equals(4) || valueOrName.equals("put")) {
                return PUT;
            } else if (valueOrName.equals(5) || valueOrName.equals("clear")) {
                return CLEAR;
            } else if (valueOrName.equals(6) || valueOrName.equals("replace")) {
                return REPLACE;
            }
            throw new ValueException(String.format("值[%s]不是有效的Command", valueOrName));
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public RelationalMultiField() {
        prefetch = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convertToCache(Object value, Records rec, boolean validate) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof Records) {
            Records r = (Records) value;
            if (validate && !r.getMeta().getName().equals(getComodel())) {
                throw new ValueException(
                        String.format("模型[%s]属性[%s]的值[%s]无效", rec.getMeta().getName(), getName(), value));
            }
            return Arrays.asList(r.getIds());
        }
        if (value instanceof Collection) {
            Records comodel = rec.getEnv().get(getComodel());
            Set<String> ids = validate
                    ? new LinkedHashSet<String>(Arrays.asList(((Records) rec.get(getName())).getIds()))
                    : new LinkedHashSet<>();
            for (List<Object> command : (Collection<List<Object>>) value) {
                Command cmd = Command.get(command.get(0));
                if (cmd == Command.ADD) {
                    // TODO
                } else if (cmd == Command.UPDATE) {
                    Records line = comodel.browse((String) command.get(1));
                    if (validate) {
                        line.update((Map<String, Object>) command.get(2));
                    }
                    // todo update store
                } else if (cmd == Command.DELETE || cmd == Command.REMOVE) {
                    ids.remove(command.get(1));
                } else if (cmd == Command.PUT) {
                    ids.add((String) command.get(1));
                } else if (cmd == Command.CLEAR) {
                    ids.clear();
                } else if (cmd == Command.REPLACE) {
                    ids = new LinkedHashSet<>((List<String>) command.get(2));
                }
            }
            return new ArrayList<>(ids);
        }
        throw new ValueException(
                String.format("模型[%s]属性[%s]的值[%s]无效", rec.getMeta().getName(), getName(), value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convertToRecord(Object value, Records rec) {
        List<String> values = (List<String>) value;
        String[] ids = values == null ? ArrayUtils.EMPTY_STRING_ARRAY : values.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        Supplier<String[]> prefetchIds = () -> prefetchX2ManyIds(rec, this);
        return rec.getEnv().getRegistry().get(comodelName).browse(rec.getEnv(), ids, prefetchIds);
    }

    @SuppressWarnings("unchecked")
    String[] prefetchX2ManyIds(Records rec, MetaField field) {
        Supplier<String[]> prefetchIds = rec.getPrefetchIds();
        String[] p = prefetchIds != null ? prefetchIds.get() : ArrayUtils.EMPTY_STRING_ARRAY;
        Records records = rec.browse(p);
        Set<String> ids = new HashSet<>();
        for (Object v : rec.getEnv().getCache().getValues(records, field)) {
            if (v != null) {
                for (String id : (List<String>) v) {
                    ids.add(id);
                }
            }
        }
        return ids.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public Object convertToRead(Object value, Records rec, boolean usePresent) {
        if (value instanceof Records) {
            return ((Records) value).getIds();
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convertToWrite(Object value, Records rec) {
        if (value == null) {
            return Arrays.asList(Arrays.asList(Command.CLEAR, null, null));
        }
        if (value instanceof Records) {
            Records v = (Records) value;
            List<Object> result = new ArrayList<>();
            result.add(Arrays.asList(Command.REPLACE, 0, Arrays.asList(v.getIds())));
            return result;
        }
        if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            if (list.size() > 0) {
                if (list.get(0) instanceof String) {
                    value = rec.getEnv().get(getComodel(), list.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
                }
            }
            return value;
        }
        throw new ValueException(String.format("模型[%s]属性[%s]的值[%s]无效", rec.getMeta().getName(), getName(), value));
    }

    @Override
    public Object convertToExport(Object value, Records rec) {
        if (value instanceof Records) {
            Records v = (Records) value;
            return v.getPresent().stream().map(r -> (String) r[1]).collect(Collectors.joining(","));
        }
        return value;
    }

    @Override
    public Object convertToPresent(Object value, Records rec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create(List<Tuple<Records, Object>> recordValues) {
        save(recordValues, true);
    }

    @Override
    public Records write(Records records, Object value) {
        return save(Arrays.asList(new Tuple<>(records, value)), false);
    }

    protected Records save(List<Tuple<Records, Object>> list, boolean create) {
        for (int i = 0; i < list.size(); i++) {
            Tuple<Records, Object> t = list.get(i);
            Records rs = t.getItem1();
            Object value = t.getItem2();
            if (value == null) {
                list.set(i, new Tuple<>(rs, Arrays.asList(Command.CLEAR, 0, 0)));
            } else if (value instanceof Records) {
                Records r = (Records) value;
                if (r.getMeta().getName().equals(getComodel())) {
                    list.set(i, new Tuple<>(rs, Arrays.asList(Command.REPLACE, 0, r.getIds())));
                }
            }
        }
        return doSave(list, create);
    }

    protected Records doSave(List<Tuple<Records, Object>> list, boolean create) {
        throw new UnsupportedOperationException();
    }
}
