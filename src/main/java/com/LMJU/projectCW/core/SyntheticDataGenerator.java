package com.LMJU.projectCW.core;

import com.LMJU.projectCW.metamodel.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Used at the start of development to generate the 500-example JSONL training dataset.
 *
 * The idea: you can't find a dataset of messy Java code paired with
 * correct JSON metamodel output on the internet, so we make our own.
 * Each example is a random Java system with intentional problems
 * (misleading comments, weird formatting, fake annotations) paired
 * with the correct JSON that the AI should produce.
 *
 * This was used to inform the prompt engineering rules — by studying
 * which examples tripped the model up, we figured out which rules
 * to add to the prompt.
 */
public class SyntheticDataGenerator {

    // Pools of realistic names so the generated code looks plausible
    private static final String[] SYSTEMS = {"ECommerce", "Hospital", "School", "Library", "Bank", "Airline", "Logistics", "SocialMedia"};
    private static final String[] PACKAGES = {"core", "auth", "data", "utils", "model", "service", "controller", "legacy"};
    private static final String[] CLASSES = {"User", "Order", "Product", "Payment", "Patient", "Doctor", "Book", "Flight", "Manager", "Report"};
    private static final String[] TYPES = {"String", "int", "boolean", "double", "Date", "float", "long", "BigDecimal"};
    private static final String[] VERBS = {"calculate", "validate", "process", "fetch", "update", "delete", "create", "sync"};

    // Run this to regenerate the dataset — outputs training_data.jsonl
    public static void main(String[] args) {
        generateDataset(500);
    }

    /**
     * Main generation loop. For each example:
     *   1) build a random metamodel (Sys -> Pack -> C -> Attr/Met)
     *   2) render it as messy Java code (the "input" the AI will see)
     *   3) render it as clean JSON (the "output" the AI should produce)
     *   4) write both as one JSONL line
     */
    public static void generateDataset(int count) {
        try (FileWriter writer = new FileWriter("training_data.jsonl")) {
            Random rand = new Random();

            for (int i = 0; i < count; i++) {
                String sysName = SYSTEMS[rand.nextInt(SYSTEMS.length)] + "_" + i;
                Sys system = MetamodelFactory.createSys(sysName);

                generateRandomContent(system, rand);

                String inputJava = generateChaoticJavaSource(system, rand);
                String outputJson = generateJsonString(system);

                String jsonL = String.format("{\"instruction\": \"Analyse the Java code. Extract the architecture to strict JSON.\", \"input\": \"%s\", \"output\": %s}\n",
                        escapeJson(inputJava), outputJson);
                writer.write(jsonL);
            }
            System.out.println("Generated " + count + " training examples.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Fills the system with random packages, classes, fields, methods,
    // and at least one relationship if there are multiple classes
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

    // Turns a clean metamodel into intentionally messy Java source code.
    // Adds random annotations, misleading comments, fake method bodies —
    // all the things that confuse a traditional parsers.
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

    // Converts the metamodel to the JSON format the AI should output.
    // This is the ground truth — always correct, even when the Java code is messy.
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