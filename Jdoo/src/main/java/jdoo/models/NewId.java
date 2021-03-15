package jdoo.models;

public class NewId {
    Object origin;
    Object ref;

    public NewId(Object origin, Object ref) {
        this.origin = origin;
        this.ref = ref;
    }

    public Object origin() {
        return origin;
    }

    public Object ref() {
        return ref;
    }
}
