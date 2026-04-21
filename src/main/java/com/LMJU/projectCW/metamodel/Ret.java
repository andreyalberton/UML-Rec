package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * The return type of a method.
 * Has no name, just the type decomposition (cName, dName).
 * A void method just has empty lists here.
 */
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