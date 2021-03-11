package jdoo.models._fields;

import jdoo.models.RecordSet;
import jdoo.tools.Mail;
import jdoo.tools.Slot;
import jdoo.tools.Translate;
import jdoo.util.Kvalues;
import jdoo.util.Pair;

//todo
public class HtmlField extends _StringField<HtmlField> {
    public HtmlField() {
        column_type = new Pair<>("text", "text");
    }

    static final Slot sanitize = new Slot("sanitize", true);
    static final Slot sanitize_tags = new Slot("sanitize_tags", true);
    static final Slot sanitize_attributes = new Slot("sanitize_attributes", true);
    static final Slot sanitize_style = new Slot("sanitize_style", false);
    static final Slot strip_style = new Slot("strip_style", false);
    static final Slot strip_classes = new Slot("strip_classes", false);

    /** whether value must be sanitized */
    public HtmlField sanitize(boolean sanitize) {
        setattr(HtmlField.sanitize, sanitize);
        return this;
    }

    /** whether to sanitize tags (only a white list of attributes is accepted) */
    public HtmlField sanitize_tags(boolean sanitize_tags) {
        setattr(HtmlField.sanitize_tags, sanitize_tags);
        return this;
    }

    /**
     * whether to sanitize attributes (only a white list of attributes is accepted)
     */
    public HtmlField sanitize_attributes(boolean sanitize_attributes) {
        setattr(HtmlField.sanitize_attributes, sanitize_attributes);
        return this;
    }

    /** whether to sanitize style attributes */
    public HtmlField sanitize_style(boolean sanitize_style) {
        setattr(HtmlField.sanitize_style, sanitize_style);
        return this;
    }

    /** whether to strip style attributes (removed and therefore not sanitized) */
    public HtmlField strip_style(boolean strip_style) {
        setattr(HtmlField.strip_style, strip_style);
        return this;
    }

    /** whether to strip classes attributes */
    public HtmlField strip_classes(boolean strip_classes) {
        setattr(HtmlField.strip_classes, strip_classes);
        return this;
    }

    @Override
    public void _setup_attrs(RecordSet model, String name) {
        super._setup_attrs(model, name);
        // Translated sanitized html fields must use html_translate or a callable.
        if (Boolean.TRUE.equals(getattr(translate)) && Boolean.TRUE.equals(getattr(sanitize))) {
            translate((callback, value) -> Translate.html_translate(callback, value));
        }
    }

    boolean b(Slot key) {
        return getattr(Boolean.class, key);
    }

    @Override
    public Object convert_to_column(Object value, RecordSet record, Kvalues values, boolean validate) {
        if (value == null || Boolean.FALSE.equals(value)) {
            return null;
        }
        if (b(sanitize)) {
            return Mail.html_sanitize(value, true, b(sanitize_tags), b(sanitize_attributes), b(sanitize_style),
                    b(strip_style), b(strip_classes));
        }
        return value;
    }

    @Override
    public Object convert_to_cache(Object value, RecordSet record, boolean validate) {
        if (value == null || Boolean.FALSE.equals(value)) {
            return null;
        }
        if (validate && b(sanitize)) {
            return Mail.html_sanitize(value, true, b(sanitize_tags), b(sanitize_attributes), b(sanitize_style),
                    b(strip_style), b(strip_classes));
        }
        return value;
    }
}
