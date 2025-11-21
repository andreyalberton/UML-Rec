package com.LMJU.projectCW.metamodel;

import java.util.List;

public class UMLRelationship {
    private String targetClassName;
    private List<RelationshipType> types;

    public UMLRelationship(String targetClassName, List<RelationshipType> types) {
        this.targetClassName = targetClassName;
        this.types = types;
    }

    public String getTargetClassName() {
        return targetClassName;
    }

    public List<RelationshipType> getTypes() {
        return types;
    }
}