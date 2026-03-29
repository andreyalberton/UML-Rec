package com.LMJU.projectCW.analysis;

public class RelationshipDefinition {

    public enum Type {
        INHERITANCE,
        IMPLEMENTATION,
        ASSOCIATION,    
        COMPOSITION,
        DEPENDENCY
    }

    private String fromClass;
    private String toClass;
    private Type type;

    public RelationshipDefinition(String fromClass, String toClass, Type type) {
        this.fromClass = fromClass;
        this.toClass = toClass;
        this.type = type;
    }
    // hey

    // Getters
    public String getFromClass() { return fromClass; }
    public String getToClass() { return toClass; }
    public Type getType() { return type; }

    @Override
    public String toString() {
        return fromClass + " " + type + " " + toClass;
    }
}