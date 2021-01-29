package jdoo.data;

public class PgVarchar {
    Integer size;

    public PgVarchar() {
    }

    public PgVarchar(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        if (size == null) {
            return "varchar";
        }
        return String.format("varchar(%s)", size);
    }
}
