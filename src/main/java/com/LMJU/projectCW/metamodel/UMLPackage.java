package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class UMLPackage {
    private String name;
    private List<UMLPackage> subPackages;
    private List<UMLClass> classes;

    public UMLPackage(String name) {
        this.name = name;
        this.subPackages = new ArrayList<>();
        this.classes = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<UMLPackage> getSubPackages() { return subPackages; }
    public List<UMLClass> getClasses() { return classes; }
}