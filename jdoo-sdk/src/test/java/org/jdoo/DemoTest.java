package org.jdoo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class DemoTest {
    @Test
    public void test() {
        String eval = "[[4, ref(base.user_admin)],[4, ref(base.efg)]]";
        Pattern r = Pattern.compile("ref\\((?<ref>\\S+)\\)");
        // Pattern r2 = Pattern.compile("\\((\\S+)\\)");
        Matcher m = r.matcher(eval);
        while (m.find()) {
            String ref = m.group();
            String refVal = m.group("ref");
            eval = eval.replace(ref, refVal);
            System.out.println(ref);
            // String key = r2.matcher(ref).group();
            // System.out.println(key);
        }

    }
}
