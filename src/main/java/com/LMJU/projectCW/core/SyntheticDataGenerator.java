package com.LMJU.projectCW.core;

import com.LMJU.projectCW.metamodel.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SyntheticDataGenerator {

    // Expanded pools for variety
    private static final String[] SYSTEMS = {"ECommerce", "Hospital", "School", "Library", "Bank", "Airline", "Logistics", "SocialMedia"};
    private static final String[] PACKAGES = {"core", "auth", "data", "utils", "model", "service", "controller", "legacy"};
    private static final String[] CLASSES = {"User", "Order", "Product", "Payment", "Patient", "Doctor", "Book", "Flight", "Manager", "Report"};
    private static final String[] TYPES = {"String", "int", "boolean", "double", "Date", "float", "long", "BigDecimal"};
    private static final String[] VERBS = {"calculate", "validate", "process", "fetch", "update", "delete", "create", "sync"};

    public static void main(String[] args) {
        // Generate 500 examples
        generateDataset(500);
    }

    public static void generateDataset(int count) {
        try (FileWriter writer = new FileWriter("training_data.jsonl")) {
            Random rand = new Random();

            for (int i = 0; i < count; i++) {
                String sysName = SYSTEMS[rand.nextInt(SYSTEMS.length)] + "_" + i;
                Sys system = MetamodelFactory.createSys(sysName);

                // Populate with data
                generateRandomContent(system, rand);

                // input: Generate Java systems
                String inputJava = generateChaoticJavaSource(system, rand);

                // output: Generate JSON
                String outputJson = generateJsonString(system);

                // write to file
                String jsonL = String.format("{\"instruction\": \"Analyse the Java code. Extract the architecture to strict JSON.\", \"input\": \"%s\", \"output\": %s}\n",
                        escapeJson(inputJava), outputJson);
                writer.write(jsonL);
            }
            System.out.println("Generated " + count + " training examples.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateRandomContent(Sys system, Random rand) {
        int packCount = rand.nextInt(3) + 1;
        List<C> allClasses = new ArrayList<>();

        for (int p = 0; p < packCount; p++) {
            Pack pack = MetamodelFactory.createPack("com." + system.getName().toLowerCase() + "." + PACKAGES[rand.nextInt(PACKAGES.length)], system);

            int classCount = rand.nextInt(3) + 1;
            for (int c = 0; c < classCount; c++) {
                String className = CLASSES[rand.nextInt(CLASSES.length)] + rand.nextInt(100);
                C newClass = MetamodelFactory.createClass(className, AccessModifier.PUBLIC, ClassType.CLASS, pack);
                allClasses.add(newClass);

                // Add 1 to 3 attributes
                int attrCount = rand.nextInt(3) + 1;
                for(int k=0; k<attrCount; k++) {
                    MetamodelFactory.createAttr("attr" + k, AccessModifier.PRIVATE, TYPES[rand.nextInt(TYPES.length)], newClass);
                }

                // Add 1 to 2 methods
                int metCount = rand.nextInt(2) + 1;
                for(int m=0; m<metCount; m++) {
                    MetamodelFactory.createMet(VERBS[rand.nextInt(VERBS.length)] + className, AccessModifier.PUBLIC, newClass);
                }
            }
        }
        // Create Relationships
        if (allClasses.size() > 1) {
            MetamodelFactory.createRelationship(allClasses.get(0), allClasses.get(1).getName(), RelationshipType.ASSOCIATION);
        }
    }

    // the engine: Generates normal Java
    private static String generateChaoticJavaSource(Sys system, Random rand) {
        StringBuilder sb = new StringBuilder();

        // Random header comment
        if (rand.nextBoolean()) sb.append("/**\n * Generated Legacy Code\n * Author: AutoGen\n */\n");

        for (Pack p : system.getPacks()) {
            sb.append("package ").append(p.getName()).append(";\n\n");

            // Random imports
            sb.append("import java.util.*;\n");
            if (rand.nextBoolean()) sb.append("import java.io.Serializable;\n");
            sb.append("\n");

            for (C c : p.getClasses()) {
                // Random Annotations
                if (rand.nextBoolean()) sb.append("@Entity\n");
                if (rand.nextBoolean()) sb.append("@Table(name=\"").append(c.getName().toUpperCase()).append("\")\n");

                sb.append("public class ").append(c.getName());
                if (rand.nextBoolean()) sb.append(" implements Serializable");
                sb.append(" {\n\n");

                // Attributes (with comments)
                for (Attr a : c.getAttrs()) {
                    if (rand.nextInt(10) > 7) sb.append("    // TODO: Deprecate this field\n");
                    sb.append("    ").append(a.getAccess()).append(" ").append(a.getCName().get(0)).append(" ").append(a.getName()).append(";\n");
                }
                sb.append("\n");

                // Relationships
                for (URel r : c.getURels()) {
                    sb.append("    @OneToMany\n");
                    sb.append("    private List<").append(r.getTarget()).append("> ").append(r.getTarget().toLowerCase()).append("s;\n");
                }
                sb.append("\n");

                // Methods with fake logic (trains the AI to ignore bodies)
                for (Met m : c.getMets()) {
                    sb.append("    ").append(m.getAccess()).append(" void ").append(m.getName()).append("() {\n");
                    // Insert random logic
                    int chaosLevel = rand.nextInt(3);
                    if (chaosLevel == 0) {
                        sb.append("        System.out.println(\"Processing...\");\n");
                    } else if (chaosLevel == 1) {
                        sb.append("        if (this.id > 0) { return; }\n");
                    } else {
                        sb.append("        // complex business logic here\n");
                        sb.append("        int x = 5 * 10;\n");
                    }
                    sb.append("    }\n\n");
                }

                sb.append("}\n"); // End Class
            }
        }
        return sb.toString();
    }

    //  Helper: Convert Objects to JSON
    private static String generateJsonString(Sys sys) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"System\": { \"name\": \"").append(sys.getName()).append("\", \"packages\": [");

        for (int i = 0; i < sys.getPacks().size(); i++) {
            Pack p = sys.getPacks().get(i);
            sb.append("{ \"name\": \"").append(p.getName()).append("\", \"classes\": [");

            for (int j = 0; j < p.getClasses().size(); j++) {
                C c = p.getClasses().get(j);
                sb.append("{ \"name\": \"").append(c.getName()).append("\", ");

                // Fields
                sb.append("\"fields\": [");
                for(int k=0; k < c.getAttrs().size(); k++){
                    Attr a = c.getAttrs().get(k);
                    sb.append("{\"name\": \"").append(a.getName()).append("\", \"type\": \"").append(a.getCName().get(0)).append("\"}");
                    if(k < c.getAttrs().size() -1) sb.append(",");
                }
                sb.append("], ");

                // Methods
                sb.append("\"methods\": [");
                for(int k=0; k < c.getMets().size(); k++){
                    Met m = c.getMets().get(k);
                    sb.append("{\"name\": \"").append(m.getName()).append("\"}");
                    if(k < c.getMets().size() -1) sb.append(",");
                }
                sb.append("], ");

                // Relationships
                sb.append("\"relationships\": [");
                for(int k=0; k < c.getURels().size(); k++){
                    URel r = c.getURels().get(k);
                    sb.append("{ \"target\": \"").append(r.getTarget()).append("\", \"type\": \"ASSOCIATION\" }");
                    if(k < c.getURels().size() -1) sb.append(",");
                }
                sb.append("] }");
                if(j < p.getClasses().size() -1) sb.append(",");
            }
            sb.append("] }");
            if(i < sys.getPacks().size() -1) sb.append(",");
        }
        sb.append("] } }");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t");
    }
}