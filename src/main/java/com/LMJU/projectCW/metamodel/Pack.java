package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class Pack {
    private String name;
    private List<Pack> packs; // Sub-packages {pack_i}
    private List<C> classes;  // {c_j}

    public Pack(String name) {
        this.name = name;
        this.packs = new ArrayList<>();
        this.classes = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<Pack> getPacks() { return packs; }
    public List<C> getClasses() { return classes; }
}