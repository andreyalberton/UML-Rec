package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class Sys {
    private String name;
    private List<Pack> packs; // {pack_i}

    public Sys(String name) {
        this.name = name;
        this.packs = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<Pack> getPacks() { return packs; }
}