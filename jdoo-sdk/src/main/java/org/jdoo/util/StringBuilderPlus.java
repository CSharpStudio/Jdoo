package org.jdoo.util;

public class StringBuilderPlus {
    StringBuilder sb = new StringBuilder();

    public StringBuilderPlus append(Object obj) {
        sb.append(obj);
        return this;
    }

    public StringBuilderPlus appendLine(Object obj) {
        append(obj);
        return appendLine();
    }

    public StringBuilderPlus appendLine() {
        sb.append("\r\n");
        return this;
    }

    public StringBuilderPlus append(String str, Object... args) {
        if (str != null) {
            if (args.length > 0) {
                sb.append(String.format(str, args));
            } else {
                sb.append(str);
            }
        }
        return this;
    }

    public StringBuilderPlus appendLine(String str, Object... args) {
        append(str, args);
        return appendLine();
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
