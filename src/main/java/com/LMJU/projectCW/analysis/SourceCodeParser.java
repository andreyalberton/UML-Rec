package com.LMJU.projectCW.analysis;

import com.LMJU.projectCW.core.MetamodelFactory;
import com.LMJU.projectCW.metamodel.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Stack;

/**
 * Turns the AI's raw JSON output into metamodel objects.
 *
 * This is the defensive layer — the AI's output is almost never perfect, so
 * we fix common formatting issues (single quotes, missing brackets,
 * null values) before trying to parse it. If something still breaks,
 * we log it and move on rather than crashing the whole batch.
 *
 * The parser is also flexible about key names — it checks for both
 * "classes" and "Classes", "fields" and "Fields", etc. because the
 * AI isn't always consistent with capitalisation.
 */
public class SourceCodeParser {

    /**
     * Parse the AI's JSON and add whatever we can recover to the system.
     * This method never throws — worst case it logs an error and returns.
     */
    public void parseAndAddToSystem(String jsonOutput, Sys currentSys) {
        try {
            // Step 1: basic string fixes (single quotes, null literals)
            String sanitized = jsonOutput.replace("'", "\"").replace(": null", ": \"void\"");

            // Step 2: stack-based bracket repair
            // Walk through the string tracking open brackets. If any are
            // still open at the end, close them so the JSON parser doesn't break.
            Stack<Character> stack = new Stack<>();
            boolean inString = false;
            for (int i = 0; i < sanitized.length(); i++) {
                char ch = sanitized.charAt(i);
                if (ch == '"' && (i == 0 || sanitized.charAt(i - 1) != '\\')) inString = !inString;

                if (!inString) {
                    if (ch == '{' || ch == '[') stack.push(ch);
                    else if (ch == '}' && !stack.isEmpty() && stack.peek() == '{') stack.pop();
                    else if (ch == ']' && !stack.isEmpty() && stack.peek() == '[') stack.pop();
                }
            }

            StringBuilder sb = new StringBuilder(sanitized);
            while (!stack.isEmpty()) {
                char open = stack.pop();
                sb.append(open == '{' ? "}" : "]");
            }
            sanitized = sb.toString();

            JSONObject root = new JSONObject(sanitized);

            // Step 3: find the system object — AI sometimes uses "System" or "system" or just puts classes at root
            JSONObject systemObj = root.has("System") ? root.getJSONObject("System") : root.optJSONObject("system");
            if (systemObj == null) systemObj = root;

            // Step 4: find and process packages and classes wherever they are
            if (systemObj.has("packages")) {
                JSONArray packs = systemObj.getJSONArray("packages");
                processPackages(packs, currentSys);

                if ((systemObj.has("classes") || systemObj.has("Classes")) && !currentSys.getPacks().isEmpty()) {
                    String classKey = systemObj.has("classes") ? "classes" : "Classes";
                    processClasses(systemObj.getJSONArray(classKey), currentSys.getPacks().get(0));
                }
            }

            if (root.has("classes") || root.has("Classes") || systemObj.has("classes") || systemObj.has("Classes")) {
                String classKey = root.has("Classes") ? "Classes" : (root.has("classes") ? "classes" : (systemObj.has("Classes") ? "Classes" : "classes"));
                JSONArray classesArray = root.has(classKey) ? root.getJSONArray(classKey) : systemObj.getJSONArray(classKey);

                Pack defaultPack = currentSys.getPacks().isEmpty() ? MetamodelFactory.createPack("default", currentSys) : currentSys.getPacks().get(0);
                processClasses(classesArray, defaultPack);
            }
            else if (systemObj.has("fields") || systemObj.has("Fields")) {
                String packageName = systemObj.optString("package", systemObj.optString("Package", "default"));
                Pack targetPack = MetamodelFactory.createPack(packageName, currentSys);

                String className = systemObj.optString("name", systemObj.optString("className", "UnknownClass"));
                C newClass = MetamodelFactory.createClass(className, AccessModifier.PUBLIC, ClassType.CLASS, targetPack);

                String fKey = systemObj.has("fields") ? "fields" : "Fields";
                processFields(systemObj.getJSONArray(fKey), newClass);

                String mKey = systemObj.has("methods") ? "methods" : "Methods";
                if (systemObj.has(mKey)) processMethods(systemObj.getJSONArray(mKey), newClass);
            }

        } catch (Exception e) {
            System.err.println("Robust Parser failed to recover AI output: " + e.getMessage());
        }
    }

