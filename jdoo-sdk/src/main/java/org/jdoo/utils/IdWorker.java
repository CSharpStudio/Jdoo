package org.jdoo.utils;

/**
 * id生成器
 * 
 * @author lrz
 */
public class IdWorker {
    /** todo 参数从网卡地址获取 */
    static SnowFlakeUtil snowFlakeUtil = new SnowFlakeUtil(1, 1);

    public static String nextId() {
        long id = snowFlakeUtil.nextId();
        String str = Long.toString(id, 36);
        return StringUtils.leftPad(str, 13, '0');
    }
}
