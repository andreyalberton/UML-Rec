package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class UMLLocalVariable {
    private String name;
    private List<String> typeClassNames;
    private List<String> dataStructureNames;

    public UMLLocalVariable(String name) {
        this.name = name;
        this.typeClassNames = new ArrayList<>();
        this.dataStructureNames = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<String> getTypeClassNames() { return typeClassNames; }
    public List<String> getDataStructureNames() { return dataStructureNames; }
}