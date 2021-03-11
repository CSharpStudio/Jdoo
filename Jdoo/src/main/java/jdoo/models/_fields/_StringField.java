package jdoo.models._fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jdoo.apis.Cache;
import jdoo.models.RecordSet;
import jdoo.models.d;
import jdoo.tools.IdValues;
import jdoo.tools.Slot;
import jdoo.util.Kvalues;
import jdoo.util.Utils;

/** Abstract class for string fields. */
public abstract class _StringField<T extends _StringField<T>> extends BaseField<T> {
    /** whether the field is translated */
    public static final Slot translate = new Slot("translate", false);

    @Override
    public boolean _translate() {
        return Utils.bool(getattr(_StringField.translate));
    }

    public T translate(boolean translate) {
        setattr(_StringField.translate, translate);
        return (T) this;
    }

    public T translate(BiConsumer<Consumer<String>, String> translate) {
        setattr(_StringField.translate, translate);
        return (T) this;
    }

    public List<String> get_trans_terms(String value) {
        if (!(getattr(_StringField.translate) instanceof BiConsumer)) {
            return value != null ? Arrays.asList(value) : Collections.emptyList();
        }
        List<String> terms = new ArrayList<>();
        BiConsumer<Consumer<String>, String> trans = (BiConsumer<Consumer<String>, String>) getattr(
                _StringField.translate);
        trans.accept(terms::add, value);
        return terms;
    }

    BiFunction<Object, String, String> get_trans_func(RecordSet records) {
        if (getattr(_StringField.translate) instanceof BiConsumer) {
            // todo
        } else {
            // todo
        }
        return null;
    }

    @Override
    public RecordSet write(RecordSet records, Object value) {
        // discard recomputation of self on records
        records.env().remove_to_compute(this, records);

        // update the cache, and discard the records that are not modified
        Cache cache = records.env().cache();
        Object cache_value = convert_to_cache(value, records, true);
        records = cache.get_records_different_from(records, this, cache_value);
        if (!records.hasId()) {
            return records;
        }
        cache.update(records, this, Utils.mutli(Arrays.asList(cache_value), records.size()));

        if (!_store()) {
            return records;
        }

        RecordSet real_recs = records.filtered("id");
        if (!real_recs.hasId()) {
            return records;
        }
        boolean update_column = true;
        boolean update_trans = false;
        boolean single_lang = ((Collection<?>) records.env("res.lang").call("get_installed")).size() == 1;
        String lang = null;
        if (_translate()) {
            lang = Optional.of(records.env().lang()).orElse("en_US");
            if (single_lang) {
                // a single language is installed
                update_trans = true;
            } else if (getattr(_StringField.translate) instanceof BiConsumer || "en_US".equals(lang)) {
                update_column = true;
                update_trans = true;
            } else if (!"en_US".equals(lang)) {
                // update the translations only except if emptying
                update_column = cache_value == null;
                update_trans = true;
            }
        }

        // update towrite if modifying the source
        if (update_column) {
            IdValues towrite = records.env().all().towrite(model_name());
            for (Object rid : real_recs.ids()) {
                // cache_value is already in database format
                towrite.set(rid, getName(), cache_value);
            }
            if (Boolean.TRUE.equals(getattr(translate)) && cache_value != null) {
                String tname = "%s,%s".formatted(records.name(), getName());
                records.env("ir.translation").call("_set_source", tname, real_recs.ids(), value);
            }
        }

        if (update_trans) {
            if (getattr(_StringField.translate) instanceof BiConsumer) {
                // the source value of self has been updated, synchronize
                // translated terms when possible
                records.env("ir.translation").call("_sync_terms_translations", this, real_recs);
            } else {
                // update translations
                value = convert_to_column(value, records, null, false);
                RecordSet source_recs = real_recs.with_context(k -> k.set("lang", "en_US"));
                Object source_value = source_recs.stream().findFirst().get().get(this);
                if (!Utils.bool(source_value)) {
                    source_recs.set(this, value);
                    source_value = value;
                }
                String tname = "%s,%s".formatted(model_name(), getName());
                if (value == null) {
                    records.env("ir.translation").search(
                            d.on("name", "=", tname).on("type", "=", "model").on("res_id", "in", real_recs.ids()))
                            .unlink();
                } else if (single_lang) {
                    Object _src = source_value;
                    Object _value = value;
                    String _lang = lang;
                    records.env("ir.translation").call("_update_translations", real_recs.ids().stream()
                            .map(res_id -> new Kvalues().set("src", _src).set("value", _value).set("name", tname)
                                    .set("lang", _lang).set("type", "model").set("state", "translated")
                                    .set("res_id", res_id))
                            .collect(Collectors.toList()));
                } else {
                    records.env("ir.translation").call("_set_ids", tname, "model", lang, real_recs.ids(), value,
                            source_value);
                }
            }
        }
        return records;
    }
}
