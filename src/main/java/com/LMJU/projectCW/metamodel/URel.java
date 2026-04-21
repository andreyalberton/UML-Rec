package com.LMJU.projectCW.metamodel;

import java.util.List;

/**
 * A UML relationship from the owning class to another class.
 * cName is the target class name, and type holds what kind of
 * relationship it is (inheritance, association, etc.).
 * The type is a list because technically two classes can be
 * linked by more than one relationship type at the same time.
 */
public class URel {
    private String cName;  // who we're pointing at
    private List<RelationshipType> type;

    // convenience alias — same as getCName(), just reads nicer
    public String getTarget() {
        return this.cName;
    }

    public URel(String cName, List<RelationshipType> type) {
        this.cName = cName;
        this.type = type;
    }

    public String getCName() { return cName; }
    public List<RelationshipType> getType() { return type; }
}