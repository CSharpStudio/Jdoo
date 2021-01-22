package jdoo.models;

import java.util.ArrayList;

public class Domain {
    
    ArrayList<Object> list = new ArrayList<Object>();

    public Domain on(String property, String op, Object value){
        list.add(new Criteria(property, op, value));
        return this;
    }
    
    public Domain and(){
        list.add(new Group("&"));
        return this;
    }
    
    public Domain or(){
        list.add(new Group("|"));
        return this;
    }
}

class Criteria {
    String property;
    String op;
    Object value;

    public Criteria(String property, String op, Object value) {
        this.property = property;
        this.op = op;
        this.value = value;
    }
}

class Group {
    String group;
    public Group(String group){
        this.group = group;
    }
}