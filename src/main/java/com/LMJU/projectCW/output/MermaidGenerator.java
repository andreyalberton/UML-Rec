package com.LMJU.projectCW.output;

import com.LMJU.projectCW.metamodel.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Converts the metamodel into Mermaid syntax, then
 * sends it to the Mermaid.ink API to get back an SVG image.
 *
 * The generator filters out framework classes
 * so the diagram only shows the user's own project classes.
 * It also applies proper UML conventions: stereotypes for interfaces
 * and abstract classes, and correct visibility symbols on members.
 */
public class MermaidGenerator {

    // We don't want Spring annotations or java.util types cluttering the diagram
    private static final Set<String> FRAMEWORK_CLASSES = new HashSet<>(Arrays.asList(
        // Spring Framework
        "ResponseEntity", "HttpHeaders", "MediaType", "HttpStatus",
        "RestController", "RequestMapping", "PostMapping", "GetMapping",
        "RequestParam", "RequestBody", "PathVariable", "MultipartFile",
        "Controller", "Service", "Repository", "Component", "Autowired",
        "SpringBootApplication", "SpringApplication",
        // Java Standard Library
        "String", "Integer", "Long", "Double", "Float", "Boolean", "Byte",
        "Character", "Short", "Object", "Class", "System", "Math",
        "List", "ArrayList", "LinkedList", "Map", "HashMap", "TreeMap",
        "Set", "HashSet", "TreeSet", "Collection", "Collections",
        "Arrays", "Optional", "Stream", "Iterator",
        "Exception", "RuntimeException", "IOException", "Error",
        "StringBuilder", "StringBuffer", "Thread", "Runnable",
        "File", "Path", "InputStream", "OutputStream", "Reader", "Writer",
        "URL", "URI", "HttpURLConnection",
        // JSON
        "JSONObject", "JSONArray", "JSONException", "JSONTokener",
        // Common test
        "Test", "Before", "After", "BeforeEach", "AfterEach",
        "Assertions", "Assert"
    ));

    /**
     * Generates Mermaid syntax with package namespaces and relationships,
     * then fetches the SVG from the Mermaid.ink API.
     */
    public static byte[] generateSvgBytes(Sys system) {
        StringBuilder mermaid = new StringBuilder("classDiagram\n");

        for (Pack p : system.getPacks()) {
            renderPackageWithNamespace(p, mermaid);
        }

        // Render relationships AFTER all classes are declared
        for (Pack p : system.getPacks()) {
            renderRelationships(p, mermaid);
        }

        System.out.println("[MERMAID SYNTAX]:\n" + mermaid);
        return fetchSvgFromApi(mermaid.toString());
    }

    /**
     * Renders classes grouped inside Mermaid namespace blocks for package visibility.
     * Uses: namespace packageName { class ClassName { ... } }
     */
    private static void renderPackageWithNamespace(Pack p, StringBuilder mermaid) {
        // Collect non-framework classes in this package
        List<C> projectClasses = new ArrayList<>();
        for (C c : p.getClasses()) {
            if (!isFrameworkClass(c.getName())) {
                projectClasses.add(c);
            }
        }

        if (!projectClasses.isEmpty()) {
            String pkgName = sanitizeName(p.getName());
            mermaid.append("    namespace ").append(pkgName).append(" {\n");

            for (C c : projectClasses) {
                renderClass(c, mermaid, "        ");
            }

            mermaid.append("    }\n");
        }

        // Recurse into sub-packages
        for (Pack sub : p.getPacks()) {
            renderPackageWithNamespace(sub, mermaid);
        }
    }

