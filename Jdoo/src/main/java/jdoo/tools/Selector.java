package jdoo.tools;

import java.util.ArrayList;
import jdoo.util.Pair;

public class Selector extends ArrayList<Pair<String, String>> {
    public Selector add(String key, String value) {
        super.add(new Pair<>(key, value));
        return this;
    }
}
