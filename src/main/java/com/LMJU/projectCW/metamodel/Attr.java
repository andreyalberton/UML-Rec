package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * A class field / attribute.
 * The type info is split into three lists following the spec:
 *   cName  — user-defined class names referenced in the type (e.g. "Student")
 *   dName  — data structure names (e.g. "List", "Map")
 *   inName — instantiation expressions (e.g. "new ArrayList()")
 */
public class Attr {
    private String name;
    private AccessModifier access;
    private List<String> cName;
    private List<String> dName;
    private List<String> inName;

    public Attr(String name, AccessModifier access) {
        this.name = name;
        this.access = access;
        this.cName = new ArrayList<>();
        this.dName = new ArrayList<>();
        this.inName = new ArrayList<>();
    }

    public String getName() { return name; }
    public AccessModifier getAccess() { return access; }
    public List<String> getCName() { return cName; }
    public List<String> getDName() { return dName; }
    public List<String> getInName() { return inName; }
}