    /**
     * Renders a single class with its fields and methods, respecting
     * standard UML visual conventions:
     *   - INTERFACE        → <<interface>> stereotype (Mermaid renders name italic)
     *   - ABSTRACT_CLASS   → <<abstract>> stereotype (renders name italic)
     *   - ENUM             → <<enumeration>> stereotype
     *   - Access modifier  → +/- / # prefix on fields and methods
     */
    private static void renderClass(C c, StringBuilder mermaid, String indent) {
        String className = sanitizeName(c.getName());
        mermaid.append(indent).append("class ").append(className).append(" {\n");

        // Stereotype line first — Mermaid uses this to get the name
        // and is the good way to show non-concrete classifiers.
        String stereotype = stereotypeFor(c.getType());
        if (stereotype != null) {
            mermaid.append(indent).append("    ").append(stereotype).append("\n");
        }

        // Draw Fields with proper visibility markers
        for (Attr a : c.getAttrs()) {
            String symbol = visibilitySymbol(a.getAccess());
            String typeName = a.getCName().isEmpty() ? "Object" : a.getCName().get(0);
            typeName = typeName.replaceAll("<.*>", "");
            mermaid.append(indent).append("    ").append(symbol)
                    .append(typeName).append(" ").append(a.getName()).append("\n");
        }

        // Draw Methods with proper visibility markers
        for (Met m : c.getMets()) {
            String symbol = visibilitySymbol(m.getAccess());
            mermaid.append(indent).append("    ").append(symbol)
                    .append(m.getName()).append("()\n");
        }

        mermaid.append(indent).append("}\n");
    }

    /**
     * Map a class type to its UML type. Returns null for a plain
     * concrete class.
     */
    private static String stereotypeFor(ClassType type) {
        if (type == null) return null;
        switch (type) {
            case INTERFACE:      return "<<interface>>";
            case ABSTRACT_CLASS: return "<<abstract>>";
            case ENUM:           return "<<enumeration>>";
            case CLASS:
            default:             return null;
        }
    }

    /**
     * UML visibility symbols: + public, - private, # protected, ~ package.
     */
    private static String visibilitySymbol(AccessModifier access) {
        if (access == null) return "+";
        switch (access) {
            case PRIVATE:           return "-";
            case PROTECTED:         return "#";
            case PACKAGE_PROTECTED: return "~";
            case PUBLIC:
            default:                return "+";
        }
    }

    /**
     * Renders relationship arrows between classes.
     * Only renders if BOTH source and target are project classes (not framework).
     */
    private static void renderRelationships(Pack p, StringBuilder mermaid) {
        for (C c : p.getClasses()) {
            if (isFrameworkClass(c.getName())) continue;

            for (URel rel : c.getURels()) {
                // Skip relationships to framework classes
                if (isFrameworkClass(rel.getCName())) continue;

                String arrow = getRelationshipArrow(rel.getType());
                mermaid.append("    ")
                        .append(sanitizeName(c.getName()))
                        .append(" ").append(arrow).append(" ")
                        .append(sanitizeName(rel.getCName()))
                        .append("\n");
            }
        }

        for (Pack sub : p.getPacks()) {
            renderRelationships(sub, mermaid);
        }
    }

    /**
     * Check if a class name belongs to a common framework/library.
     */
    private static boolean isFrameworkClass(String name) {
        if (name == null) return true;
        return FRAMEWORK_CLASSES.contains(name);
    }

    // Map each relationship type to its Mermaid arrow syntax
    private static String getRelationshipArrow(List<RelationshipType> types) {
        if (types == null || types.isEmpty()) return "-->";
        RelationshipType primary = types.get(0);
        switch (primary) {
            case INHERITANCE: return "--|>";
            case REALIZATION: return "..|>";
            case COMPOSITION: return "*--";
            case AGGREGATION: return "o--";
            case DEPENDENCY: return "..>";
            case ASSOCIATION: return "-->";
            default: return "-->";
        }
    }

    /**
     * Sanitize names for Mermaid syntax (remove spaces, dots, special chars)
     */
    private static String sanitizeName(String name) {
        if (name == null) return "Unknown";
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    // Send the Mermaid syntax to mermaid.ink and get an SVG back.
    // This is the only external network call in the whole app —
    // and it only sends diagram notation, never source code.
    private static byte[] fetchSvgFromApi(String mermaidCode) {
        try {
            String encoded = Base64.getUrlEncoder().encodeToString(mermaidCode.getBytes());
            String apiUrl = "https://mermaid.ink/svg/" + encoded;

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (InputStream in = conn.getInputStream();
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    return out.toByteArray();
                }
            } else {
                System.err.println("Mermaid API returned: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            System.err.println("API Error: " + e.getMessage());
        }
        return null;
    }
}
