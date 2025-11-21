package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class UMLMethod {
    private String name;
    private AccessModifier access;
    private List<UMLParameter> parameters;
    private List<UMLLocalVariable> localVariables;
    private UMLReturnType returnType;

    public UMLMethod(String name, AccessModifier access) {
        this.name = name;
        this.access = access;
        this.parameters = new ArrayList<>();
        this.localVariables = new ArrayList<>();
        this.returnType = new UMLReturnType();
    }

    public String getName() { return name; }
    public AccessModifier getAccess() { return access; }
    public List<UMLParameter> getParameters() { return parameters; }
    public List<UMLLocalVariable> getLocalVariables() { return localVariables; }
    public UMLReturnType getReturnType() { return returnType; }
    public void setReturnType(UMLReturnType returnType) { this.returnType = returnType; }
}