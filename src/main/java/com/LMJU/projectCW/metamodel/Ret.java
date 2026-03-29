package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class Ret {
    private List<String> cName;
    private List<String> dName;

    public Ret() {
        this.cName = new ArrayList<>();
        this.dName = new ArrayList<>();
    }

    public List<String> getCName() { return cName; }
    public List<String> getDName() { return dName; }
}