package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class UMLReturnType {
    private List<String> typeClassNames;
    private List<String> dataStructureNames;

    public UMLReturnType() {
        this.typeClassNames = new ArrayList<>();
        this.dataStructureNames = new ArrayList<>();
    }

    public List<String> getTypeClassNames() { return typeClassNames; }
    public List<String> getDataStructureNames() { return dataStructureNames; }
}