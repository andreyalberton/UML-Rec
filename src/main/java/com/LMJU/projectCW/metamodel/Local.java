package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * A local variable declared inside a method body.
 * Same shape as Par — just cName and dName for the type info.
 */
public class Local {
    private String name;
    private List<String> cName;
    private List<String> dName;

    public Local(String name) {
        this.name = name;
        this.cName = new ArrayList<>();
        this.dName = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<String> getCName() { return cName; }
    public List<String> getDName() { return dName; }
}