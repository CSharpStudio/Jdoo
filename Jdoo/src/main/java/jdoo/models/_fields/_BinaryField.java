package jdoo.models._fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jdoo.apis.Cache;
import jdoo.apis.Environment;
import jdoo.models.Domain;
import jdoo.models.RecordSet;
import jdoo.models.d;
import jdoo.tools.Slot;
import jdoo.tools.Tools;
import jdoo.util.Dict;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.util.Utils;

public class _BinaryField<T extends _BinaryField<T>> extends BaseField<T> {
    /** whether value is stored in attachment */
    public static final Slot attachment = new Slot("attachment");
    static {
        default_slots.put(attachment, true);
    }

    public _BinaryField() {
        set(prefetch, false);// not prefetched by default
        set(depends_context, new Tuple<>("bin_size"));// depends on context (content or size)
    }

    @SuppressWarnings("unchecked")
    public T attachment(boolean attachment) {
        set(BinaryField.attachment, attachment);
        return (T) this;
    }

    @Override
    public Pair<String, Object> column_type() {
        return get(Boolean.class, attachment) ? null : new Pair<>("bytea", "bytea");
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Dict values, boolean validate) {
        if (value == null) {
            return null;
        }

        if (value instanceof byte[]) {
            return value;
        }
        return value.toString().getBytes();
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        if (value instanceof String) {
            return ((String) value).getBytes();
        }
        if (value instanceof Integer && (record.context().containsKey("bin_size")
                || record.context().containsKey("bin_size_" + getName()))) {
            return Tools.human_size(value);
        }
        return value;
    }

    @Override
    public void read(RecordSet records) {

        Domain domain = d.on("res_model", "=", records.type().name()).on("res_field", "=", getName()).on("res_id", "in",
                records.ids());
        records.env("ir.attachment").sudo().call("search", domain);
        // todo
    }

    @Override
    public void create(List<Pair<RecordSet, Object>> record_values) {
        if (record_values.isEmpty()) {
            return;
        }
        Environment env = record_values.get(0).first().env();
        // try(env.norecompute()){
        List<Map<String, Object>> args = new ArrayList<>();
        for (Pair<RecordSet, Object> pair : record_values) {
            args.add(new Dict().set("name", getName())//
                    .set("res_model", model_name())//
                    .set("res_field", getName())//
                    .set("res_id", pair.first().id())//
                    .set("type", "binary")//
                    .set("datas", pair.second()));
        }
        env.get("ir.attachment").sudo().with_context(ctx -> ctx.set("binary_field_real_user", env.user()))
                .call("create", args);
        // }
    }

    @Override
    public RecordSet write(RecordSet records, Object value) {
        if (!get(Boolean.class, attachment)) {
            return super.write(records, value);
        }
        records.env().remove_to_compute(this, records);
        Cache cache = records.env().cache();
        Object cache_value = convert_to_cache(value, records, true);
        records = cache.get_records_different_from(records, this, cache_value);
        if (!records.hasId()) {
            return records;
        }
        RecordSet not_null = null;
        if (store()) {
            not_null = cache.get_records_different_from(records, this, null);
        }

        cache.update(records, this, Utils.mutli(Arrays.asList(cache_value), records.size()));

        if (store()) {
            // todo
            // RecordSet atts = records.env("ir.attachment").sudo();
            if (not_null != null && not_null.hasId()) {

            }
            if (value != null) {

            } else {

            }
        }

        return records;
    }
}
