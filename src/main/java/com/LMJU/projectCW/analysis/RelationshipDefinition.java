package com.LMJU.projectCW.analysis;

/**
 * Simple data holder for a relationship between two classes.
 *
 * This class was designed as an intermediate representation
 * to hold relationships found during analysis, before they were
 * mapped into the metamodel's URel objects. But now, the
 * ProjectAnalyzer and SourceCodeParser ended up creating URel objects
 * directly, so this class is no longer used in the pipeline.
 * It has been kept in the codebase as it could serve as a useful
 * abstraction if the analysis layer is ever decoupled from the
 * metamodel layer in a future refactor.
 */
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

    public String getFromClass() { return fromClass; }
    public String getToClass() { return toClass; }
    public Type getType() { return type; }

    @Override
    public String toString() {
        return fromClass + " " + type + " " + toClass;
    }
}