package jdoo.models;

public abstract class StringField<T extends StringField<T>> extends BaseField<T> {
    Boolean translate;

    @SuppressWarnings("unchecked")
    public T translate(boolean translate) {
        this.translate = translate;
        return (T) this;
    }
}
