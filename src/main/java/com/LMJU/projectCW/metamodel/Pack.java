package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * A Java package. Can hold classes and nested sub-packages.
 * Mirrors: Pack = (name, {pack_i}, {c_j})
 * The recursive packs list lets us represent something like com.foo.bar
 * as three nested Pack objects if we want that level of detail.
 */
public class Pack {
    private String name;
    private List<Pack> packs;   // sub-packages
    private List<C> classes;    // the actual classes living in this package

    public Pack(String name) {
        this.name = name;
        this.packs = new ArrayList<>();
        this.classes = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<Pack> getPacks() { return packs; }
    public List<C> getClasses() { return classes; }
}