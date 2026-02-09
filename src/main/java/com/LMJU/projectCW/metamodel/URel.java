package com.LMJU.projectCW.metamodel;

import java.util.List;

public class URel {
    private String cName; // Target class name
    private List<RelationshipType> type;

    public String getTarget() {
        return this.cName; //
    }

    public URel(String cName, List<RelationshipType> type) {
        this.cName = cName;
        this.type = type;
    }

    public String getCName() { return cName; }
    public List<RelationshipType> getType() { return type; }
}