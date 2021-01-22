package jdoo.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Cursor {
    static int IN_MAX = 1000;// decent limit on size of IN queries - guideline = Oracle limit

    public List<Object[]> split_for_in_conditions(Collection<?> set) {
        return split_for_in_conditions(set, IN_MAX);
    }

    public List<Object[]> split_for_in_conditions(Collection<?> set, int size) {
        List<Object[]> result = new ArrayList<Object[]>();
        Object[] original = set.toArray();
        if (size > 0) {
            int left = set.size();
            int from = 0;
            int to;
            while (left > 0) {
                if (left > size) {
                    to = from + size;
                } else {
                    to = from + left;
                }
                Object[] batch = Arrays.copyOfRange(original, from, to);
                result.add(batch);
                left -= size;
                from += size;
            }
        } else {
            result.add(original);
        }
        return result;
    }

    int rowcount;

    public int rowcount() {
        return rowcount;
    }

    public void execute(String query) {
        execute(query, null, false);
    }

    public void execute(String query, boolean log_exceptions) {
        execute(query, null, log_exceptions);
    }

    public void execute(String query, List<Object> params) {
        execute(query, params, false);
    }

    public void execute(String query, List<Object> params, boolean log_exceptions) {
        System.out.println(query);
    }

}