    private void processPackages(JSONArray packages, Sys system) {
        for (int i = 0; i < packages.length(); i++) {
            JSONObject pkgObj = packages.getJSONObject(i);
            Pack pkg = MetamodelFactory.createPack(pkgObj.optString("name", "default"), system);

            if (pkgObj.has("classes")) {
                processClasses(pkgObj.getJSONArray("classes"), pkg);
            }
        }
    }

    private void processClasses(JSONArray classes, Pack pkg) {
        for (int i = 0; i < classes.length(); i++) {
            JSONObject classObj = classes.getJSONObject(i);
            String className = classObj.has("name") ? classObj.getString("name") : classObj.optString("Name", "UnknownClass");

            C newClass = MetamodelFactory.createClass(className, AccessModifier.PUBLIC, ClassType.CLASS, pkg);

            String fieldKey = classObj.has("fields") ? "fields" : (classObj.has("Fields") ? "Fields" : null);
            if (fieldKey != null) {
                processFields(classObj.getJSONArray(fieldKey), newClass);
            }

            String methodKey = classObj.has("methods") ? "methods" : (classObj.has("Methods") ? "Methods" : null);
            if (methodKey != null) {
                processMethods(classObj.getJSONArray(methodKey), newClass);
            }

            // Parse relationships if present
            String relKey = classObj.has("relationships") ? "relationships" : (classObj.has("Relationships") ? "Relationships" : null);
            if (relKey != null) {
                processRelationships(classObj.getJSONArray(relKey), newClass);
            }
        }
    }

    private void processFields(JSONArray fields, C clazz) {
        for (int i = 0; i < fields.length(); i++) {
            JSONObject f = fields.getJSONObject(i);
            String name = f.optString("name", "attr" + i);
            String type = f.optString("type", "String");

            String accessStr = f.optString("access", "PRIVATE");
            // Fields default to PRIVATE if the AI doesn't specify
            AccessModifier access = safeAccess(accessStr, AccessModifier.PRIVATE);

            MetamodelFactory.createAttr(name, access, type, clazz);
        }
    }

    private void processMethods(JSONArray methods, C clazz) {
        for (int i = 0; i < methods.length(); i++) {
            JSONObject m = methods.getJSONObject(i);
            String name = m.optString("name", "method" + i);

            String accessStr = m.optString("access", "PUBLIC");
            // Methods default to PUBLIC if not specified
            AccessModifier access = safeAccess(accessStr, AccessModifier.PUBLIC);

            MetamodelFactory.createMet(name, access, clazz);
        }
    }

    // Safely parse an access modifier string
    private AccessModifier safeAccess(String s, AccessModifier defaultModifier) {
        if (s == null || s.trim().isEmpty() || s.equalsIgnoreCase("null")) {
            return defaultModifier;
        }
        try {
            return AccessModifier.valueOf(s.toUpperCase());
        } catch (Exception e) {
            return defaultModifier;
        }
    }

    private ClassType safeType(String s) {
        try { return ClassType.valueOf(s.toUpperCase()); } catch (Exception e) { return ClassType.CLASS; }
    }

    private void processRelationships(JSONArray relationships, C clazz) {
        for (int i = 0; i < relationships.length(); i++) {
            try {
                JSONObject rel = relationships.getJSONObject(i);
                String target = rel.optString("target", rel.optString("targetClass", rel.optString("to", "")));
                String typeStr = rel.optString("type", rel.optString("relationType", "ASSOCIATION"));

                if (!target.isEmpty()) {
                    RelationshipType relType;
                    try {
                        relType = RelationshipType.valueOf(typeStr.toUpperCase());
                    } catch (Exception e) {
                        relType = RelationshipType.ASSOCIATION;
                    }
                    MetamodelFactory.createRelationship(clazz, target, relType);
                }
            } catch (Exception e) {
                System.err.println("Skipping malformed relationship: " + e.getMessage());
            }
        }
    }
}