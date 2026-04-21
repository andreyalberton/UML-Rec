package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single class (or interface, enum, abstract class).
 * This is the heart of the metamodel — it holds all the fields, methods,
 * and relationships that the AI extracts from the source code.
 * Mirrors: C = (name, access, type, {attr_i}, {met_j}, {uRel_k})
 */
public class C {
    private String name;
    private AccessModifier access;
    private ClassType type;
    private List<Attr> attrs;
    private List<Met> mets;
    private List<URel> uRels;

    // Stubbed for future OCL constraint support
    private List<String> constraints = new ArrayList<>();

    public C(String name, AccessModifier access, ClassType type) {
        this.name = name;
        this.access = access;
        this.type = type;
        this.attrs = new ArrayList<>();
        this.mets = new ArrayList<>();
        this.uRels = new ArrayList<>();
    }

    // OCL constraints — not used yet, but the field is here
    // so we don't have to touch the metamodel later
    public void addConstraint(String ocl) {
        this.constraints.add(ocl);
    }

    public List<String> getConstraints() { return constraints; }
    public String getName() { return name; }
    public AccessModifier getAccess() { return access; }
    public ClassType getType() { return type; }
    public List<Attr> getAttrs() { return attrs; }
    public List<Met> getMets() { return mets; }
    public List<URel> getURels() { return uRels; }
}