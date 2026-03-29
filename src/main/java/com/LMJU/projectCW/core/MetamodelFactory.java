package com.LMJU.projectCW.core;

import com.LMJU.projectCW.metamodel.*;
import java.util.Arrays;
import java.util.List;

public class MetamodelFactory {

    // 1 Create System
    public static Sys createSys(String name) {
        return new Sys(name);
    }

    // 2 Create Package and link to Parent System
    public static Pack createPack(String name, Sys parentSys) {
        Pack p = new Pack(name);
        if (parentSys != null) {
            parentSys.getPacks().add(p);
        }
        return p;
    }

    // 2-b Create sub-package and link to parent package
    public static Pack createSubPack(String name, Pack parentPack) {
        Pack p = new Pack(name);
        if (parentPack != null) {
            parentPack.getPacks().add(p);
        }
        return p;
    }

    // 3 Create class and link to package
    public static C createClass(String name, AccessModifier access, ClassType type, Pack parentPack) {
        C newClass = new C(name, access, type);
        if (parentPack != null) {
            parentPack.getClasses().add(newClass);
        }
        return newClass;
    }

    // 4 Create Attribute and link to Class
    public static Attr createAttr(String name, AccessModifier access, String typeName, C parentClass) {
        Attr a = new Attr(name, access);
        if (typeName != null && !typeName.isEmpty()) {
            a.getCName().add(typeName);
        }
        parentClass.getAttrs().add(a);
        return a;
    }

    // 5 Create method and link to class
    public static Met createMet(String name, AccessModifier access, C parentClass) {
        Met m = new Met(name, access);
        parentClass.getMets().add(m);
        return m;
    }

    // 6 Create Relationship
    public static void createRelationship(C source, String targetClassName, RelationshipType type) {
        URel rel = new URel(targetClassName, Arrays.asList(type));
        source.getURels().add(rel);
    }
}