package com.LMJU.projectCW.analysis;

import com.LMJU.projectCW.core.MetamodelFactory;
import com.LMJU.projectCW.metamodel.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Stack;

public class SourceCodeParser {

    public void parseAndAddToSystem(String jsonOutput, Sys currentSys) {
        try {
            // 1. FIX FORMATTING
            String sanitized = jsonOutput.replace("'", "\"").replace(": null", ": \"void\"");

            // 2. BULLETPROOF JSON REPAIR
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

            // 3. FLEXIBLE SEARCHING
            JSONObject systemObj = root.has("System") ? root.getJSONObject("System") : root.optJSONObject("system");
            if (systemObj == null) systemObj = root;

            // 4. FIND CLASSES AND PACKAGES
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
        }
    }

    private void processFields(JSONArray fields, C clazz) {
        for (int i = 0; i < fields.length(); i++) {
            JSONObject f = fields.getJSONObject(i);
            String name = f.optString("name", "attr" + i);
            String type = f.optString("type", "String");

            String accessStr = f.optString("access", "PRIVATE");
            // CRITICAL FIX: Pass PRIVATE as the safety fallback
            AccessModifier access = safeAccess(accessStr, AccessModifier.PRIVATE);

            MetamodelFactory.createAttr(name, access, type, clazz);
        }
    }

    private void processMethods(JSONArray methods, C clazz) {
        for (int i = 0; i < methods.length(); i++) {
            JSONObject m = methods.getJSONObject(i);
            String name = m.optString("name", "method" + i);

            String accessStr = m.optString("access", "PUBLIC");
            // CRITICAL FIX: Pass PUBLIC as the safety fallback
            AccessModifier access = safeAccess(accessStr, AccessModifier.PUBLIC);

            MetamodelFactory.createMet(name, access, clazz);
        }
    }

    // CRITICAL FIX: Now accepts a default fallback so Fields don't accidentally become Public on parse errors
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
}