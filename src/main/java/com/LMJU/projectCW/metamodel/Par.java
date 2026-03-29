package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class Par {
    private String name;
    private List<String> cName;
    private List<String> dName;

    public Par(String name) {
        this.name = name;
        this.cName = new ArrayList<>();
        this.dName = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<String> getCName() { return cName; }
    public List<String> getDName() { return dName; }
}