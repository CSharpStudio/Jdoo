package jdoo.models;

import jdoo.tools.Tuple2;

public class HtmlField extends StringField<HtmlField> {
    public HtmlField(){
        column_type = new Tuple2<String, Object>("text", "text");
    }

    Boolean sanitize;
    Boolean sanitize_tags;
    Boolean sanitize_attributes;
    Boolean sanitize_style;
    Boolean strip_style;
    Boolean strip_classes;

    public HtmlField sanitize(boolean sanitize){
        this.sanitize=sanitize;
        return this;
    }
    public HtmlField sanitize_tags(boolean sanitize_tags){
        this.sanitize_tags=sanitize_tags;
        return this;
    }
    public HtmlField sanitize_attributes(boolean sanitize_attributes){
        this.sanitize_attributes=sanitize_attributes;
        return this;
    }
    public HtmlField sanitize_style(boolean sanitize_style){
        this.sanitize_style=sanitize_style;
        return this;
    }
    public HtmlField strip_style(boolean strip_style){
        this.strip_style=strip_style;
        return this;
    }
    public HtmlField strip_classes(boolean strip_classes){
        this.strip_classes=strip_classes;
        return this;
    }
}
