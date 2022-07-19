package org.jdoo.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lrz
 */
public class ThrowableUtils {
    /**
     * 获取根本原因:首次引发异常的原因
     * 
     * @param throwable
     * @return
     */
    public static Throwable getCause(Throwable throwable) {
        Throwable t = throwable;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    /**
     * 获取调试信息
     * 
     * @param error
     * @return
     */
    public static String getDebug(Throwable error) {
        List<String> message = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Throwable t = error;
        while (t != null) {
            String msg = t.getMessage();
            if (StringUtils.isNotEmpty(msg)) {
                String at = t.getStackTrace()[0].toString();
                message.add(String.format("%s:%s\r\n\tat %s", t.getClass().getName(), msg, at));
            } else {
                errors.add(t.getClass().getName() + ":" + t.getStackTrace()[0].toString());
            }
            t = t.getCause();
        }
        if (message.size() > 0) {
            Collections.reverse(message);
            return StringUtils.join(message, "\r\nCause:");
        }
        return StringUtils.join(errors, "\r\nat:");
    }
}
