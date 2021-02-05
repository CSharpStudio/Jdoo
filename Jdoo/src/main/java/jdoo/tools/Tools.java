package jdoo.tools;

import java.util.Collection;

import jdoo.util.Tuple;

public class Tools {
    public static String human_size(Object sz) {
        if (sz == null) {
            return "";
        }
        Tuple<String> units = new Tuple<>("bytes", "Kb", "Mb", "Gb", "Tb");
        int i = 0;
        double s = 0;
        if (sz instanceof String) {
            s = ((String) sz).length();
        } else {
            s = (double) sz;
        }
        while (s >= 1024 && i < units.size() - 1) {
            s /= 1024;
            i++;
        }
        return String.format("%0.2f %s", s, units.get(i));
    }

    public static boolean hasId(Object id) {
        if (id instanceof String) {
            return true;
        }
        return false;
    }

    public static boolean hasValue(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Collection<?>) {
            return ((Collection<?>) obj).size() > 0;
        } else if (obj instanceof String) {
            return !((String) obj).isEmpty();
        }
        return true;
    }
}
