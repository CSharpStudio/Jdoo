package org.jdoo.utils;

import org.junit.jupiter.api.Test;

public class SnowflakeIdTest {
    @Test
    public void textStringId() {
        System.out.println(Long.MAX_VALUE);
        System.out.println(Long.toString(Long.MAX_VALUE, 36));
        for (int i = 0; i < 10; i++) {
            long id = SnowFlakeUtil.getSnowflakeId();
            String str = Long.toString(id, 36);
            System.out.println(id);
            System.out.println(StringUtils.leftPad(str, 13, '0'));
        }
    }
}
