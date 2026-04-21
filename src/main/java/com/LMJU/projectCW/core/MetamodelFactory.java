package com.LMJU.projectCW.core;

import com.LMJU.projectCW.metamodel.*;
import java.util.Arrays;
import java.util.List;

/**
 * Class used to build metamodel objects.
 *
 * Every create method does two things: build the object AND link it
 * to its parent automatically. This way the containment hierarchy
 * is always consistent — you can't accidentally create an orphan
 * class floating around with no package.
 */
public class MetamodelFactory {

    // Create a new system
    public static Sys createSys(String name) {
        return new Sys(name);
    }

    // Create a package and wire it into the system
    public static Pack createPack(String name, Sys parentSys) {
        Pack p = new Pack(name);
        if (parentSys != null) {
            parentSys.getPacks().add(p);
        }
        return p;
    }

    // Create a sub-package inside another package
    public static Pack createSubPack(String name, Pack parentPack) {
        Pack p = new Pack(name);
        if (parentPack != null) {
            parentPack.getPacks().add(p);
        }
        return p;
    }

    // Create a class and put it into a package
    public static C createClass(String name, AccessModifier access, ClassType type, Pack parentPack) {
        C newClass = new C(name, access, type);
        if (parentPack != null) {
            parentPack.getClasses().add(newClass);
        }
        return newClass;
    }

    // Create an attribute (field) and link it to a class
    public static Attr createAttr(String name, AccessModifier access, String typeName, C parentClass) {
        Attr a = new Attr(name, access);
        if (typeName != null && !typeName.isEmpty()) {
            a.getCName().add(typeName);
        }
        parentClass.getAttrs().add(a);
        return a;
    }

    // Create a method and attach it to a class
    public static Met createMet(String name, AccessModifier access, C parentClass) {
        Met m = new Met(name, access);
        parentClass.getMets().add(m);
        return m;
    }

    // Create a relationship between two classes (source -> target)
    public static void createRelationship(C source, String targetClassName, RelationshipType type) {
        URel rel = new URel(targetClassName, Arrays.asList(type));
        source.getURels().add(rel);
    }
}