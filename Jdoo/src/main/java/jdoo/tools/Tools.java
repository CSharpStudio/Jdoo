package jdoo.tools;

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
}
