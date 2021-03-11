package jdoo.tools;

public class Slot {
    private String name;
    private Object value;

    public Slot(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public Object value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Slot) {
            Slot other = (Slot) obj;
            return name.equals(other.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}