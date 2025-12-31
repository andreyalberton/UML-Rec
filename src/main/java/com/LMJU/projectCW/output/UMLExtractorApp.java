package com.LMJU.projectCW.output;

import com.LMJU.projectCW.core.ModelAbstractor;
import com.LMJU.projectCW.metamodel.Sys;

import java.util.List;

public class UMLExtractorApp {

    public static void main(String[] args) {
        // This will point to the dataset folder
        // Structure is:
        // /dataset
        //    /ProjectX (contains Java files)
        //    /ProjectY (contains Java files)
        String datasetPath = "src/main/resources/dataset";

        System.out.println(" BATCH UML EXTRACTION ENGINE ");
        System.out.println("-------------------------------\n");

        // Initialize the Engine
        ModelAbstractor engine = new ModelAbstractor();

        // Run the Batch Process
        // This loops through folders
        List<Sys> allSystems = engine.generateSystemsFromDirectory(datasetPath);

        System.out.println("\n---------------------------");
        System.out.println("   GENERATION COMPLETE: " + allSystems.size() + " SYSTEMS BUILT");
        System.out.println("---------------------------\n");

        // Output Results in xml
        UMLCodeParser xmlRenderer = new UMLCodeParser();

        for (Sys sys : allSystems) {
            System.out.println("\n---------------------------");
            System.out.println("--- System: " + sys.getName() + " ---");
            System.out.println("---------------------------\n");

            // Generate valid XML
            String xml = xmlRenderer.parse(sys);

            System.out.println(xml);
        }
        }
    }
