package jdoo.tools;

public class Slot {
    private String _name;

    public Slot(String name) {
        this._name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Slot) {
            Slot other = (Slot) obj;
            return _name.equals(other._name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }

    @Override
    public String toString() {
        return _name;
    }
}