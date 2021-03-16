package jdoo.models._fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jdoo.apis.Cache;
import jdoo.apis.Environment;
import jdoo.models.Domain;
import jdoo.models.RecordSet;
import jdoo.models.d;
import jdoo.tools.Slot;
import jdoo.tools.Tools;
import jdoo.util.Kvalues;
import jdoo.util.Kwargs;
import jdoo.util.Pair;
import jdoo.util.Tuple;
import jdoo.util.Utils;

public class _BinaryField<T extends _BinaryField<T>> extends BaseField<T> {
    /** whether value is stored in attachment */
    public static final Slot attachment = new Slot("attachment", true);

    public _BinaryField() {
        setattr(Slots.prefetch, false);// not prefetched by default
        setattr(Slots.depends_context, new Tuple<>("bin_size"));// depends on context (content or size)
    }

    public T attachment(boolean attachment) {
        setattr(BinaryField.attachment, attachment);
        return (T) this;
    }

    public boolean _attachment() {
        return getattr(Boolean.class, BinaryField.attachment);
    }

    @Override
    public Pair<String, Object> column_type() {
        return getattr(Boolean.class, attachment) ? null : new Pair<>("bytea", "bytea");
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Kvalues values, boolean validate) {
        // Binary values may be byte strings (python 2.6 byte array), but
        // the legacy OpenERP convention is to transfer and store binaries
        // as base64-encoded strings. The base64 string may be provided as a
        // unicode in some circumstances, hence the str() cast here.
        // This str() coercion will only work for pure ASCII unicode strings,
        // on purpose - non base64 data must be passed as a 8bit byte strings.
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return Base64.getDecoder().decode((String) value);
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
            // If the client requests only the size of the field, we return that
            // instead of the content. Presumably a separate request will be done
            // to read the actual content, if necessary.
            return Tools.human_size(value);
        }
        return Boolean.FALSE.equals(value) ? null : value;
    }

    @Override
    public Object convert_to_record(Object value, RecordSet record) {
        return value == null ? false : value;
    }

    @Override
    public void read(RecordSet records) {
        // values are stored in attachments, retrieve them
        assert _attachment();
        Domain domain = d.on("res_model", "=", records.type().name())//
                .on("res_field", "=", getName())//
                .on("res_id", "in", records.ids());
        // Note: the "bin_size" flag is handled by the field "datas" itself
        Map<Object, Object> data = records.env("ir.attachment").sudo().search(domain).stream()
                .collect(Collectors.toMap(att -> att.get("res_id"), att -> att.get("datas")));
        Cache cache = records.env().cache();
        for (RecordSet record : records) {
            cache.set(record, this, data.getOrDefault(record.id(), false));
        }
    }

    @Override
    public void create(List<Pair<RecordSet, Object>> record_values) {
        assert _attachment();
        if (record_values.isEmpty()) {
            return;
        }
        // create the attachments that store the values
        Environment env = record_values.get(0).first().env();
        List<Map<String, Object>> args = new ArrayList<>();
        for (Pair<RecordSet, Object> pair : record_values) {
            args.add(new Kvalues(k -> k.set("name", getName())//
                    .set("res_model", model_name())//
                    .set("res_field", getName())//
                    .set("res_id", pair.first().id())//
                    .set("type", "binary")//
                    .set("datas", pair.second())));
        }
        env.get("ir.attachment").sudo().with_context(ctx -> ctx.set("binary_field_real_user", env.user()))
                .call("create", args);
    }

    @Override
    public Object write(RecordSet records, Object value) {
        if (!_attachment()) {
            return super.write(records, value);
        }
        // discard recomputation of self on records
        records.env().remove_to_compute(this, records);
        Cache cache = records.env().cache();
        Object cache_value = convert_to_cache(value, records, true);
        records = cache.get_records_different_from(records, this, cache_value);
        if (!records.hasId()) {
            return records;
        }
        RecordSet not_null = null;
        if (_store()) {
            // determine records that are known to be not null
            not_null = cache.get_records_different_from(records, this, null);
        }

        cache.update(records, this, Utils.mutli(Arrays.asList(cache_value), records.size()));

        // retrieve the attachments that store the values, and adapt them
        if (_store()) {
            // todo
            RecordSet atts = records.env("ir.attachment").sudo();
            if (not_null != null && not_null.hasId()) {
                atts = atts.search(d.on("res_model", "=", model_name())//
                        .on("res_field", "=", getName())//
                        .on("res_id", "in", records.ids()));
            }
            if (Utils.bool(value)) {
                // update the existing attachments
                atts.write(new Kvalues().set("datas", value));
                RecordSet atts_records = records.browse(atts.mapped("res_id"));
                // create the missing attachments
                RecordSet missing = records.subtract(atts_records).filtered("id");
                if (missing.hasId()) {
                    atts.create(missing.stream().map(record -> new Kwargs()//
                            .set("name", getName())//
                            .set("res_model", record.name())//
                            .set("res_field", getName())//
                            .set("res_id", record.name())//
                            .set("type", "binary")//
                            .set("datas", value)).collect(Collectors.toList()));
                }
            } else {
                atts.unlink();
            }
        }
        return records;
    }
}
