package com.tfjybj.framework.json;

import java.util.List;

public class ClassDemo {
    String NAME = "jxf";
    List items = null;
    int age = 2;
    String testNull = null;
    String testEmpty = "";

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public List getItems() {
        return items;
    }

    public void setItems(List items) {
        this.items = items;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getTestNull() {
        return testNull;
    }

    public void setTestNull(String testNull) {
        this.testNull = testNull;
    }

    public String getTestEmpty() {
        return testEmpty;
    }

    public void setTestEmpty(String testEmpty) {
        this.testEmpty = testEmpty;
    }
}
