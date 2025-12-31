package com.LMJU.projectCW.analysis;

import com.LMJU.projectCW.core.MetamodelFactory;
import com.LMJU.projectCW.metamodel.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class SourceCodeParser {

    public void parseAndAddToSystem(String jsonString, Sys system) {
        try {
            /*

            Print what the AI actually sent
            System.out.println("DEBUG: AI JSON -> " + jsonString);

            if (jsonString == null || !jsonString.trim().startsWith("{")) {
                System.err.println("ignoring invalid JSON output.");
                return;
            }
                */
            JSONObject root = new JSONObject(jsonString);

            String fullPath = root.optString("className", "UnknownClass");
            String accessStr = root.optString("access", "PUBLIC").toUpperCase();
            String typeStr = root.optString("type", "CLASS").toUpperCase();

            //  Handle package hierarchy
            Pack parentPackage = getOrCreatePackageHierarchy(fullPath, system);

            // Create the Class Object
            C newClass = MetamodelFactory.createClass(
                    fullPath,
                    safeAccess(accessStr),
                    safeType(typeStr),
                    parentPackage
            );

            // Parse Attributes
            if (root.has("attributes")) {
                JSONArray attrs = root.getJSONArray("attributes");
                for (int i = 0; i < attrs.length(); i++) {
                    JSONObject attrJson = attrs.getJSONObject(i);
                    String attrName = attrJson.getString("name");
                    String attrAccess = attrJson.optString("access", "PRIVATE").toUpperCase();
                    String attrType = attrJson.optString("type", "Object");

                    MetamodelFactory.createAttr(
                            fullPath + "." + attrName,
                            safeAccess(attrAccess),
                            attrType,
                            newClass
                    );
                }
            }

            // Parse Relationships
            if (root.has("relationships")) {
                JSONArray rels = root.getJSONArray("relationships");
                for (int i = 0; i < rels.length(); i++) {
                    JSONObject relJson = rels.getJSONObject(i);
                    String target = relJson.getString("target");
                    String relTypeStr = relJson.getString("type").toUpperCase();

                    MetamodelFactory.createRelationship(newClass, target, safeRelType(relTypeStr));
                }
            }

            // Parse  Constraints
            if (root.has("constraints")) {
                JSONArray ocls = root.getJSONArray("constraints");
                for (int i = 0; i < ocls.length(); i++) {
                    newClass.addConstraint(ocls.getString(i));
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing ai for system '" + system.getName() + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Pack getOrCreatePackageHierarchy(String fullClassName, Sys system) {
        String[] parts = fullClassName.split("\\.");

        if (parts.length <= 1) {
            return getOrCreateRootPackage("default", system);
        }

        List<Pack> currentLevelPacks = system.getPacks();
        Pack currentPack = null;
        String currentPath = "";

        // Iterate parts, stopping before the Class Name
        for (int i = 0; i < parts.length - 1; i++) {
            String pName = parts[i];
            currentPath = (currentPath.isEmpty()) ? pName : currentPath + "." + pName;

            Pack found = null;
            for (Pack p : currentLevelPacks) {
                if (p.getName().equals(currentPath)) {
                    found = p;
                    break;
                }
            }

            if (found == null) {
                found = new Pack(currentPath);
                currentLevelPacks.add(found);
            }

            currentPack = found;
            currentLevelPacks = currentPack.getPacks();
        }

        return currentPack;
    }

    // Helper to force a package if one doesn't exist
    private Pack getOrCreateRootPackage(String pkgName, Sys system) {
        for (Pack p : system.getPacks()) {
            if (p.getName().equals(pkgName)) return p;
        }
        Pack newPack = new Pack(pkgName);
        system.getPacks().add(newPack);
        return newPack;
    }

    private AccessModifier safeAccess(String s) {
        try { return AccessModifier.valueOf(s); } catch (Exception e) { return AccessModifier.PUBLIC; }
    }

    private ClassType safeType(String s) {
        try { return ClassType.valueOf(s); } catch (Exception e) { return ClassType.CLASS; }
    }

    private RelationshipType safeRelType(String s) {
        if (s.equals("EXTENDS")) return RelationshipType.INHERITANCE;
        if (s.equals("IMPLEMENTS")) return RelationshipType.REALIZATION;
        try { return RelationshipType.valueOf(s); } catch (Exception e) { return RelationshipType.ASSOCIATION; }
    }
}