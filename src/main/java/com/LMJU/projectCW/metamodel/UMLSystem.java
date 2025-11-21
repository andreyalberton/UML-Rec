package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class UMLSystem {
    private String name;
    private List<UMLPackage> packages;

    public UMLSystem(String name) {
        this.name = name;
        this.packages = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<UMLPackage> getPackages() { return packages; }
}