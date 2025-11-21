package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class UMLAttribute {
    private String name;
    private AccessModifier access;
    private List<String> typeClassNames;
    private List<String> dataStructureNames;
    private List<String> instantiatedClassNames;

    public UMLAttribute(String name, AccessModifier access) {
        this.name = name;
        this.access = access;
        this.typeClassNames = new ArrayList<>();
        this.dataStructureNames = new ArrayList<>();
        this.instantiatedClassNames = new ArrayList<>();
    }

    public String getName() { return name; }
    public AccessModifier getAccess() { return access; }
    public List<String> getTypeClassNames() { return typeClassNames; }
    public List<String> getDataStructureNames() { return dataStructureNames; }
    public List<String> getInstantiatedClassNames() { return instantiatedClassNames; }
}