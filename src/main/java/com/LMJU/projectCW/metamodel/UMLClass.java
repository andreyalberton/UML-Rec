package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class UMLClass {
    private String name;
    private AccessModifier access;
    private ClassType type;
    private List<UMLAttribute> attributes;
    private List<UMLMethod> methods;
    private List<UMLRelationship> relationships;

    public UMLClass(String name, AccessModifier access, ClassType type) {
        this.name = name;
        this.access = access;
        this.type = type;
        this.attributes = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.relationships = new ArrayList<>();
    }

    public String getName() { return name; }
    public AccessModifier getAccess() { return access; }
    public ClassType getType() { return type; }
    public List<UMLAttribute> getAttributes() { return attributes; }
    public List<UMLMethod> getMethods() { return methods; }
    public List<UMLRelationship> getRelationships() { return relationships; }
}