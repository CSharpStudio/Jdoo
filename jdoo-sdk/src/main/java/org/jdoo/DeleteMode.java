package org.jdoo;

/**
 * 删除模式
 * 
 * @author lrz
 */
public enum DeleteMode {
    /** 设置为空 */
    SetNull("SET NULL"),
    /** 级联删除 */
    Cascade("CASCADE"),
    /** 限制删除 */
    Restrict("RESTRICT");

    private String name;

    DeleteMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
