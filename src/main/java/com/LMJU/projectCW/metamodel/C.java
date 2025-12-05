package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class C {
    private String name;
    private AccessModifier access;
    private ClassType type;
    private List<Attr> attrs; // {attr_i}
    private List<Met> mets;   // {met_j}
    private List<URel> uRels; // {rel_k} mapped to uRel

    public C(String name, AccessModifier access, ClassType type) {
        this.name = name;
        this.access = access;
        this.type = type;
        this.attrs = new ArrayList<>();
        this.mets = new ArrayList<>();
        this.uRels = new ArrayList<>();
    }

    public String getName() { return name; }
    public AccessModifier getAccess() { return access; }
    public ClassType getType() { return type; }
    public List<Attr> getAttrs() { return attrs; }
    public List<Met> getMets() { return mets; }
    public List<URel> getURels() { return uRels; }
}