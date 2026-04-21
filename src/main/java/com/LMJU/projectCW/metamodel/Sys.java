package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Root of the whole metamodel — represents one entire software system.
 * Everything else (packages, classes, relationships) lives under this.
 * Maps directly to the formal tuple: Sys = (name, {pack_i})
 */
public class Sys {
    private String name;
    private List<Pack> packs;

    public Sys(String name) {
        this.name = name;
        this.packs = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<Pack> getPacks() { return packs; }
}